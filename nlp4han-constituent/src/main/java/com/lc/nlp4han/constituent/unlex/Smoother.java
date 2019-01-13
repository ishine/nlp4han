package com.lc.nlp4han.constituent.unlex;

/**
 * 用于平滑语法中的规则概率，减少过拟合
 * 
 * @author 王宁
 */
abstract class Smoother
{
	public static int smoothType = 0;
	double same;
	double diff;

	public Smoother(double smooth)
	{
		this.diff = smooth;
		this.same = 1.0 - diff;
	}

	public Grammar smooth(Grammar g)
	{
		for (BinaryRule bRule : g.getbRules())
			smoothBRule(bRule, g.getNumSubSymbol(bRule.getParent()), g.getNumSubSymbol(bRule.getLeftChild()),
					g.getNumSubSymbol(bRule.getRightChild()));
		
		for (UnaryRule uRule : g.getuRules())
			smoothURule(uRule, g.getNumSubSymbol(uRule.getParent()), g.getNumSubSymbol(uRule.getChild()));
		
		for (PreterminalRule preRule : g.getLexicon().getPreRules())
			smoothPreRule(preRule, g.getNumSubSymbol(preRule.getParent()));
		
		return g;
	}

	protected abstract BinaryRule smoothBRule(BinaryRule bRule, short nSubSymP, short nSubSymLC, short nSubSymRc);

	protected abstract UnaryRule smoothURule(UnaryRule uRule, short nSubSymP, short nSubSymC);

	protected abstract PreterminalRule smoothPreRule(PreterminalRule preRule, short nSubSymP);

//	public double getSame()
//	{
//		return same;
//	}
//
//	public void setSame(double same)
//	{
//		this.same = same;
//	}
//
//	public double getDiff()
//	{
//		return diff;
//	}
//
//	public void setDiff(double diff)
//	{
//		this.diff = diff;
//	}
}
