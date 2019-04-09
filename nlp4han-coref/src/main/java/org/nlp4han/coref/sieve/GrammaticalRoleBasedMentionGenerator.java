package org.nlp4han.coref.sieve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nlp4han.coref.centering.GrammaticalRoleRuleSet;
import org.nlp4han.coref.centering.MentionUtil;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreeNodeUtil;

public class GrammaticalRoleBasedMentionGenerator extends MentionGeneratorWrapper
{
	public GrammaticalRoleBasedMentionGenerator()
	{
		
	}
	
	public GrammaticalRoleBasedMentionGenerator(MentionGenerator generator)
	{
		this.generator = generator;
	}

	@Override
	public Document generate(Document doc)
	{
		Document d;
		if (this.generator != null)
			d = this.generator.generate(doc);
		else
			d = doc;
		
		List<TreeNode> trees = d.getTrees();
		
		List<List<Mention>> mentionsBySentences = new ArrayList<List<Mention>>();
		
		HashMap<String, List<String>> grammaticalRoleRuleSet = GrammaticalRoleRuleSet.getGrammaticalRoleRuleSet(); // 语法角色规则集
		
		for (int i=0 ; i<trees.size() ; i++)
		{
			TreeNode tree = trees.get(i);
			
			List<Mention> mentions = mentions(tree, grammaticalRoleRuleSet, i);
			
			mentionsBySentences.add(mentions);
		}
		
		this.addMentions(d, mentionsBySentences);
		return d;
	}
	
	/**
	 * 根据语法角色规则，从短语结构树中提取实体集
	 * 
	 * @param root
	 * @param grammaticalRoleRuleSet
	 * @return
	 */
	public List<Mention> mentions(TreeNode root, HashMap<String, List<String>> grammaticalRoleRuleSet, int sentenceIndex)
	{
		if (root == null)
			throw new RuntimeException("输入错误");
		
		List<Mention> result = new ArrayList<Mention>();
		
		Map<String, List<Mention>> maps = generateMentionsAndGrammaticalRole(root, grammaticalRoleRuleSet, sentenceIndex);
		
		Set<String> keys = maps.keySet();
		Iterator<String> it = keys.iterator();
		
		while (it.hasNext())
		{
			String key = it.next();
			result.addAll(maps.get(key));
		}
		
		return MentionUtil.sort(result);
	}
	
	/**
	 * 生成语法角色与实体的映射
	 * 
	 * @param tree
	 * @param grammaticalRoleRuleSet
	 * @return
	 */
	private Map<String, List<Mention>> generateMentionsAndGrammaticalRole(TreeNode tree,
			HashMap<String, List<String>> grammaticalRoleRuleSet, int sentenceIndex)
	{
		Map<String, List<Mention>> result = new HashMap<String, List<Mention>>();
		List<String> SBJRules = grammaticalRoleRuleSet.get("SBJ");
		if (SBJRules != null)
		{
			for (String rule : SBJRules)
			{
				List<TreeNode> nodes = parseOneRule(tree, rule);
				List<Mention> mentions = getMentions(nodes, "SBJ", sentenceIndex);
				if (mentions != null && !mentions.isEmpty() && !result.containsKey("SBJ"))
				{
					result.put("SBJ", mentions);
				}
				else if (mentions != null && !mentions.isEmpty() && result.containsKey("SBJ"))
				{
					List<Mention> e = result.get("SBJ");
					e.addAll(mentions);
				}
			}
		}

		List<String> IORules = grammaticalRoleRuleSet.get("IO");
		if (IORules != null)
		{
			for (String rule : IORules)
			{
				List<TreeNode> nodes = parseOneRule(tree, rule);
				List<Mention> mentions = getMentions(nodes, "IO", sentenceIndex);
				if (mentions != null && !mentions.isEmpty() && !result.containsKey("IO"))
				{
					result.put("IO", mentions);
				}
				else if (mentions != null && !mentions.isEmpty() && result.containsKey("IO"))
				{
					List<Mention> e = result.get("IO");
					e.addAll(mentions);
				}

			}
		}
		List<Mention> IOMentions = result.get("IO");

		List<String> OBJRules = grammaticalRoleRuleSet.get("OBJ");
		if (OBJRules != null)
		{
			for (String rule : OBJRules)
			{
				List<TreeNode> nodes = parseOneRule(tree, rule);
				List<Mention> mentions = getMentions(nodes, "OBJ", sentenceIndex);

				if (IOMentions != null && !IOMentions.isEmpty())
				{// 间接宾语不能同时为直接宾语
					for (int i = 0; i < mentions.size(); i++)
					{
						for (Mention iom : IOMentions)
						{
							if (iom.getHead().equals(mentions.get(i).getHead())
									&& iom.getHeadIndex() == mentions.get(i).getHeadIndex())
							{
								mentions.remove(i);
							}
						}
					}
				}

				if (mentions != null && !mentions.isEmpty() && !result.containsKey("OBJ"))
				{
					result.put("OBJ", mentions);
				}
				else if (mentions != null && !mentions.isEmpty() && result.containsKey("OBJ"))
				{
					List<Mention> e = result.get("OBJ");
					e.addAll(mentions);
				}
			}
		}

		return result;
	}
	
	/**
	 * 根据ruleStr规则，找出tree中与之结构相同的结点
	 */
	private List<TreeNode> parseOneRule(TreeNode tree, String ruleStr)
	{
		if (tree == null || ruleStr == null)
			throw new RuntimeException("输入错误");
		String[] str = ruleStr.split("#");
		if (str.length != 2)
			throw new RuntimeException("规则错误");
		String targetNodeName = str[0];
		String rule = str[1];
		List<TreeNode> result = new ArrayList<TreeNode>();

		List<String> parts = BracketExpUtil.stringToList(rule);

		List<TreeNode> nodes = TreeNodeUtil.getNodesWithSpecifiedName(tree, new String[] { parts.get(1) });

		for (int i = 0; i < nodes.size(); i++)
		{
			Map<Boolean, List<TreeNode>> r = parse(nodes.get(i), parts, 0, new EndSite(), targetNodeName);
			if (r.containsKey(true))
			{
				result.addAll(r.get(true));
			}
		}
		return result;
	}
	
	/**
	 * 递归算法，从一对括号的第一个结点node开始，找出符合规则的结点，startSite与endSite记录一对括号的位置
	 * 
	 * @param node
	 * @param parts
	 * @param startSite
	 * @param endSite
	 * @param targetNodeName
	 * @return
	 */
	private Map<Boolean, List<TreeNode>> parse(TreeNode node, List<String> parts, int startSite, EndSite endSite,
			String targetNodeName)
	{
		TreeNode currentNode = node;
		TreeNode father = null;
		int index = -1;
		TreeNode targetNode = null;
		Map<Boolean, List<TreeNode>> result = new HashMap<Boolean, List<TreeNode>>();
		for (int i = startSite + 2; i < parts.size(); i++)
		{
			if (parts.get(i).equals("("))
			{
				String tmp1 = parts.get(i + 1);
				if (tmp1.equals("_"))
				{
					tmp1 = targetNodeName;

				}
				List<TreeNode> tmp = TreeNodeUtil.getChildNodeWithSpecifiedName(currentNode, new String[] { tmp1 });
				boolean tag = false;
				EndSite end = new EndSite();
				for (int j = 0; j < tmp.size(); j++)
				{
					Map<Boolean, List<TreeNode>> map = parse(tmp.get(j), parts, i, end, targetNodeName);
					if (map.containsKey(true))
					{
						tag = true;
						if (result.containsKey(true))
						{
							if (!map.get(true).isEmpty())
							{
								List<TreeNode> nodes = result.get(true);
								nodes.addAll(map.get(true));
							}
						}
						else
						{
							result.put(true, map.get(true));
						}
					}

				}
				if (tag == false)
				{
					if (result.containsKey(true))
						result.remove(true);
					return result;
				}
				i = end.getEndSite();
			}
			else if (parts.get(i).equals(" "))
			{
			}
			else if (parts.get(i).equals(")"))
			{
				if (parts.get(startSite + 1).equals("_"))
					targetNode = node;
				if (!result.containsKey(true))
				{
					List<TreeNode> t = new ArrayList<TreeNode>();
					if (targetNode != null)
					{
						t.add(targetNode);
					}
					result.put(true, t);
				}
				else
				{
					if (targetNode != null)
					{
						result.get(true).add(targetNode);
					}
				}
				endSite.setEndSite(i);
				return result;
			}
			else
			{
				if (father == null)
				{
					father = node.getParent();
					index = TreeNodeUtil.getIndex(node);
				}

				if (parts.get(i).equals("_"))
				{
					if (index + 1 >= father.getChildrenNum() || !father.getChildName(index + 1).equals(targetNodeName))
					{
						if (result.containsKey(true))
							result.remove(true);
						return result;
					}
					index++;
					targetNode = father.getChild(index);
					currentNode = node.getParent().getChild(index);
				}
				else if (parts.get(i).equals("?"))
				{
					index++;
					currentNode = node.getParent().getChild(index);
				}
				else
				{
					if (index + 1 >= father.getChildrenNum() || !father.getChildName(index + 1).equals(parts.get(i)))
					{
						if (result.containsKey(true))
							result.remove(true);
						return result;
					}
					index++;
					currentNode = node.getParent().getChild(index);
				}
			}
		}
		return null;
	}
	
	private List<Mention> getMentions(List<TreeNode> nodes, String grammaticalRole, int sentenceIndex)
	{
		List<Mention> result = new ArrayList<Mention>();
		for (int i = 0; i < nodes.size(); i++)
		{
			List<Mention> tmp = getMention(nodes.get(i), grammaticalRole, sentenceIndex);
			if (tmp != null)
				result.addAll(tmp);
		}
		return result;
	}
	
	/**
	 * 根据结点与其对应的语法角色生成实体
	 */
	private List<Mention> getMention(TreeNode treeNode, String grammaticalRole, int index)
	{
		List<Mention> result = new ArrayList<Mention>();

		List<TreeNode> children = TreeNodeUtil.getChildNodeWithSpecifiedName(treeNode,
				new String[] { "NP", "DNP", "NN", "NR", "PN" });
		if (children.isEmpty())
		{
			generateAndAddMention(treeNode, grammaticalRole, result, index);
		}
		else
		{
			for (int i = 0; i < children.size(); i++)
			{
				if (children.get(i).getNodeName().equals("DNP"))
				{
					List<TreeNode> nodes = TreeNodeUtil.getChildNodeWithSpecifiedName(children.get(i),
							new String[] { "NP" });
					for (int j = 0; j < nodes.size(); j++)
					{
						generateAndAddMention(nodes.get(j), grammaticalRole, result, index);
					}
				}
				else
				{
					generateAndAddMention(children.get(i), grammaticalRole, result, index);
				}

			}
		}

		return result;
	}
	
	private void generateAndAddMention(TreeNode node, String grammaticalRole, List<Mention> es, int index)
	{
		TreeNode tmp;
//		if (node.getNodeName().equals("NP"))
//		{
		tmp = TreeNodeUtil.getHead(node);
//		}
//		else
//			tmp = node;
		if (tmp != null)
		{
			Mention e = new Mention();
			e.setHeadNode(tmp);
			e.setGrammaticalRole(grammaticalRole);
			e.setSentenceIndex(index);
			if (!es.contains(e))
				es.add(e);
		}
	}
	
	private class EndSite
	{
		private int endSite = 0;

		public EndSite()
		{

		}

		public int getEndSite()
		{
			return endSite;
		}

		public void setEndSite(int endSite)
		{
			this.endSite = endSite;
		}

		@Override
		public String toString()
		{
			return "[endSite=" + endSite + "]";
		}

	}
	
}
