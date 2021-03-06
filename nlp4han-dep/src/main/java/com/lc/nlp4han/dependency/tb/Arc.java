package com.lc.nlp4han.dependency.tb;

/**
 * 依存树边-带标记（关系）的有向边
 *
 * 边有两个节点和关系组成
 * 
 */
public class Arc
{
	private String relation;
	private Vertice head;
	private Vertice dependent;

	public Arc(String relation, Vertice head, Vertice dependent)
	{
		this.relation = relation;
		this.head = head;
		this.dependent = dependent;
	}

	public String getRelation()
	{
		return relation;
	}

	public void setRelation(String relation)
	{
		this.relation = relation;
	}

	public Vertice getHead()
	{
		return head;
	}

	public void setHead(Vertice head)
	{
		this.head = head;
	}

	public Vertice getDependent()
	{
		return dependent;
	}

	public void setDependent(Vertice dependent)
	{
		this.dependent = dependent;
	}

	@Override
	public String toString()
	{
		return relation + "(" + head.toString() + ", " + dependent.toString() + ")";
	}

}
