package com.lc.nlp4han.constituent.pcfg;

import java.util.ArrayList;
import java.util.List;

import com.lc.nlp4han.constituent.TreeNode;

/**
 * CFG文法规则
 *
 */
public class RewriteRule
{
	private String lhs;// 规则左部
	private ArrayList<String> rhs = new ArrayList<String>();// 规则右部

	/**
	 * 初始化RewriteRule
	 * 
	 * @param lhs
	 *            规则左侧字符串
	 * @param list
	 *            规则右侧字符串列表
	 */
	public RewriteRule(String lhs, ArrayList<String> list)
	{
		this.lhs = lhs;
		for (String type : list)
		{
			this.rhs.add(type);
		}
	}

	/**
	 * 初始化RewriteRule
	 * 
	 * @param args
	 *            规则左侧和右侧string
	 */
	public RewriteRule(String... args)
	{
		this.lhs = args[0];
		for (int i = 1; i < args.length; i++)
		{
			this.rhs.add(args[i]);
		}
	}

	/**
	 * 由规则字符串构造规则
	 * 
	 * @param ruleStr
	 *            规则的字符串形式
	 */
	public RewriteRule(String ruleStr)
	{
		String[] strArray = ruleStr.split("->");
		lhs = strArray[0];
		rhs = new ArrayList<String>();
		for (String string : strArray[1].split(" "))
		{
			rhs.add(string);
		}
	}

	public static RewriteRule getRewriteRule(String ruleStr)
	{
		return new RewriteRule(ruleStr);
	}

	/**
	 * 由树结构中的节点值和子节点初始化RewriteRule
	 * 
	 * @param lhs
	 * @param children
	 */
	public RewriteRule(String lhs, List<? extends TreeNode> children)
	{
		this.lhs = lhs;
		for (TreeNode node : children)
		{
			this.rhs.add(node.getNodeName());
		}
	}

	public void setLHS(String lhs)
	{
		this.lhs = lhs;
	}

	public String getLHS()
	{
		return lhs;
	}

	public ArrayList<String> getRHS()
	{
		return rhs;
	}

	public void setRHS(ArrayList<String> rhs)
	{
		this.rhs = rhs;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lhs == null) ? 0 : lhs.hashCode());
		result = prime * result + ((rhs == null) ? 0 : rhs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		RewriteRule other = (RewriteRule) obj;
		if (lhs == null)
		{
			if (other.lhs != null)
				return false;
		}
		else if (!lhs.equals(other.lhs))
			return false;
		
		if (rhs == null)
		{
			if (other.rhs != null)
				return false;
		}
		else if (!rhs.equals(other.rhs))
			return false;
		
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder strb = new StringBuilder();
		strb.append(lhs + "->");
		for (String st : rhs)
		{
			strb.append(st);
			strb.append(" ");
		}
		return strb.toString().trim();
	}
}
