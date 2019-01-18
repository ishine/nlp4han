package org.nlp4han.coref.centering;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nlp4han.coref.AnaphoraResolution;
import org.nlp4han.coref.AnaphoraResult;
import org.nlp4han.coref.hobbs.AttributeFilter;
import org.nlp4han.coref.hobbs.AttributeGeneratorByDic;
import org.nlp4han.coref.hobbs.CandidateFilter;
import org.nlp4han.coref.hobbs.PNFilter;
import org.nlp4han.coref.sieve.Document;
import org.nlp4han.coref.sieve.GrammaticalRoleBasedMentionGenerator;
import org.nlp4han.coref.sieve.Mention;

import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreeNodeUtil;

/**
 * 中心理论的BFP算法
 * 
 * @author 杨智超
 *
 */
public class CenteringBFP implements AnaphoraResolution
{
	public static String SEPARATOR = "->"; // 指代结果中的分隔符
	private CandidateFilter attributeFilter;

	public CenteringBFP()
	{

	}

	/**
	 * 运行BFP算法
	 * 
	 * @param mentionsOfUtterances
	 *            话语的提及集
	 * @param rootNodesOfUtterances
	 *            话语的结构树集
	 * @return 生成消解后的Mention集合，其中每个Mention中的antecedent为其先行词
	 */
	private List<List<Mention>> run(List<List<Mention>> mentionsOfUtterances, List<TreeNode> rootNodesOfUtterances)
	{
		List<Center> centersOfUtterances = new ArrayList<Center>(); // Utterances的Center集合
		if (mentionsOfUtterances.size() > 1)
		{
			Center ui = generateCenter(mentionsOfUtterances.get(0), null, null, rootNodesOfUtterances.get(0), null);
			centersOfUtterances.add(ui);
			for (int i = 1; i < mentionsOfUtterances.size(); i++)
			{
				List<Mention> utter = mentionsOfUtterances.get(i);
				ui = generateCenter(utter, mentionsOfUtterances.get(i-1), ui, rootNodesOfUtterances.get(i), rootNodesOfUtterances.get(i - 1));
				centersOfUtterances.add(ui);
			}
			return extractMentionSet(centersOfUtterances);
		}
		return mentionsOfUtterances;
	}

	/**
	 * 提及e是否为代词
	 * 
	 * @param mention
	 *            待检测的提及
	 * @param root
	 *            提及e对应结点的根结点
	 * @return
	 */
	private boolean isPronoun(Mention mention, TreeNode root)
	{
		if (mention == null)
			throw new RuntimeException("输入错误");
		TreeNode node = mention2Node(mention, root);
		TreeNode pnNode = TreeNodeUtil.getFirstNodeUpWithSpecifiedName(node, new String[] { "PN" });
		if (pnNode != null)
			return true;
		else
			return false;
	}

	/**
	 * 从Center列表中抽取对应的Mention集
	 * 
	 * @param centersOfUtterances
	 * @return
	 */
	private List<List<Mention>> extractMentionSet(List<Center> centersOfUtterances)
	{
		if (centersOfUtterances != null)
		{
			List<List<Mention>> result = new ArrayList<List<Mention>>();
			for (int i = 0; i < centersOfUtterances.size(); i++)
			{
				List<Mention> tmp = new ArrayList<Mention>();
				
				for (Mention m : centersOfUtterances.get(i).getCf())
				{
					if (m.getAntecedent() != null)
					{
						tmp.add(m);
					}
				}
				result.add(tmp);
			}
			return result;
		}
		return null;
	}

	/**
	 * 设置属性过滤器
	 * 
	 * @param attributeFilter
	 */
	public void setAttributeFilter(AttributeFilter attributeFilter)
	{
		this.attributeFilter = attributeFilter;
	}

	/**
	 * 生成话语的最优的中心数值（Cb、Cf、Cp）
	 * 
	 * @param mentionsOfUi
	 *            当前会话的提及集
	 * @param centerOfUi_1
	 *            前句会话的提及中心
	 * @param rootOfUi
	 *            当前会话的根节点
	 * @param rootOfUi_1
	 *            前句会话的根节点
	 * @return
	 */
	public Center generateCenter(List<Mention> mentionsOfUi, List<Mention> mentionsOfUi_1, Center centerOfUi_1, TreeNode rootOfUi, TreeNode rootOfUi_1)
	{
		if (mentionsOfUi == null)
		{
			throw new RuntimeException("输入错误！");
		}
		if (centerOfUi_1 != null)
		{
			List<Mention> pronounMentions = getPronounMentions(mentionsOfUi, rootOfUi);
			if (pronounMentions.isEmpty())
				return new Center(mentionsOfUi, mentionsOfUi);
			List<List<Mention>> anaphorMentionsList = generateAllAnaphorMentions(pronounMentions, mentionsOfUi_1, centerOfUi_1, rootOfUi,
					rootOfUi_1);
			List<Center> candidates = new ArrayList<Center>();
			List<String> transitions = new ArrayList<String>();
			for (int i = 0; i < anaphorMentionsList.size(); i++)
			{
				List<Mention> newMentionsOfUi = replaceMention(mentionsOfUi, pronounMentions, anaphorMentionsList.get(i));
				Center c = new Center(mentionsOfUi, newMentionsOfUi); // 注意：第一个句子的时候，两个参数应该相同，Cb的值应为null
				candidates.add(c);
				String transition = getTransition(c, centerOfUi_1);
				transitions.add(transition);
			}
			if (!transitions.isEmpty())
			{
				int index = bestTransition(transitions);
				if (index > -1)
				{
					for (int i=0 ; i<pronounMentions.size() ; i++)
					{
						Mention temp = anaphorMentionsList.get(index).get(i);
						if (temp.getAntecedent() == null)
							pronounMentions.get(i).setAntecedent(temp);
						else
							pronounMentions.get(i).setAntecedent(temp.getAntecedent());
					}
					return candidates.get(index);
				}
			}
			return null;

		}
		else
		{// 两个参数相同，表示无指代，也表示第一个句子
			return new Center(mentionsOfUi, mentionsOfUi);
		}
	}

	/**
	 * 将提及集中的代词提及替换成回指的提及
	 * 
	 * @param mentionsOfUi
	 * @param pronounMentionsOfUi
	 * @param anaphorMentionsOfUi_1
	 * @return
	 */
	private List<Mention> replaceMention(List<Mention> mentionsOfUi, List<Mention> pronounMentionsOfUi,
			List<Mention> anaphorMentionsOfUi_1)
	{
		if (mentionsOfUi == null || pronounMentionsOfUi == null || anaphorMentionsOfUi_1 == null)
		{
			throw new RuntimeException("输入错误");
		}
		List<Mention> result = new ArrayList<Mention>();
		int index;
		for (int i = 0; i < mentionsOfUi.size(); i++)
		{
			if ((index = pronounMentionsOfUi.indexOf(mentionsOfUi.get(i))) != -1)
			{
				result.add(anaphorMentionsOfUi_1.get(index));
			}
			else
				result.add(mentionsOfUi.get(i));
		}
		return result;
	}

	/**
	 * 得到提及集中的代词提及
	 * 
	 * @param mentionsOfUi
	 * @param rootOfUi
	 * @return
	 */
	private List<Mention> getPronounMentions(List<Mention> mentionsOfUi, TreeNode rootOfUi)
	{
		if (mentionsOfUi != null)
		{
			List<Mention> result = new ArrayList<Mention>();
			if (mentionsOfUi.size() > 0)
			{
				for (Mention e : mentionsOfUi)
				{
					if (isPronoun(e, rootOfUi))
					{
						result.add(e);
					}
				}
			}
			return result;
		}
		return null;
	}

	/**
	 * 获取最佳Transition的索引
	 */
	private static int bestTransition(List<String> transitions)
	{
		if (transitions == null)
		{
			throw new RuntimeException("输入错误！");
		}
		if (transitions.isEmpty())
			return -1;
		int result;
		if ((result = transitions.indexOf("Continue")) != -1)
		{
			return result;
		}
		else if ((result = transitions.indexOf("Retain")) != -1)
		{
			return result;
		}
		else if ((result = transitions.indexOf("Smooth-Shift")) != -1)
		{
			return result;
		}
		else if ((result = transitions.indexOf("Rough-Shift")) != -1)
		{
			return result;
		}
		return -1;
	}

	/**
	 * 生成所有的回指提及集，用以确定Cb
	 */
	private List<List<Mention>> generateAllAnaphorMentions(List<Mention> pronounMentionsOfUi, List<Mention> mentionsOfUi_1, Center centerOfUi_1,
			TreeNode rootOfUi, TreeNode rootOfUi_1)
	{
		if (pronounMentionsOfUi == null || centerOfUi_1 == null)
		{
			throw new RuntimeException("输入错误");
		}
		if (pronounMentionsOfUi.size() < 1)
		{
			return new ArrayList<List<Mention>>();
		}
		List<List<Mention>> result;
		List<Mention> newMentionsOfUi_1 = centerOfUi_1.getCf();
		List<List<Mention>> tmp = new ArrayList<List<Mention>>();

		for (int i = 0; i < pronounMentionsOfUi.size(); i++)
		{
			List<Mention> candidates = getMatchingMentions(newMentionsOfUi_1, mentionsOfUi_1, pronounMentionsOfUi.get(i), attributeFilter,
					rootOfUi, rootOfUi_1);
			tmp.add(candidates);
		}
		if (tmp.size() < 1)
			return new ArrayList<List<Mention>>();
		result = transform(tmp);
		return result;
	}

	private static List<List<Mention>> transform(List<List<Mention>> mentionsList)
	{
		if (mentionsList == null)
			throw new RuntimeException("输入错误");
		List<List<Mention>> result = new ArrayList<List<Mention>>();
		List<Integer> indexQueue = new ArrayList<Integer>(); // 记录mentionsList中每一组List<Mention>被访问的位置

		for (int i = 0; i < mentionsList.size(); i++)
		{// 初始化 indexQueue
			indexQueue.add(0);
		}

		while (indexQueue.get(indexQueue.size() - 1) < mentionsList.get(indexQueue.size() - 1).size())
		{
			List<Mention> list = new ArrayList<Mention>();
			for (int i = 0; i < indexQueue.size(); i++)
			{
				Mention e = mentionsList.get(i).get(indexQueue.get(i));
				list.add(e);
			}
			result.add(list);
			indexQueue.set(0, indexQueue.get(0) + 1);

			for (int i = 0; i < indexQueue.size() - 1; i++)
			{
				if (indexQueue.get(i) >= mentionsList.get(i).size())
				{
					indexQueue.set(i, 0);
					indexQueue.set(i + 1, indexQueue.get(i + 1) + 1);
				}
				else
					break;
			}
		}

		return result;
	}

	/**
	 * mention为Ui中的代词提及，在Ui-1的提及集mentionsOfUi_1中找出属性相容的提及
	 * filter为属性过滤器，rootOfUi为Ui的结构树根结点，rootOfUi_1为Ui-1的结构树根结点
	 */
	private static List<Mention> getMatchingMentions(List<Mention> newMentionsOfUi_1, List<Mention> mentionsOfUi_1, Mention mention, CandidateFilter filter,
			TreeNode rootOfUi, TreeNode rootOfUi_1)
	{
		List<Mention> result = new ArrayList<Mention>();
		TreeNode leaf = mention2Node(mention, rootOfUi);
		TreeNode node = TreeNodeUtil.getFirstNodeUpWithSpecifiedName(leaf, new String[] { "NP", "PN" });
		List<TreeNode> nodes = new LinkedList<TreeNode>();
		List<TreeNode> nodes_copy = new LinkedList<TreeNode>();
		for (int i = 0; i < mentionsOfUi_1.size(); i++)
		{
			leaf = mention2Node(mentionsOfUi_1.get(i), rootOfUi_1);
			TreeNode tmp = TreeNodeUtil.getFirstNodeUpWithSpecifiedName(leaf, new String[] { "NP", "PN" });
			nodes.add(tmp);
			nodes_copy.add(tmp);
		}

		if (filter != null)
		{
			filter.setReferenceConditions(node);
//			filter.setFilteredNodes(nodes_copy);
			filter.filter(nodes_copy);
		}

		for (TreeNode n : nodes_copy)
		{
			if (incompatible(n, node))
				continue;
			else
			{
				int index = nodes.indexOf(n);
				result.add(newMentionsOfUi_1.get(index));
			}
		}

		return result;
	}

	/**
	 * node1与node2不相容的规则
	 * 
	 * @param node1
	 * @param node2
	 * @return
	 */
	private static boolean incompatible(TreeNode node1, TreeNode node2)
	{
		if (node1.getNodeName().equals("PN") && node2.getNodeName().equals("PN")
				&& !TreeNodeUtil.getString(node1).equals(TreeNodeUtil.getString(node2)))
		{// node1与node2都是"PN"（代词），但他们是不同的代词，则node1与node2不相容
			return true;
		}
		return false;
	}

	/**
	 * 在结构树root中找出提及mention对应的结点
	 */
	private static TreeNode mention2Node(Mention mention, TreeNode root)
	{
		if (mention == null && root == null)
			throw new RuntimeException("输入错误");
		TreeNode result = TreeNodeUtil.string2Node(mention.getHead(), mention.getHeadIndex(), root);
		return result;
	}

	/**
	 * 根据前后两句的Center，获得相应的Transition状态
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static String getTransition(Center centerOfUi, Center centerOfUi_1)
	{// 注意：若Ui-1为首句，其Cb为undefined(null)
		Mention cbOfUi = null;
		Mention cpOfUi = null;
		Mention cbOfUi_1 = null;
		
		cbOfUi = centerOfUi.getCb().getAntecedent() == null ? centerOfUi.getCb() : centerOfUi.getCb().getAntecedent();
		cpOfUi = centerOfUi.getCp().getAntecedent() == null ? centerOfUi.getCp() : centerOfUi.getCp().getAntecedent();
		
		if (centerOfUi_1.getCb() != null)
			cbOfUi_1 = centerOfUi_1.getCb().getAntecedent() == null ? centerOfUi_1.getCb() : centerOfUi_1.getCb().getAntecedent();
		
		
		if (centerOfUi != null && centerOfUi_1 != null)
		{
			if (cbOfUi.equals(cpOfUi))
			{
				if (cbOfUi.equals(cbOfUi_1) || cbOfUi_1 == null)
					return "Continue";
				if (!cbOfUi.equals(cbOfUi_1))
					return "Smooth-Shift";
			}
			else
			{
				if (cbOfUi.equals(cbOfUi_1) || cbOfUi_1 == null)
					return "Retain";
				if (!cbOfUi.equals(cbOfUi_1))
					return "Rough-Shift";
			}
		}
		return null;
	}

	/**
	 * 根据会话的提及集，解析出以字符串形式表示的指代消解结果
	 * 
	 * @param oldMentionsSet
	 * @param mentionsSet
	 * @return
	 */
	public static List<String> analysisResult(List<List<Mention>> mentionsSet)
	{
		if (mentionsSet == null)
			throw new RuntimeException("输入错误");
		if (mentionsSet.size() < 2)
			return new ArrayList<String>();
		List<String> result = new ArrayList<String>();
		for (int i = 1; i < mentionsSet.size(); i++)
		{
			for (int j = 0; j < mentionsSet.get(i).size(); j++)
			{
				Mention temp = mentionsSet.get(i).get(j);
				String word1 = temp.getHead();
				String size1 = "(" + (i + 1) + "-" + (temp.getHeadIndex() + 1) + ")";
				String word2 = temp.getAntecedent().getHead();
				String size2 = "(" + (temp.getAntecedent().getSentenceIndex()+1) + "-" + (temp.getAntecedent().getHeadIndex() + 1) + ")";
				String str = word1 + size1 + SEPARATOR + word2 + size2;

					result.add(str);
			}
		}
		return result;
	}

	public static List<AnaphoraResult> analysisResult(List<List<Mention>> newMentionsSet, List<TreeNode> rootNodesOfUtterances)
	{
		if (newMentionsSet == null)
			throw new RuntimeException("输入错误");
		if (newMentionsSet.size() < 2)
			return new ArrayList<AnaphoraResult>();
		List<AnaphoraResult> result = new ArrayList<AnaphoraResult>();
		for (int i = 0; i < newMentionsSet.size(); i++)
		{
			for (int j = 0; j < newMentionsSet.get(i).size(); j++)
			{
				Mention temp = newMentionsSet.get(i).get(j);
				
				TreeNode root1 = rootNodesOfUtterances.get(i);
				TreeNode ponoun = TreeNodeUtil.getAllLeafNodes(root1).get(temp.getHeadIndex());
				
				TreeNode root2 = rootNodesOfUtterances.get(temp.getAntecedent().getSentenceIndex());
				TreeNode antecedent = TreeNodeUtil.getAllLeafNodes(root2).get(temp.getAntecedent().getHeadIndex());
				
				AnaphoraResult tmp = new AnaphoraResult(ponoun, antecedent);
				result.add(tmp);
			}
		}
		return result;
	}

	@Override
	public List<AnaphoraResult> resolve(List<TreeNode> sentences)
	{
		if (attributeFilter == null)
		{
			AttributeFilter af = new AttributeFilter(new PNFilter()); // 组合过滤器

			af.setAttributeGenerator(new AttributeGeneratorByDic()); // 装入属性生成器
			attributeFilter = af;
		}
		
		Document doc = new Document();
		doc.setTrees(sentences);
		
		GrammaticalRoleBasedMentionGenerator grbmg = new GrammaticalRoleBasedMentionGenerator();
		doc = grbmg.generate(doc);
		
		List<List<Mention>> eou = doc.getMentionsBySentences();

		List<List<Mention>> newMentions = run(eou, sentences);
		List<AnaphoraResult> result = analysisResult(newMentions, sentences);
		return result;
	}

	@Override
	public AnaphoraResult resolve(List<TreeNode> sentences, TreeNode pronoun)
	{
		List<AnaphoraResult> allResults = resolve(sentences);
		
		for (AnaphoraResult ar : allResults)
		{
			if (ar.getPronNode().equals(TreeNodeUtil.getAllLeafNodes(pronoun).get(0)))
				return ar;
		}
		return null;
	}

	@Override
	public List<AnaphoraResult> resolve(Document doc)
	{
		List<TreeNode> sentences = doc.getTrees();
		return resolve(sentences);
	}

}
