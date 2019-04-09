package com.lc.nlp4han.constituent.pcfg;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 概率上下文无关文法
 *
 */
public class PCFG extends CFG
{
	public PCFG()
	{

	}
   
	public PCFG(String startSymbol, Set<String> nonTerminalSet, Set<String> terminalSet,HashMap<String,Double> posMap,
			HashMap<String, HashSet<RewriteRule>> ruleMapStartWithlhs,
			HashMap<ArrayList<String>, HashSet<RewriteRule>> ruleMapStartWithrhs)
	{
		super(startSymbol,nonTerminalSet,terminalSet,posMap,ruleMapStartWithlhs,ruleMapStartWithrhs);
	}
	
	public PCFG(String startSymbol, HashMap<String, Double> posMap, Set<String> nonTerminalSet, Set<String> terminalSet,
			Set<RewriteRule> ruleSet)
	{
		super(startSymbol, posMap,nonTerminalSet, terminalSet,ruleSet);
	}
	
	/**
	 * 从文本流中加载CFG文法
	 * 
	 * @param in
	 * @param encoding
	 * @throws IOException
	 */
	public PCFG(InputStream in, String encoding) throws IOException
	{
		super.readGrammar(in, encoding);
	}

	protected RewriteRule readRule(String ruleStr)
	{
		return new PRule(ruleStr);
	}
	
	public double getPosPro(String pos) {
		return super.posProb.get(pos);
	}

	/**
	 * 获取PCFG中所有非终结符扩展出的规则概率之和与1.0的误差，取其中最大的值返回
	 */
	private double getProMaxErrorOfNonTer()
	{
		double MaxErrorOfPCNF = 0;
		for (String string : super.getNonTerminalSet())
		{
			double pro = 0;
			for (RewriteRule rule : getRuleByLHS(string))
			{
				PRule prule = (PRule) rule;
				pro += prule.getProb();
			}

			if (Math.abs(1.0 - pro) > MaxErrorOfPCNF)
			{
				MaxErrorOfPCNF = Math.abs(1.0 - pro);
			}
		}

		return MaxErrorOfPCNF;
	}
}
