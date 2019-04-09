package com.lc.nlp4han.constituent.unlex;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 * 规则基类
 * 
 * @author 王宁
 * 
 */
public abstract class Rule
{
	public static double ruleThres = 1.0e-30;// 小于该值均赋值为0.0
	public static double preRulethres = 1.0e-87;
	protected short parent;

	public abstract void split();

	public abstract void merge(Short[][] symbolToMerge, double[][] weights);

	public void write(BufferedWriter writer, Grammar g) throws IOException
	{
		for (String rule : toStringRules(g))
		{
			writer.write(rule + "\r");
		}
	}

	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + parent;
		return result;
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rule other = (Rule) obj;
		if (parent != other.parent)
			return false;
		return true;
	}

	public abstract String[] toStringRules(Grammar g);

//	public abstract String toStringRule(NonterminalTable nonterminalTable, short... labels);

	public short getParent()
	{
		return parent;
	}

	public void setParent(short parent)
	{
		this.parent = parent;
	}

	abstract boolean withIn(HashSet<? extends Rule> rules);
}
