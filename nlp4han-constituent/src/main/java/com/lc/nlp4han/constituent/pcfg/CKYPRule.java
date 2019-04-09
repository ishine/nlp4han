package com.lc.nlp4han.constituent.pcfg;

import java.util.ArrayList;

public class CKYPRule extends PRule 
{
	private int k;// 记录CKY算法中table表中的某节点的分裂位置
	private int i;// 记录CKY算法中table表中的某节点的分裂的第一个rhs中对应非终结符的ArrayList的位置
	private int j;// 记录CKY算法中table表中的某节点的分裂的第二个rhs中对应非终结符的ArrayList的位置

	public CKYPRule()
	{
		super();
	}

	public CKYPRule(double pro, String lhs, ArrayList<String> rhs, int k, int i, int j)
	{
		super(pro, lhs, rhs);
		this.k = k;
		this.i = i;
		this.j = j;
	}

	public CKYPRule(double pro, String lhs, String rhs, int k, int i, int j)
	{
		super(pro, lhs, rhs);
		this.k = k;
		this.i = i;
		this.j = j;
	}

	public CKYPRule(PRule prule, int k, int i, int j)
	{
		super.setLHS(prule.getLHS());
		super.setRHS(prule.getRHS());
		super.setProb(prule.getProb());
		this.k = k;
		this.i = i;
		this.j = j;
	}

	public int getK()
	{
		return k;
	}

	public void setK(int k)
	{
		this.k = k;
	}

	public int getI()
	{
		return i;
	}

	public void setI(int i)
	{
		this.i = i;
	}

	public int getJ()
	{
		return j;
	}

	public void setJ(int j)
	{
		this.j = j;
	}

	@Override
	public String toString()
	{
		return super.toString()+ " k=" + k + " i=" + i + " j=" + j;
	}

}
