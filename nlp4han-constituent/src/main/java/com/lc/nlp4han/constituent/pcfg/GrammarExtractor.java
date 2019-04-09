package com.lc.nlp4han.constituent.pcfg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.PlainTextByTreeStream;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;

/**
 * 文法抽取工具类
 * 
 * 从树库中抽取PCFG文法
 * 
 */
public class GrammarExtractor
{
	public static PCFG getPCFG(String fileName, String enCoding) throws IOException
	{
		return extractGrammar(fileName, enCoding);
	}

	/**
	 * 由括号表达式列表直接得到PCFG
	 */
	public static PCFG getPCFG(ArrayList<String> bracketStrList) throws IOException
	{
		PCFG grammar = brackets2PCFG(bracketStrList);

		return grammar;
	}

	private static PCFG extractGrammar(String fileName, String enCoding) throws IOException
	{
		ArrayList<String> bracketStrList = getBrackets(fileName, enCoding);

		// 括号表达式生成文法
		PCFG grammar = brackets2PCFG(bracketStrList);

		return grammar;

	}

	private static ArrayList<String> getBrackets(String fileName, String enCoding)
			throws IOException, FileNotFoundException
	{
		// 括号表达式树拼接成括号表达式String数组
		PlainTextByTreeStream ptbt = new PlainTextByTreeStream(new FileInputStreamFactory(new File(fileName)),
				enCoding);
		String bracketStr = ptbt.read();
		ArrayList<String> bracketStrList = new ArrayList<String>();
		while (bracketStr != null)
		{
			bracketStrList.add(bracketStr);
			bracketStr = ptbt.read();
		}
		ptbt.close();
		return bracketStrList;
	}

	// 由括号表达式的list得到对应的文法集合
	private static PCFG brackets2PCFG(ArrayList<String> bracketStrList) throws IOException
	{
		HashMap<String, Integer> posMap = new HashMap<String, Integer>();
		PCFG grammar = new PCFG();
		HashMap<PRule, Integer> ruleCounter = new HashMap<PRule, Integer>();

		for (String bracketStr : bracketStrList)
		{
			TreeNode rootNode1 = BracketExpUtil.generateTree(bracketStr);
			traverse(rootNode1, grammar, ruleCounter, posMap);
		}

		computeProbOfRule(grammar, ruleCounter);

		grammar.setPosProb(getPOSProb(posMap));

		return grammar;
	}

	/**
	 * 遍历树得到基本文法
	 */
	private static void traverse(TreeNode node, PCFG grammar, HashMap<PRule, Integer> ruleCounter,
			HashMap<String, Integer> posCount)
	{
		if (grammar.getStartSymbol() == null)
		{// 起始符提取
			grammar.setStartSymbol(node.getNodeName());
		}

		if (node.getChildren().size() == 0)
		{
			grammar.addTerminal(node.getNodeName());// 终结符提取
			return;
		}

		if (node.getChildren().size() == 1 && node.getChild(0).isLeaf()) // 词性标注提取
		{
			String string = node.getNodeName();
			if (posCount.keySet().contains(string))
			{
				posCount.put(string, posCount.get(string) + 1);
			}
			else
			{
				posCount.put(string, 1);
			}
		}
		grammar.addNonTerminal(node.getNodeName());// 非终结符提取

		if (node.getChildren() != null && node.getChildren().size() > 0)
		{
			RewriteRule R = new RewriteRule(node.getNodeName(), node.getChildren());
			PRule rule = new PRule(R, 0);

			addRuleCount(rule, ruleCounter);

			grammar.add(rule);// 添加规则

			for (TreeNode node1 : node.getChildren())
			{// 深度优先遍历
				traverse(node1, grammar, ruleCounter, posCount);
			}
		}
	}

	private static void addRuleCount(PRule rule, HashMap<PRule, Integer> ruleCounter)
	{
		if (ruleCounter.containsKey(rule))
		{
			ruleCounter.put(rule, ruleCounter.get(rule) + 1);
		}
		else
		{
			ruleCounter.put(rule, 1);
		}
	}

	private static void computeProbOfRule(PCFG grammar, HashMap<PRule, Integer> ruleCounter)
	{
		for (String nonTer : grammar.getNonTerminalSet())
		{
			Set<RewriteRule> ruleSet = grammar.getRuleByLHS(nonTer);
			int allNum = 0;
			for (RewriteRule rule : ruleSet)
			{
				PRule pr = (PRule) rule;
				allNum += ruleCounter.get(pr);
			}

			for (RewriteRule rule : ruleSet)
			{
				PRule pr = (PRule) rule;
				pr.setProb(1.0 * ruleCounter.get(rule) / allNum);
			}
		}
	}

	/**
	 * 由词性标注计数器得到词性标注概率
	 * 
	 * @param posCount
	 * @return
	 */
	private static HashMap<String, Double> getPOSProb(HashMap<String, Integer> posCount)
	{
		int sum = 0;
		HashMap<String, Double> map1 = new HashMap<String, Double>();
		for (String str : posCount.keySet())
		{
			sum += posCount.get(str);
		}

		for (String str : posCount.keySet())
		{

			double pro = 1.0 * posCount.get(str) / sum;
			map1.put(str, pro);
		}

		return map1;
	}

}
