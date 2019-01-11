package com.lc.nlp4han.constituent.unlex;

import java.util.HashMap;

/**
 * 记录规则的期望次数
 * 
 * @author 王宁
 */
public class RuleCounter
{
	protected HashMap<BinaryRule, double[][][]> bRuleCounter;
	protected HashMap<UnaryRule, double[][]> uRuleCounter;
	protected HashMap<PreterminalRule, double[]> preRuleCounter;
	
	protected HashMap<Short, double[]> sameParentRulesCounter;// <parent,[ParentSubIndex,sum]>
	protected HashMap<Short, double[]> sameTagToUNKCounter;// 记录tag_i-->UNK 的期望

	public RuleCounter()
	{
		bRuleCounter = new HashMap<>();
		uRuleCounter = new HashMap<>();
		preRuleCounter = new HashMap<>();
		
		sameParentRulesCounter = new HashMap<>();
		sameTagToUNKCounter = new HashMap<Short, double[]>();
	}

	// 另一种计算方式
	// public void calSameParentRulesExpectation(ArrayList<Short> nSubSymbolArr)
	// {
	// for (Map.Entry<BinaryRule, double[][][]> entry : bRuleCounter.entrySet())
	// {
	// BinaryRule bRule = entry.getKey();
	// double[][][] ruleCount = entry.getValue();
	// double[] samePCount = new double[nSubSymbolArr.get(bRule.getParent())];
	// for (int pSubSymbol = 0; pSubSymbol < samePCount.length; pSubSymbol++)
	// {
	// double tempCount = 0.0;
	// for (double[] countArr : ruleCount[pSubSymbol])
	// {
	// for (double subRuleCount : countArr)
	// {
	// tempCount += subRuleCount;
	// }
	// }
	// samePCount[pSubSymbol] += tempCount;
	// }
	// calSameParentRulesExpectationHelper(bRule.getParent(), samePCount);
	// }
	// for (Map.Entry<UnaryRule, double[][]> entry : uRuleCounter.entrySet())
	// {
	// UnaryRule uRule = entry.getKey();
	// double[][] ruleCount = entry.getValue();
	// double[] samePCount = new double[nSubSymbolArr.get(uRule.getParent())];
	// for (int pSubSymbol = 0; pSubSymbol < samePCount.length; pSubSymbol++)
	// {
	// double tempCount = 0.0;
	// for (double subRuleCount : ruleCount[pSubSymbol])
	// {
	// tempCount += subRuleCount;
	// }
	// samePCount[pSubSymbol] += tempCount;
	// }
	// calSameParentRulesExpectationHelper(uRule.getParent(), samePCount);
	// }
	// for (Map.Entry<PreterminalRule, double[]> entry : preRuleCounter.entrySet())
	// {
	// PreterminalRule preRule = entry.getKey();
	// double[] ruleCount = entry.getValue();
	// double[] samePCount = new double[nSubSymbolArr.get(preRule.getParent())];
	// for (int pSubSymbol = 0; pSubSymbol < ruleCount.length; pSubSymbol++)
	// {
	// samePCount[pSubSymbol] = ruleCount[pSubSymbol];
	// }
	// calSameParentRulesExpectationHelper(preRule.getParent(), samePCount);
	// }
	// }
	//
	// private void calSameParentRulesExpectationHelper(short parent, double[]
	// samePCount)
	// {
	// sameParentRulesCounter.merge(parent, samePCount, (oldCount, newCount) ->
	// {
	// if (newCount.length != oldCount.length)
	// throw new Error("传递参数有错误。");
	// for (int i = 0; i < newCount.length; i++)
	// {
	// oldCount[i] += newCount[i];
	// }
	// return oldCount;
	// });
	// }

	public void calcSameParentRulesExpectation(Grammar g)
	{
		for (short pSymbol = 0; pSymbol < g.getNumSymbol(); pSymbol++)
		{
			double[] count = new double[g.getNumSubSymbol((short) pSymbol)];
			if (g.getbRuleSetBySameHead(pSymbol) != null)
			{
				for (BinaryRule bRule : g.getbRuleSetBySameHead(pSymbol))
				{
					double[][][] ruleCount = bRuleCounter.get(bRule);
					for (int pSubSymbol = 0; pSubSymbol < count.length; pSubSymbol++)
					{
						double tempCount = 0.0;
						for (double[] countArr : ruleCount[pSubSymbol])
						{
							for (double subRuleCount : countArr)
								tempCount += subRuleCount;
						}
						count[pSubSymbol] += tempCount;
					}
				}
			}
			
			if (g.getuRuleSetBySameHead(pSymbol) != null)
			{
				for (UnaryRule uRule : g.getuRuleSetBySameHead(pSymbol))
				{
					double[][] ruleCount = uRuleCounter.get(uRule);
					for (int pSubSymbol = 0; pSubSymbol < count.length; pSubSymbol++)
					{
						double tempCount = 0.0;
						for (double subRuleCount : ruleCount[pSubSymbol])
							tempCount += subRuleCount;

						count[pSubSymbol] += tempCount;
					}
				}
			}
			
			if (g.getPreRuleSetBySameHead(pSymbol) != null)
			{
				for (PreterminalRule preRule : g.getPreRuleSetBySameHead(pSymbol))
				{
					double[] ruleCount = preRuleCounter.get(preRule);
					for (int pSubSymbol = 0; pSubSymbol < count.length; pSubSymbol++)
						count[pSubSymbol] += ruleCount[pSubSymbol];
				}
			}
			
			sameParentRulesCounter.put(pSymbol, count);
		}
	}

}
