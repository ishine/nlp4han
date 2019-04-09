package com.lc.nlp4han.constituent.pcfg;

import java.util.ArrayList;

/**
 * PCFG文法规则
 *
 */
public class PRule extends RewriteRule implements Comparable<PRule>
{
	private double prob;

	public PRule()
	{
		super();
	}

	/**
	 * 初始化PRule
	 * 
	 * @param 规则的概率，以及终结符和非终结符的字符串形式
	 */
	public PRule(double pro, String... args)
	{
		super(args);
		this.prob = pro;
	}

	/**
	 * 由规则字符串构造规则
	 * 
	 * @param ruleStr
	 *            规则的字符串形式
	 */
	public PRule(String ruleStr)
	{
		super(ruleStr.split(" ---- ")[0]);
		prob = Double.parseDouble(ruleStr.split(" ---- ")[1]);
	}

	/**
	 * 初始化PRule
	 * 
	 * @param pro
	 * @param lhs
	 * @param rhs
	 */
	public PRule(double pro, String lhs, ArrayList<String> rhs)
	{
		super(lhs, rhs);
		this.prob = pro;
	}

	/**
	 * 初始化PRule
	 * 
	 * @param rule
	 * @param pro
	 */
	public PRule(RewriteRule rule, double pro)
	{
		super(rule.getLHS(), rule.getRHS());
		this.prob = pro;
	}

	/**
	 * 得到该规则的概率
	 * 
	 * @return proOfRule
	 */
	public double getProb()
	{
		return prob;
	}

	/**
	 * 设置该规则的概率
	 * 
	 * @param proOfRule
	 */
	public void setProb(double proOfRule)
	{
		this.prob = proOfRule;
	}

	@Override
	public String toString()
	{
		StringBuilder strb = new StringBuilder();
		strb.append(super.toString());
		strb.append(" ---- " + prob);
		return strb.toString();
	}

	@Override
	public int compareTo(PRule o)
	{// 排序由大到小
		if (prob < o.getProb())
		{
			return 1;
		}
		
		if (prob > o.getProb())
		{
			return -1;
		}
		
		return 0;
	}
}
