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
