package com.lc.nlp4han.constituent.unlex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * 将分裂后的语法合并
 * 
 * @author 王宁
 * 
 */
public class GrammarMerger
{
	public static void mergeGrammar(Grammar grammar, TreeBank treeBank, double mergeRate, RuleCounter ruleCounter)
	{
		double[][] mergeWeight = computerMergerWeight(grammar, ruleCounter);
		
		// printMergeWeight(mergeWeight);
		ArrayList<Short> newNumSubsymbolArr = new ArrayList<>(
				Arrays.asList(new Short[grammar.getNumSubsymbolArr().size()]));
		Collections.copy(newNumSubsymbolArr, grammar.getNumSubsymbolArr());
		Short[][] mergeSymbols = getMergeSymbol(grammar, treeBank, mergeRate, newNumSubsymbolArr, mergeWeight);
		
		mergeRule(grammar.getbRules(), mergeSymbols, mergeWeight);
		mergeRule(grammar.getuRules(), mergeSymbols, mergeWeight);
		mergeRule(grammar.getLexicon().getPreRules(), mergeSymbols, mergeWeight);
		
		grammar.setNumSubsymbolArr(newNumSubsymbolArr);
		mergeWeight = null;
		
		mergeTrees(grammar, treeBank);
	}

	public static <T extends Rule> void mergeRule(Set<T> rules, Short[][] symbolToMerge, double[][] mergeWeight)
	{
		for (Rule rule : rules)
		{
			rule.merge(symbolToMerge, mergeWeight);
		}
	}

	public static void mergeTrees(Grammar g, TreeBank treeBank)
	{

		for (AnnotationTreeNode tree : treeBank.getTreeBank())
		{
			mergeTreeAnnotation(g, tree);
		}
	}

	public static void mergeTreeAnnotation(Grammar g, AnnotationTreeNode tree)
	{
		if (tree.isLeaf())
			return;
		
		tree.getAnnotation().setNumSubSymbol(g.getNumSubSymbol(tree.getAnnotation().getSymbol()));
		tree.forgetIOScoreAndScale();
		tree.getAnnotation().setInnerScores(null);
		tree.getAnnotation().setOuterScores(null);
		for (AnnotationTreeNode child : tree.getChildren())
		{
			mergeTreeAnnotation(g, child);
		}
	}

	// TODO:处理期望值为0的
	public static double[][] computerMergerWeight(Grammar g, RuleCounter ruleCounter)
	{
		double[][] mergerWeight = new double[g.getNumSymbol()][];
		// 根节点不分裂、不合并
		for (short i = 1; i < mergerWeight.length; i++)
		{
			mergerWeight[i] = new double[g.getNumSubsymbolArr().get(i)];
			for (short j = 0; j < mergerWeight[i].length; j++)
			{
				mergerWeight[i][j] = ruleCounter.sameParentRulesCounter.get(i)[j]
						/ (ruleCounter.sameParentRulesCounter.get(i)[j]
								+ ruleCounter.sameParentRulesCounter.get(i)[j + 1]);
				mergerWeight[i][j + 1] = ruleCounter.sameParentRulesCounter.get(i)[j + 1]
						/ (ruleCounter.sameParentRulesCounter.get(i)[j]
								+ ruleCounter.sameParentRulesCounter.get(i)[j + 1]);
				j++;
			}
		}
		return mergerWeight;
	}

	@SuppressWarnings("unchecked")
	public static Short[][] getMergeSymbol(Grammar g, TreeBank treeBank, double mergeRate,
			ArrayList<Short> newNumSubsymbolArr, double[][] mergeWeight)
	{
		ArrayList<Short>[] symbolToMerge = new ArrayList[g.getNumSymbol()];
		PriorityQueue<SentenceLikehood> senScoreGradient = new PriorityQueue<>();
		// 假设一个符号没有分裂,已知root 没有分裂
		for (short i = 1; i < g.getNumSymbol(); i++)
		{
			for (short j = 0; j < g.getNumSubSymbol(i); j++)
			{
				double tallyLogGradient = 0;
				for (AnnotationTreeNode tree : treeBank.getTreeBank())
				{
					double logSenScoreGradient = getMergeSymbolHelper(g, tree, i, j, mergeWeight);
					tallyLogGradient += logSenScoreGradient;
				}
				System.out.println(
						"若合并非终结符号" + g.symbolStrValue(i) + "的第" + j + "," + (j + 1) + "个子符号后树库似然比：" + tallyLogGradient);
				senScoreGradient.add(new SentenceLikehood(i, j, tallyLogGradient));
				j++;
			}
		}
		int mergeCount = (int) (senScoreGradient.size() * mergeRate);
		double litterBigger = 0;
		System.out.println("预计合并" + mergeCount + "对子符号。");
		for (int i = 0; i < mergeCount; i++)
		{
			SentenceLikehood s = senScoreGradient.poll();
			if (symbolToMerge[s.symbol] == null)
			{
				symbolToMerge[s.symbol] = new ArrayList<Short>();
			}
			if (s.logAllSenScoreGradient > 0.25)
			{
				System.out.println("实际合并" + i + "对子符号。");
				System.out.println("合并第" + i + "对子符号后，树库与合并前的似然值之比为：" + litterBigger);
				break;
			}
			litterBigger = s.logAllSenScoreGradient;
			symbolToMerge[s.symbol].add(s.subSymbolIndex);
			if (i == mergeCount - 1)
			{
				System.out.println("合并第" + (i + 1) + "对子符号后，合并前树库与合并后树库的似然值之比为：" + s.logAllSenScoreGradient);
			}
		}

		Short[][] mergeSymbols = new Short[symbolToMerge.length][];
		Short[] subSymbolToMerge;
		// assume a subState without split,root 没有分裂
		for (int i = 1; i < symbolToMerge.length; i++)
		{
			if (symbolToMerge[i] != null)
			{
				symbolToMerge[i].sort(new Comparator<Short>()
				{
					@Override
					public int compare(Short o1, Short o2)
					{
						if (o1 < o2)
							return -1;
						else if (o1 > o2)
							return 1;
						else
							return 0;
					}
				});

				subSymbolToMerge = symbolToMerge[i].toArray(new Short[symbolToMerge[i].size()]);
				mergeSymbols[i] = subSymbolToMerge;
				newNumSubsymbolArr.set(i, (short) (g.getNumSubSymbol((short) i) - subSymbolToMerge.length));
			}
		}
		return mergeSymbols;
	}

	public static double getMergeSymbolHelper(Grammar g, AnnotationTreeNode tree, short symbol, short subSymbolIndex,
			double[][] mergeWeight)
	{
		double logSenScoreGradient = 0;
		for (AnnotationTreeNode child : tree.getChildren())
		{
			if (!child.isLeaf())
				logSenScoreGradient += getMergeSymbolHelper(g, child, symbol, subSymbolIndex, mergeWeight);
		}
		if (tree.getAnnotation().getSymbol() == symbol && symbol != 0)
		{
			logSenScoreGradient += Math.log(TreeBank.calSentenceSocreIgnoreScale(tree)
					/ calSenSocreAssumeMergeState_iIgnoreScale(tree, subSymbolIndex, mergeWeight));
			// logSenScoreGradient += Math.log(TreeBank.calSentenceSocreIgnoreScale(tree))
			// - Math.log(calSenSocreAssumeMergeState_iIgnoreScale(tree, subSymbolIndex,
			// mergeWeight));
			// logSenScoreGradient += TreeBank.calLogSentenceSocre(tree)
			// - calLogSenSocreAssumeMergeState_i(tree, subSymbolIndex, mergeWeight);
		}
		return logSenScoreGradient;
	}

	/**
	 * @param 树中要合并的节点
	 * @param 树中要合并的节点的subStateIndex
	 * @return 树的似然值
	 */
	public static double calLogSenSocreAssumeMergeState_i(AnnotationTreeNode node, int subStateIndex,
			double[][] mergeWeight)
	{

		if (node.getAnnotation().getInnerScores() == null || node.getAnnotation().getOuterScores() == null)
			throw new Error("没有计算树上节点的内外向概率。");
		if (node.isLeaf())
			throw new Error("不能利用叶子节点计算内外向概率。");
		if (node.getAnnotation().getSymbol() == 0)// root不分裂不合并
			return 0;
		double sentenceScore = 0.0;
		Double[] innerScore = node.getAnnotation().getInnerScores();
		Double[] outerScores = node.getAnnotation().getOuterScores();
		int numSubState = innerScore.length;
		if (subStateIndex % 2 == 1)
		{
			subStateIndex = subStateIndex - 1;
		}
		for (int i = 0; i < numSubState; i++)
		{
			if (i == subStateIndex)
			{
				double iWeight = mergeWeight[node.getAnnotation().getSymbol()][i];
				double brotherWeight = mergeWeight[node.getAnnotation().getSymbol()][i + 1];
				if (iWeight == Double.NaN || brotherWeight == Double.NaN)
				{
					System.err.println(" count Of SubSymbol_Si and SubSymbol_Si+1 underFlow.");
				}
				sentenceScore += (iWeight * innerScore[i] + brotherWeight * innerScore[i + 1])
						* (outerScores[i] + outerScores[i + 1]);
				i++;
			}
			else
			{
				sentenceScore += innerScore[i] * outerScores[i];
			}
		}
		double logSenScore = Math.log(sentenceScore)
				+ 100 * (node.getAnnotation().getInnerScale() + node.getAnnotation().getOuterScale());
		return logSenScore;
	}

	public static double calSenSocreAssumeMergeState_iIgnoreScale(AnnotationTreeNode node, int subStateIndex,
			double[][] mergeWeight)
	{

		if (node.getAnnotation().getInnerScores() == null || node.getAnnotation().getOuterScores() == null)
			throw new Error("没有计算树上节点的内外向概率。");
		if (node.isLeaf())
			throw new Error("不能利用叶子节点计算内外向概率。");
		if (node.getAnnotation().getSymbol() == 0)// root不分裂不合并
			return 0;
		double sentenceScore = 0.0;
		Double[] innerScore = node.getAnnotation().getInnerScores();
		Double[] outerScores = node.getAnnotation().getOuterScores();
		int numSubState = innerScore.length;
		if (subStateIndex % 2 == 1)
		{
			subStateIndex = subStateIndex - 1;
		}
		for (int i = 0; i < numSubState; i++)
		{
			if (i == subStateIndex)
			{
				double iWeight = mergeWeight[node.getAnnotation().getSymbol()][i];
				double brotherWeight = mergeWeight[node.getAnnotation().getSymbol()][i + 1];
				if (iWeight == Double.NaN || brotherWeight == Double.NaN)
				{
					System.err.println(" count Of SubSymbol_Si and SubSymbol_Si+1 underFlow.");
				}
				sentenceScore += (iWeight * innerScore[i] + brotherWeight * innerScore[i + 1])
						* (outerScores[i] + outerScores[i + 1]);
				i++;
			}
			else
			{
				sentenceScore += innerScore[i] * outerScores[i];
			}
		}
		double senScore = sentenceScore;
		return senScore;
	}

	static class SentenceLikehood implements Comparable<SentenceLikehood>
	{
		short symbol;
		short subSymbolIndex;
		double logAllSenScoreGradient;

		SentenceLikehood(short symbol, short subSymbolIndex, double logAllSenScoreGradient)
		{
			this.logAllSenScoreGradient = logAllSenScoreGradient;
			this.symbol = symbol;
			this.subSymbolIndex = subSymbolIndex;
		}

		@Override
		public int compareTo(SentenceLikehood o)
		{
			if (this.logAllSenScoreGradient < o.logAllSenScoreGradient)
				return -1;
			else if (this.logAllSenScoreGradient > o.logAllSenScoreGradient)
				return 1;
			else
				return 0;
		}
	}

	public static void printMergeWeight(double[][] mergeWeight)
	{
		for (double[] arr : mergeWeight)
		{
			if (arr != null)
				for (double weight : arr)
				{
					System.out.print(weight + " ");
				}
			else
			{
				System.out.print("null");
			}
			System.out.println();
		}

	}

	public static void main(String[] args)
	{
		Boolean addParentLabel = false;
		// ROOT 0、A 1、B 2、C 3、E 4、F 5、G 6
		String[] sentences = { "(ROOT(A(B b)(C c)))", "(ROOT (A(E e)(F f)))" };
		short nSubROOT = 1;
		short nSubA = 2;
		short nSubB = 1;
		short nSubC = 2;
		short nSubE = 1;
		short nSubF = 1;
		TreeBank treeBank = new TreeBank();
		for (int i = 0; i < sentences.length; i++)
		{
			treeBank.addTree(sentences[i], addParentLabel);
		}
		treeBank.nonterminalTable.getNumSubsymbolArr().set(1, (short) 2);
		treeBank.nonterminalTable.getNumSubsymbolArr().set(3, (short) 2);
		UnaryRule ROOT_A = new UnaryRule((short) 0, nSubROOT, (short) 1, nSubA);
		double S_ROOT_A0 = 1.0 / 2.0;
		double S_ROOT_A1 = 1.0 / 2.0;
		ROOT_A.setSubRuleScore((short) 0, (short) 0, S_ROOT_A0);
		ROOT_A.setSubRuleScore((short) 0, (short) 1, S_ROOT_A1);

		BinaryRule A_BC = new BinaryRule((short) 1, nSubA, (short) 2, nSubB, (short) 3, nSubC);// A -> B C
		double S_A0_BC0 = 1.0 / 9.0;
		double S_A0_BC1 = 2.0 / 9.0;
		double S_A1_BC0 = 1.0 / 12.0;
		double S_A1_BC1 = 5.0 / 12.0;
		A_BC.setSubRuleScore((short) 0, (short) 0, (short) 0, S_A0_BC0);
		A_BC.setSubRuleScore((short) 0, (short) 0, (short) 1, S_A0_BC1);
		A_BC.setSubRuleScore((short) 1, (short) 0, (short) 0, S_A1_BC0);
		A_BC.setSubRuleScore((short) 1, (short) 0, (short) 1, S_A1_BC1);

		BinaryRule A_EF = new BinaryRule((short) 1, nSubA, (short) 4, nSubE, (short) 5, nSubE);// ROOT -> E F
		double S_A0_EF = 2.0 / 3.0;
		double S_A1_EF = 1.0 / 2.0;
		A_EF.setSubRuleScore((short) 0, (short) 0, (short) 0, S_A0_EF);
		A_EF.setSubRuleScore((short) 1, (short) 0, (short) 0, S_A1_EF);

		PreterminalRule Bb = new PreterminalRule((short) 2, nSubB, "b");
		Bb.setSubRuleScore((short) 0, 1.0);
		PreterminalRule Cc = new PreterminalRule((short) 3, nSubC, "c");
		Cc.setSubRuleScore((short) 0, 1.0);
		Cc.setSubRuleScore((short) 1, 1.0);
		PreterminalRule Ee = new PreterminalRule((short) 4, nSubE, "e");
		Ee.setSubRuleScore((short) 0, 1.0);
		PreterminalRule Ff = new PreterminalRule((short) 5, nSubF, "f");
		Ff.setSubRuleScore((short) 0, 1.0);
		Grammar grammar = new Grammar();
		grammar.setNontermianalTable(treeBank.nonterminalTable);
		grammar.add(ROOT_A);
		grammar.add(A_BC);
		grammar.add(A_EF);
		grammar.add(Bb);
		grammar.add(Cc);
		grammar.add(Ee);
		grammar.add(Ff);

		System.out.println("合并前");
		grammar.printRules();

		Short[][] symbolToMerge = { null, { 0 }, null, { 0 }, null, null };// A0、A1合并，C0、C1合并
		double[][] weights = { { 0.0 }, { 3.0 / 5.0, 2.0 / 5.0 }, { 1.0 }, { 1.0 / 4.0, 3.0 / 4.0 }, { 1 }, { 1 } };// ROOT不分裂
		mergeRule(grammar.getbRules(), symbolToMerge, weights);
		mergeRule(grammar.getuRules(), symbolToMerge, weights);
		mergeRule(grammar.getLexicon().getPreRules(), symbolToMerge, weights);
		treeBank.nonterminalTable.getNumSubsymbolArr().set(1, (short) 1);
		treeBank.nonterminalTable.getNumSubsymbolArr().set(3, (short) 1);
		System.out.println("合并后");
		grammar.printRules();
	}
}
