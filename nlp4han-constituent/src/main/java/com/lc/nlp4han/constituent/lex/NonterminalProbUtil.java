package com.lc.nlp4han.constituent.lex;

import java.util.ArrayList;
import java.util.HashMap;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.lex.NPBUtil;

/**
 * 获得树库中非终结符的先验概率
 *
 */
public class NonterminalProbUtil
{
	/**
	 * 获得树库中非终结符的先验概率
	 * 
	 * @param bracketStrList
	 * @param type
	 * @return 
	 */
	public static HashMap<String, Double> getNonterminalProb(ArrayList<String> bracketStrList, String type)
	{
		HashMap<String, Integer> symbol2Count = new HashMap<String, Integer>();
		for (String bracketStr : bracketStrList)
		{
			TreeNode rootNode1 = BracketExpUtil.generateTree(bracketStr);
			
			if (type.equals("lex"))
				NPBUtil.labelNPB(rootNode1);
			
			traverse(rootNode1, symbol2Count);
		}
		
		HashMap<String, Double> map1 = computeProb(symbol2Count);
		
		return map1;
	}

	private static void traverse(TreeNode node, HashMap<String, Integer> symbol2Count)
	{
		if (node.getChildrenNum() == 0 || (node.getChildrenNum() == 1 && node.getChild(0).getChildrenNum() == 0))
		{
			return;
		}
		else
		{
			String str = node.getNodeName();
			if (symbol2Count.containsKey(str))
			{
				int n = symbol2Count.get(str) + 1;
				symbol2Count.put(str, n);
			}
			else
				symbol2Count.put(str, 1);
		}
		
		for (TreeNode node1 : node.getChildren())
			traverse(node1, symbol2Count);
	}

	private static HashMap<String, Double> computeProb(HashMap<String, Integer> symbol2Count)
	{
		int sum = 0;
		HashMap<String, Double> map1 = new HashMap<String, Double>();
		for (String str : symbol2Count.keySet())
			sum += symbol2Count.get(str);
		
		for (String str : symbol2Count.keySet())
		{
			double pro = 1.0 * symbol2Count.get(str) / sum;
			map1.put(str, pro);
		}
		
		return map1;
	}
}