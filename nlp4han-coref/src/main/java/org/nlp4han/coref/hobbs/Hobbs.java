package org.nlp4han.coref.hobbs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nlp4han.coref.AnaphoraResolution;
import org.nlp4han.coref.AnaphoraResult;
import org.nlp4han.coref.centering.CenteringBFP;
import org.nlp4han.coref.sieve.Document;

import com.lc.nlp4han.constituent.Path;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreeNodeUtil;

/**
 * 基于Hobbs算法的指代消解
 * 
 * @author 杨智超
 *
 */
public class Hobbs implements AnaphoraResolution
{

	private CandidateFilter filter;
	private List<TreeNode> constituentTrees;

	public Hobbs()
	{
		AttributeFilter attributeFilter = new AttributeFilter(new PNFilter()); // 组合过滤器
		attributeFilter.setAttributeGenerator(new AttributeGeneratorByDic()); // 装入属性生成器
		
		this.filter = attributeFilter;
	}

	public Hobbs(CandidateFilter filter)
	{
		this.filter = filter;
	}

	public void setFilter(CandidateFilter filter)
	{
		this.filter = filter;
	}

	/**
	 * HOBBS算法
	 * 
	 * @param constituentTrees
	 *            结构树集合
	 * @param pronoun
	 *            代词结点
	 * @return 先行词结点
	 */
	public TreeNode hobbs(List<TreeNode> constituentTrees, TreeNode pronoun)
	{
		this.constituentTrees = constituentTrees;
		TreeNode x;

		TreeNode tmp;
		List<TreeNode> candidateNodes;
		int index = constituentTrees.size() - 1;

		tmp = TreeNodeUtil.getFirstNPNodeUp(pronoun);
		x = TreeNodeUtil.getFirstNPNodeUp(tmp);
		if (x != null)
		{
			Path path = new Path(x, tmp);
			candidateNodes = TreeNodeUtil.getNPNodeOnLeftOfPath(x, path);
			filter.filter(candidateNodes);

			if (!candidateNodes.isEmpty())
			{ // 若存在NP结点，并且在x和该候选结点间存在NP或IP结点，则返回该结点
				candidateNodes = existNpOrIPNodeBetween2Nodes(candidateNodes, x);
				if (!candidateNodes.isEmpty())
				{
					for (int k = 0; k < candidateNodes.size(); k++)
					{
						if (TreeNodeUtil.getHead(candidateNodes.get(k)) != null)
							return candidateNodes.get(k);
					}
				}
			}
		}
		else
			x = tmp;
		
		while (index >= 0)
		{
			if (x == null || x.getParent() == null)
			{// 若结点x为最顶层IP结点，则在最近的前树中查找候选结点
				index--;
				if (index < 0)
					break;
				x = constituentTrees.get(index);
				candidateNodes = TreeNodeUtil.getNPNodes(x);
				filter.filter(candidateNodes);
				if (!candidateNodes.isEmpty())
				{
					for (int k = 0; k < candidateNodes.size(); k++)
					{
						if (TreeNodeUtil.getHead(candidateNodes.get(k)) != null)
							return candidateNodes.get(k);
					}
				}
			}
			else
			{
				tmp = TreeNodeUtil.getFirstNPOrIPNodeUp(x);
				Path path = new Path(x, tmp);
				x = tmp;
				if (TreeNodeUtil.isNPNode(x) && !dominateNNode(x, path))
				{// 若结点x为NP结点，且path没有穿过x直接支配的Nominal结点,则返回x
					return x;
				}
				
				candidateNodes = TreeNodeUtil.getNPNodeOnLeftOfPath(x, path);
				filter.filter(candidateNodes);
				if (!candidateNodes.isEmpty())
				{
					for (int k = 0; k < candidateNodes.size(); k++)
					{
						if (TreeNodeUtil.getHead(candidateNodes.get(k)) != null)
							return candidateNodes.get(k);
					}
				}
				
				if (TreeNodeUtil.isIPNode(x))
				{
					candidateNodes = getNPNodeOnRightOfPath(x, path);
					filter.filter(candidateNodes);
					if (!candidateNodes.isEmpty())
					{
						for (int k = 0; k < candidateNodes.size(); k++)
						{
							if (TreeNodeUtil.getHead(candidateNodes.get(k)) != null)
								return candidateNodes.get(k);
						}
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * 在根结点rootNode下，路径path右侧，从左至右，广度优先搜索NP结点。但不遍历低于任何遇到的NP或IP结点的分支。
	 * 
	 * @param rootNode
	 * @param path
	 * @return
	 */
	private List<TreeNode> getNPNodeOnRightOfPath(TreeNode rootNode, Path path)
	{
		List<TreeNode> result = new LinkedList<TreeNode>();
		List<TreeNode> candidates = TreeNodeUtil.getNodesWithSpecifiedNameOnLeftOrRightOfPath(rootNode, path, "right",
				new String[] { "NP" });
		boolean flag = false;
		for (TreeNode treeNode : candidates)
		{
			List<TreeNode> tmp = TreeNodeUtil.getNodesUpWithSpecifiedName(treeNode, new String[] { "NP", "IP" });
			for (int i = 0; i < tmp.size(); i++)
			{
				if (tmp.get(i).getParent() != null)
				{
					flag = true;
					break;
				}
			}
			if (flag)
			{
				flag = false;
				break;
			}
			result.add(treeNode);
		}
		return result;
	}

	/**
	 * 获得candidateNodes中与结点treeNode间存在NP或IP结点的结点
	 * 
	 * @param candidateNodes
	 * @param treeNode
	 * @return
	 */
	private List<TreeNode> existNpOrIPNodeBetween2Nodes(List<TreeNode> candidateNodes, TreeNode treeNode)
	{
		List<TreeNode> result = new LinkedList<TreeNode>();
		for (int i = 0; i < candidateNodes.size(); i++)
		{
			TreeNode tmp;
			tmp = candidateNodes.get(i);
			if (TreeNodeUtil.getNodesWithSpecifiedNameBetween2Nodes(tmp, treeNode, new String[] { "NP", "IP" })
					.size() > 0)
			{
				result.add(tmp);
			}
		}
		return result;
	}

	/**
	 * 路径path是否穿过结点treeNode直接支配的一个Nominal结点
	 * 
	 * @param treeNode
	 * @param path
	 * @return 若路径path穿过结点，返回true；否则，返回false
	 */
	private boolean dominateNNode(TreeNode treeNode, Path path)
	{
		List<TreeNode> candidates = TreeNodeUtil.getChildNodeWithSpecifiedName(treeNode,
				new String[] { "NP", "NN", "NR" });
		for (TreeNode tn : candidates)
		{
			if (path.contains(tn))
				return true;
		}
		return false;
	}

	public String resultStr(TreeNode pronoun, TreeNode antecedent)
	{
		if (pronoun == null || antecedent == null)
			return null;
		TreeNode root1 = TreeNodeUtil.getRootNode(pronoun);
		TreeNode root2 = TreeNodeUtil.getRootNode(antecedent);
		int site_s1 = -1;
		int site_s2 = -1;
		int site_pron = -1;
		int site_ante = -1;
		if (root1 == root2)
		{
			List<TreeNode> leaves = TreeNodeUtil.getAllLeafNodes(root1);
			site_s1 = site_s2 = constituentTrees.indexOf(root1);
			site_pron = leafSite(TreeNodeUtil.getString(pronoun), leaves);
			site_ante = leafSite(
					TreeNodeUtil.getString(TreeNodeUtil.getHead(antecedent)), leaves);
		}
		else
		{
			List<TreeNode> leaves1 = TreeNodeUtil.getAllLeafNodes(root1);
			List<TreeNode> leaves2 = TreeNodeUtil.getAllLeafNodes(root2);
			site_s1 = constituentTrees.indexOf(root1);
			site_s2 = constituentTrees.indexOf(root2);
			site_pron = leafSite(TreeNodeUtil.getString(pronoun), leaves1);
			site_ante = leafSite(
					TreeNodeUtil.getString(TreeNodeUtil.getHead(antecedent)), leaves2);
		}

		String nodeName_pron = TreeNodeUtil.getString(pronoun);
		String nodeName_ante = TreeNodeUtil
				.getString(TreeNodeUtil.getHead(antecedent));
		String result = nodeName_pron + "(" + (site_s1 + 1) + "-" + (site_pron + 1) + ")" + CenteringBFP.SEPARATOR
				+ nodeName_ante + "(" + (site_s2 + 1) + "-" + (site_ante + 1) + ")";

		return result;
	}

	private int leafSite(String nodeName, List<TreeNode> nodes)
	{
		if (nodeName == null || nodes == null)
			throw new RuntimeException("输入错误");
		for (int i = 0; i < nodes.size(); i++)
		{
			if (nodes.get(i).getNodeName().equals(nodeName))
				return i;
		}
		return -1;
	}

	@Override
	public List<AnaphoraResult> resolve(List<TreeNode> sentences)
	{
		List<AnaphoraResult> result = new ArrayList<AnaphoraResult>();
		List<TreeNode> prons = new ArrayList<TreeNode>();
		for (int i=0 ; i<sentences.size() ; i++)
		{
			List<TreeNode> tmp = TreeNodeUtil.getNodesWithSpecifiedName(sentences.get(i), new String[] {"PN"});
			if (tmp != null && !tmp.isEmpty())
			{
				prons.addAll(tmp);
			}
		}
		
		if (!prons.isEmpty())
		{
			if (filter == null)
			{
				AttributeFilter af = new AttributeFilter(new PNFilter()); // 组合过滤器
				af.setAttributeGenerator(new AttributeGeneratorByDic()); // 装入属性生成器
				
				filter = af;
			}
			
			for (int i=0 ; i<prons.size() ; i++)
			{
				filter.setReferenceConditions(prons.get(i));
				TreeNode anaphNode = hobbs(sentences, prons.get(i));
				//String resultStr = resultStr(prons.get(i), anaphNode);
				AnaphoraResult tmp = new AnaphoraResult(prons.get(i), anaphNode);
				result.add(tmp);
			}
		}
		
		return result;
	}

	@Override
	public AnaphoraResult resolve(List<TreeNode> sentences, TreeNode pronoun)
	{
		if (filter == null)
		{
			AttributeFilter af = new AttributeFilter(new PNFilter()); // 组合过滤器
			af.setAttributeGenerator(new AttributeGeneratorByDic()); // 装入属性生成器
			
			filter = af;
		}
		filter.setReferenceConditions(pronoun);
		TreeNode anaphNode = hobbs(sentences, pronoun);
		//String resultStr = resultStr(pronoun, anaphNode);
		AnaphoraResult result = new AnaphoraResult(pronoun, anaphNode);
		return result;
	}

	@Override
	public List<AnaphoraResult> resolve(Document doc)
	{
		List<TreeNode> sentences = doc.getTrees(); 
		return resolve(sentences);
	}
	
}
