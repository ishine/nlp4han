package com.lc.nlp4han.constituent.lex;

import java.util.HashMap;

/**
 * 解析线图中的项
 *
 */
public class ChartEntry
{
	private boolean flag;
	private HashMap<Edge, Double> edgeMap;

	public ChartEntry(boolean flag, HashMap<Edge, Double> edgeMap)
	{
		this.flag = flag;
		this.edgeMap = edgeMap;
	}

	public ChartEntry()
	{
	}

	public boolean isFlag()
	{
		return flag;
	}

	public void setFlag(boolean flag)
	{
		this.flag = flag;
	}

	public HashMap<Edge, Double> getEdgeMap()
	{
		return edgeMap;
	}

	public void setEdgeMap(HashMap<Edge, Double> edgeMap)
	{
		this.edgeMap = edgeMap;
	}
}
