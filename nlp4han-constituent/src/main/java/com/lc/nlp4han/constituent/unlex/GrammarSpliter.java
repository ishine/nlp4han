package com.lc.nlp4han.constituent.unlex;

import java.io.IOException;
import java.util.HashSet;

/**
 * 分裂语法
 * 
 * @author 王宁
 * 
 * 
 */
public class GrammarSpliter
{
	public static void splitGrammar(Grammar oldG, TreeBank treeBank)
	{
		splitRule(oldG.getbRules());
		splitRule(oldG.getuRules());
		splitRule(oldG.getLexicon().getPreRules());
		
		oldG.getNumSubsymbolArr().replaceAll(e -> Short.valueOf((short) (e * 2)));
		oldG.getNumSubsymbolArr().set(oldG.symbolIntValue(oldG.getStartSymbol()), (short) 1);
		
		// 让PreterminalRule概率归一化
		GrammarExtractor.normalizedPreTermianlRules(oldG);
		
		for (AnnotationTreeNode tree : treeBank.getTreeBank())
		{
			splitTreeAnnotation(tree, oldG);
		}

	}

	// public static void normalizedPreTermianlRules(Grammar g)
	// {
	// HashMap<Short, Double[]> sameHeadPRuleScoreSum = new HashMap<Short,
	// Double[]>();
	// for (PreterminalRule preRule : g.getLexicon().getPreRules())
	// {
	// short parent = preRule.parent;
	// short nSubP = g.getNumSubSymbol(parent);
	// if (!sameHeadPRuleScoreSum.containsKey(preRule.getParent()))
	// {
	// sameHeadPRuleScoreSum.put(preRule.parent, new
	// Double[g.getNumSubSymbol(preRule.getParent())]);
	// }
	// for (short i = 0; i < nSubP; i++)
	// {
	// if (sameHeadPRuleScoreSum.get(preRule.parent)[i] == null)
	// {
	// double tag_iScoreSum = 0.0;
	// for (PreterminalRule theRule : g.getPreRuleSetBySameHead(preRule.parent))
	// {
	// tag_iScoreSum += theRule.getScore(i);
	// }
	// sameHeadPRuleScoreSum.get(preRule.parent)[i] = tag_iScoreSum;
	// }
	// preRule.setScore(i, preRule.getScore(i) /
	// sameHeadPRuleScoreSum.get(preRule.parent)[i]);
	// }
	// }
	// }

	private static <T extends Rule> void splitRule(HashSet<T> rules)
	{
		for (T rule : rules)
		{
			rule.split();
		}
	}

	public static void splitTreeAnnotation(AnnotationTreeNode tree, Grammar g)
	{
		if (tree == null)
			return;
		if (tree.isLeaf())
			return;
		if (!(tree.getAnnotation().getSymbol() == g.symbolIntValue(g.getStartSymbol())))
			tree.getAnnotation().setNumSubSymbol((short) (g.getNumSubSymbol(tree.getAnnotation().getSymbol())));

		for (AnnotationTreeNode child : tree.getChildren())
		{
			if (!child.isLeaf())
				splitTreeAnnotation(child, g);
		}
	}

	public static void main()
	{
		Grammar g = null;
		try
		{
			g = LatentGrammarExtractorTool.getGrammar("C:\\Users\\hp\\Desktop\\test100tree.txt", "utf-8", 0, 0.5, 50,
					0.01, Lexicon.DEFAULT_RAREWORD_THRESHOLD);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		try
		{
			GrammarSpliter.splitGrammar(g, new TreeBank("C:\\Users\\hp\\Desktop\\test100tree.txt", false, "utf-8"));

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
