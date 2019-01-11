package com.lc.nlp4han.constituent.unlex;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 隐PCFG文法提取器
 * 
 * @author 王宁
 * 
 */
public class GrammarExtractor
{
	public static Random random = new Random(0);
	
	public TreeBank treeBank;
	public HashSet<String> dictionary;
	public List<Short> preterminal;// 词性标注对应的整数
	
	public HashMap<PreterminalRule, Integer>[] preRuleBySameHeadCount;// 长度与preterminal相同
	public HashMap<BinaryRule, Integer>[] bRuleBySameHeadCount;// 数组下标表示nonterminal对应的整数
	public HashMap<UnaryRule, Integer>[] uRuleBySameHeadCount;// 数组下标表示nonterminal对应的整数
	
	public int[] numOfSameHeadRule;
	public int rareWordThreshold;
	
	public HashMap<String, Double> wordCount;
	public RuleCounter ruleCounter;

	public Grammar extractLatentGrammar(String treeBankPath, String encoding, int rareWordThreshold,
			int SMCycle, int EMIterations, double mergeRate, double smooth) throws IOException
	{

		TreeBank treeBank = new TreeBank(treeBankPath, false, encoding);
		return extractLatentGrammar(treeBank, rareWordThreshold, SMCycle, EMIterations, mergeRate, smooth);
	}

	public Grammar extractLatentGrammar(TreeBank treeBank, int rareWordThreshold, int SMCycle,
			int EMIterations, double mergeRate, double smooth)
	{
		this.treeBank = treeBank;
		this.rareWordThreshold = rareWordThreshold;
		
		initGrammarExtractor();
		
		Grammar g = calcInitialGrammar();
		
		train(g, treeBank, SMCycle, mergeRate, EMIterations, smooth);
		
		return g;
	}

	public Grammar extractGrammarParentLabel(String treeBankPath, String encoding, int rareWordThreshold)
			throws IOException
	{
		TreeBank treeBank = new TreeBank(treeBankPath, true, encoding);
		
		return extractGrammarParentLabel(treeBank, rareWordThreshold);
	}

	public Grammar extractGrammarParentLabel(TreeBank treeBank, int rareWordThreshold)
	{
		this.treeBank = treeBank;
		this.rareWordThreshold = rareWordThreshold;
		
		initGrammarExtractor();
		
		return calcInitialGrammar();
	}

	/**
	 * 
	 * @return 初始语法
	 */
	private Grammar calcInitialGrammar()
	{
		// 规则计数
		calcInitialGRuleCount();
		
		// 计算文法初始概率
		calcInitalRuleScores();
		
		HashSet<BinaryRule> bRules;
		HashSet<UnaryRule> uRules;
		HashSet<PreterminalRule> preRules;
		HashMap<BinaryRule, Integer> allBRule = new HashMap<BinaryRule, Integer>();
		HashMap<PreterminalRule, Integer> allPreRule = new HashMap<PreterminalRule, Integer>();
		HashMap<UnaryRule, Integer> allURule = new HashMap<UnaryRule, Integer>();

		ArrayList<Short> tagWithRareWord = new ArrayList<Short>();
		ArrayList<Integer> rareWordCount = new ArrayList<Integer>();
		ArrayList<String> rareWord = new ArrayList<String>();
		int allRareWord = 0;

		for (HashMap<BinaryRule, Integer> map : this.bRuleBySameHeadCount)
			allBRule.putAll(map);
		
		for (HashMap<PreterminalRule, Integer> map : this.preRuleBySameHeadCount)
		{
			allPreRule.putAll(map);
			for (Map.Entry<PreterminalRule, Integer> entry : map.entrySet())
			{
				boolean flag = false;// 表示该规则的左部的tag是否添加到tagWithRareWord中
				if (entry.getValue() <= rareWordThreshold)
				{
					if (!flag)
					{
						tagWithRareWord.add(entry.getKey().getParent());
						rareWordCount.add(1);
						flag = true;
					}
					else
						rareWordCount.set(rareWordCount.size() - 1, rareWordCount.get(rareWordCount.size() - 1) + 1);
					
					allRareWord++;
				}
			}
		}
		
		for (HashMap<UnaryRule, Integer> map : this.uRuleBySameHeadCount)
			allURule.putAll(map);
		
		for (Map.Entry<String, Double> entry : wordCount.entrySet())
		{
			if (entry.getValue() <= rareWordThreshold)
				rareWord.add(entry.getKey());
		}
		
		bRules = new HashSet<BinaryRule>(allBRule.keySet());
		uRules = new HashSet<UnaryRule>(allURule.keySet());
		preRules = new HashSet<PreterminalRule>(allPreRule.keySet());
		Lexicon lexicon = new Lexicon(preRules, this.dictionary, tagWithRareWord, rareWordCount, rareWord, allRareWord);
		Grammar intialG = new Grammar(bRules, uRules, lexicon, treeBank.getNonterminalTable());
		
		double[][] subTag2UNKScores = new double[intialG.getNumSymbol()][1];
		for (double[] arr : subTag2UNKScores)
			arr[0] = 1;
		
		intialG.setSubTag2UNKScores(subTag2UNKScores);
		
		return intialG;
	}

	@SuppressWarnings("unchecked")
	private void initGrammarExtractor()
	{
		dictionary = new HashSet<String>();
		preterminal = treeBank.getNonterminalTable().getIntValueOfPreterminalArr();
		preRuleBySameHeadCount = new HashMap[preterminal.size()];
		
		for (int i = 0; i < preterminal.size(); i++)
			preRuleBySameHeadCount[i] = new HashMap<PreterminalRule, Integer>();
		
		bRuleBySameHeadCount = new HashMap[treeBank.getNonterminalTable().getNumSymbol()];
		for (int i = 0; i < treeBank.getNonterminalTable().getNumSymbol(); i++)
			bRuleBySameHeadCount[i] = new HashMap<BinaryRule, Integer>();
		
		uRuleBySameHeadCount = new HashMap[treeBank.getNonterminalTable().getNumSymbol()];
		for (int i = 0; i < treeBank.getNonterminalTable().getNumSymbol(); i++)
			uRuleBySameHeadCount[i] = new HashMap<UnaryRule, Integer>();
		
		numOfSameHeadRule = new int[this.treeBank.getNonterminalTable().getNumSymbol()];
		wordCount = new HashMap<>();
	}

	private void calcInitialGRuleCount()
	{
		ArrayDeque<AnnotationTreeNode> queue = new ArrayDeque<AnnotationTreeNode>();
		for (AnnotationTreeNode tree : treeBank.getTreeBank())
		{
			queue.offer(tree);
			while (!queue.isEmpty())
			{
				short parent, leftChild = -1, rightChild = -1;
				parent = queue.peek().getAnnotation().getSymbol();

				if (queue.peek().getChildren().size() == 2)
				{
					leftChild = queue.peek().getChildren().get(0).getAnnotation().getSymbol();
					rightChild = queue.peek().getChildren().get(1).getAnnotation().getSymbol();
					if (leftChild != -1 && rightChild != -1)
					{
						BinaryRule bRule = new BinaryRule(parent, leftChild, rightChild);
						if (!bRuleBySameHeadCount[parent].containsKey(bRule))
						{
							bRuleBySameHeadCount[parent].put(bRule, 1);
						}
						else
						{
							int count = bRuleBySameHeadCount[parent].get(bRule) + 1;
							bRuleBySameHeadCount[parent].remove(bRule);
							bRuleBySameHeadCount[parent].put(bRule, count);
						}
					}
				}
				else if (queue.peek().getChildren().size() == 1)
				{
					if (queue.peek().getChildren().get(0).getAnnotation().getWord() == null)
					{
						leftChild = queue.peek().getChildren().get(0).getAnnotation().getSymbol();
						if (leftChild != -1)
						{
							UnaryRule uRule = new UnaryRule(parent, leftChild);
							if (!uRuleBySameHeadCount[parent].containsKey(uRule))
							{
								uRuleBySameHeadCount[parent].put(uRule, 1);
							}
							else
							{
								int count = uRuleBySameHeadCount[parent].get(uRule) + 1;
								uRuleBySameHeadCount[parent].remove(uRule);
								uRuleBySameHeadCount[parent].put(uRule, count);
							}
						}
					}
					else
					{
						String child = queue.peek().getChildren().get(0).getAnnotation().getWord();
						dictionary.add(child);
						PreterminalRule preRule = new PreterminalRule(parent, child);
						if (!preRuleBySameHeadCount[preterminal.indexOf(parent)].containsKey(preRule))
						{
							preRuleBySameHeadCount[preterminal.indexOf(parent)].put(preRule, 1);
						}
						else
						{
							int count = preRuleBySameHeadCount[preterminal.indexOf(parent)].get(preRule) + 1;
							preRuleBySameHeadCount[preterminal.indexOf(parent)].remove(preRule);
							preRuleBySameHeadCount[preterminal.indexOf(parent)].put(preRule, count);
						}

						if (wordCount.containsKey(child))
							wordCount.put(child, wordCount.get(child) + 1);
						else
							wordCount.put(child, 1.0);
					}
				}

				numOfSameHeadRule[parent]++;
				for (AnnotationTreeNode child : queue.poll().getChildren())
				{
					if (!child.getChildren().isEmpty())
						queue.offer(child);
				}
			}
		}
		
//		calcInitalRuleScores();
	}

	// 计算初始文法的概率
	private void calcInitalRuleScores()
	{
		for (HashMap<BinaryRule, Integer> map : bRuleBySameHeadCount)
		{
			for (Map.Entry<BinaryRule, Integer> entry : map.entrySet())
			{
				double b1 = entry.getValue();
				double b2 = numOfSameHeadRule[entry.getKey().parent];
				double score = b1 / b2;
				entry.getKey().initScores((short) 1, (short) 1, (short) 1);
				entry.getKey().setScore((short) 0, (short) 0, (short) 0, score);
			}
		}
		
		for (HashMap<PreterminalRule, Integer> map : preRuleBySameHeadCount)
		{
			if (map.size() != 0)
				for (Map.Entry<PreterminalRule, Integer> entry : map.entrySet())
				{
					double b1 = entry.getValue();
					double b2 = numOfSameHeadRule[entry.getKey().parent];
					double score = b1 / b2;
					entry.getKey().initScores((short) 1);
					entry.getKey().setScore((short) 0, score);
				}
		}
		
		for (HashMap<UnaryRule, Integer> map : uRuleBySameHeadCount)
		{
			for (Map.Entry<UnaryRule, Integer> entry : map.entrySet())
			{
				double b1 = entry.getValue();
				double b2 = numOfSameHeadRule[entry.getKey().parent];
				double score = b1 / b2;
				entry.getKey().initScores((short) 1, (short) 1);
				entry.getKey().setScore((short) 0, (short) 0, score);
			}
		}
	}

	// 以下是语法训练
	public Grammar train(Grammar g, TreeBank treeBank, int SMCycle, double mergeRate, int EMIterations, double smooth)
	{
		treeBank.calcIOScore(g);
		
		double totalLSS = treeBank.calcLogTreeBankSentenceSocre();
		System.out.println("训练前树库似然值：" + totalLSS);
		
		System.out.println("SMCycle: " + SMCycle);
		System.out.println("EMIterations: " + EMIterations);
		System.out.println("startSymbol:" + g.getStartSymbol());
		for (int i = 0; i < SMCycle; i++)
		{
			System.out.println("->SMCycle: #" + i);
			
			System.err.println("开始分裂。");
			GrammarSpliter.splitGrammar(g, treeBank);
			System.out.println("分裂后：\n" + g.toStringAllSymbol());
			EM(g, treeBank, EMIterations);
			System.err.println("分裂EM完成。");

			System.err.println("开始合并。");
			GrammarMerger.mergeGrammar(g, treeBank, mergeRate, ruleCounter);
			System.out.println("合并后：\n" + g.toStringAllSymbol());
			EM(g, treeBank, EMIterations / 2);
			System.err.println("合并EM完成。");

			SmoothByRuleOfSameChild smoother = new SmoothByRuleOfSameChild(smooth);
			System.err.println("开始平滑规则。");
			smoother.smooth(g);
			normalizeBAndURule(g);
			normalizedPreTermianlRules(g);
			System.out.println("平滑后：\n" + g.toStringAllSymbol());
			EM(g, treeBank, EMIterations / 2);
			System.err.println("平滑EM完成。");
		}

		System.out.println("经" + SMCycle + "次SM周期后：\n" + g.toStringAllSymbol());
		return g;
	}

	/**
	 * 将处理后的语法期望最大化，得到新的规则
	 */
	private void EM(Grammar g, TreeBank treeBank, int iterations)
	{
		if (iterations > 0)
		{
			double totalLSS = 0;
			treeBank.calcIOScore(g);
			totalLSS = treeBank.calcLogTreeBankSentenceSocre();
			System.out.println("EM算法开始前树库的log似然值：" + totalLSS);
			
			double t1, t2, t3, t4;
			for (int i = 0; i < iterations; i++)
			{
				t1 = System.currentTimeMillis();
				calcRuleExpectation(g, treeBank);// 重新计算规则的期望， EStep完成。

				t2 = System.currentTimeMillis();
				recalculateRuleScore(g);// 刷新规则概率，MSetp完成。

				t3 = System.currentTimeMillis();
				treeBank.calcIOScore(g);// 刷新树库上节点内外向概率
				t4 = System.currentTimeMillis();
				totalLSS = treeBank.calcLogTreeBankSentenceSocre();
				
				System.out.println("第" + (i + 1) + "次EM迭代后树库的Log似然值：" + totalLSS + "………………calExpT:" + (t2 - t1) + "ms"
						+ ",calRuleST:" + (t3 - t2) + "ms" + ",calIOST:" + (t4 - t3) + "ms");
			}
			
			calcRuleExpectation(g, treeBank);
			System.out.println("EM算法结束。");
			System.out.println("EM算法结束后树库的log似然值：" + totalLSS);
		}

	}

	private void calcRuleExpectation(Grammar g, TreeBank treeBank)
	{
		ruleCounter = new RuleCounter();
		for (AnnotationTreeNode tree : treeBank.getTreeBank())
			refreshRuleCountExpectation(g, tree, tree);

		ruleCounter.calcSameParentRulesExpectation(g);
	}

	/**
	 * 跟新规则的scores
	 * 
	 * @param g
	 *            语法
	 */
	private void recalculateRuleScore(Grammar g)
	{
		double newScore;
		double denominator;
		for (BinaryRule bRule : g.getbRules())
		{

			int pNumSub = g.getNumSubSymbol(bRule.getParent());
			int lCNumSub = g.getNumSubSymbol(bRule.getLeftChild());
			int rCNumSub = g.getNumSubSymbol(bRule.getRightChild());

			for (short i = 0; i < pNumSub; i++)
			{
				if (ruleCounter.sameParentRulesCounter.get(bRule.parent)[i] != 0.0)
					denominator = ruleCounter.sameParentRulesCounter.get(bRule.parent)[i];
				else
					throw new Error("sameParentRulesCounter计算错误。");
				
				for (short j = 0; j < lCNumSub; j++)
				{
					for (short k = 0; k < rCNumSub; k++)
					{
						newScore = ruleCounter.bRuleCounter.get(bRule)[i][j][k] / denominator;
						if (newScore < Rule.ruleThres)
							newScore = 0.0;

						bRule.setScore(i, j, k, newScore);
					}
				}
			}

		}

		for (UnaryRule uRule : g.getuRules())
		{
			int pNumSub = g.getNumSubSymbol(uRule.getParent());
			int cNumSub = g.getNumSubSymbol(uRule.getChild());
			for (short i = 0; i < pNumSub; i++)
			{
				if (ruleCounter.sameParentRulesCounter.get(uRule.parent)[i] != 0.0)
					denominator = ruleCounter.sameParentRulesCounter.get(uRule.parent)[i];
				else
					throw new Error("sameParentRulesCounter计算错误。");
				
				for (short j = 0; j < cNumSub; j++)
				{
					newScore = ruleCounter.uRuleCounter.get(uRule)[i][j] / denominator;
					if (newScore < Rule.ruleThres)
						newScore = 0.0;

					uRule.setScore(i, j, newScore);
				}
			}
		}

		for (PreterminalRule preRule : g.getLexicon().getPreRules())
		{
			int pNumSub = g.getNumSubSymbol(preRule.parent);
			for (short i = 0; i < pNumSub; i++)
			{
				if (ruleCounter.sameParentRulesCounter.get(preRule.parent)[i] != 0.0)
					denominator = ruleCounter.sameParentRulesCounter.get(preRule.parent)[i];
				else
					throw new Error("sameParentRulesCounter计算错误。");
				
				newScore = ruleCounter.preRuleCounter.get(preRule)[i] / denominator;
				if (newScore < Rule.preRulethres)
					newScore = 0.0;

				preRule.setScore(i, newScore);
			}
		}
	}

	private double[][] calcTag2UNKScores(Grammar g)
	{
		double[][] subTag2UNKScores = new double[g.getNumSymbol()][];
		for (int tag = 0; tag < g.getNumSymbol(); tag++)
		{
			if (!g.hasPreterminalSymbol((short) tag))
				continue;
			subTag2UNKScores[tag] = new double[g.getNumSubSymbol((short) tag)];
			for (int subT = 0; subT < subTag2UNKScores[tag].length; subT++)
			{
				if (ruleCounter.sameTagToUNKCounter.containsKey((short) tag))
				{
					double subTagCount = ruleCounter.sameParentRulesCounter.get((short) tag)[subT];
					double subTagUNKCount = ruleCounter.sameTagToUNKCounter.get((short) tag)[subT];
					subTag2UNKScores[tag][subT] = subTagUNKCount / subTagCount;
					System.out.println(g.symbolStrValue((short) tag) + "_" + subT + " " + subTagUNKCount / subTagCount);
				}
				else
				{
					subTag2UNKScores[tag][subT] = 1;
					System.out.println(g.symbolStrValue((short) tag) + " 没有出现过UNK.");
				}
			}
		}
		return subTag2UNKScores;
	}

	public static void normalizeBAndURule(Grammar g)
	{
		HashMap<Short, Double[]> sameHeadRuleScoreSum = new HashMap<Short, Double[]>();
		for (short symbol = 0; symbol < g.getNumSymbol(); symbol++)
		{
			if (!g.hasPreterminalSymbol(symbol))
			{
				Double[] ruleScoreSum = new Double[g.getNumSubSymbol(symbol)];
				sameHeadRuleScoreSum.put(symbol, ruleScoreSum);
				Set<BinaryRule> sameHeadBSet = g.getbRuleSetBySameHead(symbol);
				if (sameHeadBSet != null)
					for (BinaryRule bRule : sameHeadBSet)
					{
						for (short i = 0; i < ruleScoreSum.length; i++)
						{
							if (ruleScoreSum[i] == null)
								ruleScoreSum[i] = 0.0;

							ruleScoreSum[i] += bRule.getParent_i_ScoceSum(i);
						}
					}
				
				Set<UnaryRule> sameHeadUSet = g.getuRuleSetBySameHead(symbol);
				if (sameHeadUSet != null)
					for (UnaryRule uRule : sameHeadUSet)
					{
						for (short i = 0; i < ruleScoreSum.length; i++)
						{
							if (ruleScoreSum[i] == null)
								ruleScoreSum[i] = 0.0;

							ruleScoreSum[i] += uRule.getParent_i_ScoceSum(i);
						}
					}
			}
		}

		for (BinaryRule bRule : g.getbRules())
		{
			short nSubParent = g.getNumSubSymbol(bRule.getParent());
			for (short subP = 0; subP < nSubParent; subP++)
			{
				double tag_iScoreSum = sameHeadRuleScoreSum.get(bRule.getParent())[subP];
				short nSubLC = g.getNumSubSymbol(bRule.getLeftChild());
				for (short subLC = 0; subLC < nSubLC; subLC++)
				{
					short nSubRC = g.getNumSubSymbol(bRule.getRightChild());
					for (short subRC = 0; subRC < nSubRC; subRC++)
					{
						double score = bRule.getScore(subP, subLC, subRC);
						bRule.setScore(subP, subLC, subRC, score / tag_iScoreSum);
					}
				}
			}
		}
		
		for (UnaryRule uRule : g.getuRules())
		{
			short nSubParent = g.getNumSubSymbol(uRule.getParent());
			for (short subP = 0; subP < nSubParent; subP++)
			{
				double tag_iScoreSum = sameHeadRuleScoreSum.get(uRule.getParent())[subP];
				short nSubC = g.getNumSubSymbol(uRule.getChild());
				for (short subC = 0; subC < nSubC; subC++)
				{
					double score = uRule.getScore(subP, subC);
					uRule.setScore(subP, subC, score / tag_iScoreSum);
				}
			}
		}
	}

	public static void normalizedPreTermianlRules(Grammar g)
	{
		HashMap<Short, Double[]> sameHeadPRuleScoreSum = new HashMap<Short, Double[]>();
		for (PreterminalRule preRule : g.getLexicon().getPreRules())
		{
			short parent = preRule.parent;
			short nSubP = g.getNumSubSymbol(parent);
			if (!sameHeadPRuleScoreSum.containsKey(parent))
				sameHeadPRuleScoreSum.put(parent, new Double[nSubP]);

			for (short i = 0; i < nSubP; i++)
			{
				Double sameHeadScoreSum = sameHeadPRuleScoreSum.get(parent)[i];
				if (sameHeadScoreSum == null)
				{
					BigDecimal tag_iScoreSum = BigDecimal.valueOf(0.0);
					for (PreterminalRule theRule : g.getPreRuleSetBySameHead(parent))
						tag_iScoreSum = tag_iScoreSum.add(BigDecimal.valueOf(theRule.getScore(i)));

					sameHeadPRuleScoreSum.get(preRule.parent)[i] = tag_iScoreSum.doubleValue();
					sameHeadScoreSum = tag_iScoreSum.doubleValue();
				}
				
				preRule.setScore(i, BigDecimal.valueOf(preRule.getScore(i))
						.divide(BigDecimal.valueOf(sameHeadScoreSum), 15, BigDecimal.ROUND_HALF_UP).doubleValue());
			}
		}
	}

	private void refreshRuleCountExpectation(Grammar g, AnnotationTreeNode root, AnnotationTreeNode tree)
	{
		if (tree.getChildren().size() == 0 || tree == null)
			return;
		
		double scalingFactor;
		Annotation rootLabel = root.getAnnotation();
		Annotation pLabel = tree.getAnnotation();
		short pSymbol = pLabel.getSymbol();
		short nSubP = g.getNumSubSymbol(pSymbol);
		double rootIS = rootLabel.getInnerScores()[0];
		Double[] pOutS = pLabel.getOuterScores();
		if (tree.getChildren().size() == 2)
		{
			AnnotationTreeNode lC = tree.getChildren().get(0);
			AnnotationTreeNode rC = tree.getChildren().get(1);
			Annotation lCLabel = lC.getAnnotation();
			Annotation rCLabel = rC.getAnnotation();
			short lcSymbol = lCLabel.getSymbol();
			short rcSymbol = rCLabel.getSymbol();
			short nSubLC = g.getNumSubSymbol(lcSymbol);
			short nSubRC = g.getNumSubSymbol(rcSymbol);
			BinaryRule rule = new BinaryRule(pSymbol, lcSymbol, rcSymbol);
			rule = g.getRule(rule);
			double[][][] count;
			if (!ruleCounter.bRuleCounter.containsKey(rule))
			{
				count = new double[nSubP][nSubLC][nSubRC];
				ruleCounter.bRuleCounter.put(rule, count);
			}
			else
			{
				count = ruleCounter.bRuleCounter.get(rule);
			}
			scalingFactor = ScalingTools.calcScaleFactor(pLabel.getOuterScale() + lCLabel.getInnerScale()
					+ rCLabel.getInnerScale() - rootLabel.getInnerScale());
			Double[] lCinnerS = lCLabel.getInnerScores();
			Double[] rCinnerS = rCLabel.getInnerScores();
			for (short i = 0; i < nSubP; i++)
			{
				double pOS = pOutS[i];
				if (pOS == 0)
					continue;
				for (short j = 0; j < nSubLC; j++)
				{
					double lCIS = lCinnerS[j];
					if (lCIS == 0)
						continue;
					for (short k = 0; k < nSubRC; k++)
					{
						double rCIS = rCinnerS[k];
						if (rCIS == 0)
							continue;
						double rS = rule.getScore(i, j, k);
						if (rS == 0)
							continue;
						count[i][j][k] = count[i][j][k] + (rS * lCIS / rootIS * rCIS * scalingFactor * pOS);
					}
				}
			}
		}
		else if (tree.getChildren().size() == 1 && tree.getChildren().get(0).getAnnotation().getWord() == null)
		{
			AnnotationTreeNode child = tree.getChildren().get(0);
			Annotation cLabel = child.getAnnotation();
			short cSymbol = cLabel.getSymbol();
			short nSubC = g.getNumSubSymbol(cSymbol);
			UnaryRule rule = new UnaryRule(pSymbol, cSymbol);
			rule = g.getRule(rule);
			double[][] count;
			if (!ruleCounter.uRuleCounter.containsKey(rule))
			{
				count = new double[nSubP][nSubC];
				ruleCounter.uRuleCounter.put(rule, count);
			}
			else
			{
				count = ruleCounter.uRuleCounter.get(rule);
			}
			scalingFactor = ScalingTools
					.calcScaleFactor(pLabel.getOuterScale() + cLabel.getInnerScale() - rootLabel.getInnerScale());
			Double[] cInnerS = cLabel.getInnerScores();
			for (short i = 0; i < nSubP; i++)
			{
				double pOS = pOutS[i];
				if (pOS == 0.0)
					continue;
				for (short j = 0; j < nSubC; j++)
				{
					double cIS = cInnerS[j];
					if (cIS == 0.0)
						continue;
					double rS = rule.getScore(i, j);
					if (rS == 0.0)
						continue;
					count[i][j] = count[i][j] + (rS * cIS / rootIS * scalingFactor * pOS);
				}
			}
		}
		else if (tree.isPreterminal())
		{
			PreterminalRule rule = new PreterminalRule(pSymbol, tree.getChildren().get(0).getAnnotation().getWord());
			rule = g.getRule(rule);
			double[] count;
			if (!ruleCounter.preRuleCounter.containsKey(rule))
			{
				count = new double[nSubP];
				ruleCounter.preRuleCounter.put(rule, count);
			}
			else
			{
				count = ruleCounter.preRuleCounter.get(rule);
			}
			scalingFactor = ScalingTools.calcScaleFactor(pLabel.getOuterScale() - root.getAnnotation().getInnerScale());
			double tempCount = 0.0;
			for (short i = 0; i < nSubP; i++)
			{
				double pOS = pOutS[i];
				if (pOS == 0)
					continue;
				double rs = rule.getScore(i);
				if (rs == 0)
					continue;
				tempCount = rs / rootIS * scalingFactor * pOS;
				count[i] = count[i] + tempCount;
				// if (g.isRareWord(rule.getWord()))//这句对效率影响非常大，每次去ArrayList中调用contains（）
				// {
				// if (!ruleCounter.sameTagToUNKCounter.containsKey(pSymbol))
				// {
				// ruleCounter.sameTagToUNKCounter.put(pSymbol, new double[nSubP]);
				// }
				// ruleCounter.sameTagToUNKCounter.get(pSymbol)[i] =
				// ruleCounter.sameTagToUNKCounter.get(pSymbol)[i]
				// + tempCount;
				// }
			}
		}
		else if (tree.getChildren().size() > 2)
			throw new Error("error tree:more than 2 children.");

		for (AnnotationTreeNode child : tree.getChildren())
			refreshRuleCountExpectation(g, root, child);
	}
}
