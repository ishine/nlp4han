package com.lc.nlp4han.constituent.unlex;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 记录二叉化后得到的树中的标记（非终结符）和与之对应整数
 * 
 * @author 王宁
 * 
 */
public class NonterminalTable
{
	private HashMap<String, Short> str_intMap;// "ROOT" - 0
	private HashMap<Short, String> int_strMap;
	
	private short numInitialSymbol;// 二叉化后得到的所有非终结符个数
	
	private ArrayList<Short> intValueOfPreterminalArr;
	private ArrayList<Short> numSubsymbolArr;

	public NonterminalTable()
	{
		str_intMap = new HashMap<String, Short>();
		int_strMap = new HashMap<Short, String>();
		numInitialSymbol = 0;
		intValueOfPreterminalArr = new ArrayList<Short>();
		numSubsymbolArr = new ArrayList<Short>();
	}

	public boolean hasSymbol(String symbol)
	{
		if (str_intMap.containsKey(symbol))
			return true;
		else
			return false;
	}

	public void addToPreterminalArr(String preterminalSymbol)
	{
		short intValue = str_intMap.get(preterminalSymbol);
		addToPreterminalArr(intValue);
	}

	public void addToPreterminalArr(short preterminalSymbol)
	{
		intValueOfPreterminalArr.add(preterminalSymbol);
	}

	public boolean hasPreterminalSymbol(short preterminalSymbol)
	{
		return intValueOfPreterminalArr.contains(preterminalSymbol);
	}

	
	/**
	 * 
	 * @param symbol
	 *            要添加的符号对应的short值
	 * @return 返回新symbol对应的整数，返回-1表示已有symbol/short对
	 */
	public short putSymbol(String symbol)
	{
		if (hasSymbol(symbol))
			return -1;
		str_intMap.put(symbol, numInitialSymbol);
		int_strMap.put(numInitialSymbol, symbol);
		numSubsymbolArr.add((short) 1);
		numInitialSymbol++;
		return (short) (numInitialSymbol - 1);
	}

	public short intValue(String symbol)
	{
		return str_intMap.get(symbol);
	}

	/**
	 * @param symbol
	 * @return 该符号对应的String
	 */
	public String stringValue(short symbol)
	{
		return int_strMap.get(symbol);
	}

	/**
	 * 
	 * @param symbol
	 * @return 返回该symbol分裂的个数
	 */
	public short numSubsymbol(String symbol)
	{
		if (hasSymbol(symbol))
		{
			short intVlaue = str_intMap.get(symbol);
			return numSubsymbolArr.get(intVlaue);
		}
		else
		{
			return -1;
		}
	}

	public short getNumSymbol()
	{
		return this.numInitialSymbol;
	}

	public ArrayList<Short> getIntValueOfPreterminalArr()
	{
		return intValueOfPreterminalArr;
	}

	public void setIntValueOfPreterminalArr(ArrayList<Short> intValueOfPreterminalArr)
	{
		this.intValueOfPreterminalArr = intValueOfPreterminalArr;
	}

	public ArrayList<Short> getNumSubsymbolArr()
	{
		return numSubsymbolArr;
	}

	public void setNumSubsymbolArr(ArrayList<Short> numSubsymbolArr)
	{
		this.numSubsymbolArr = numSubsymbolArr;
	}

	public void setNumInitialSymbol(short numInitialSymbol)
	{
		this.numInitialSymbol = numInitialSymbol;
	}

	public HashMap<String, Short> getStr_intMap()
	{
		return str_intMap;
	}

	public HashMap<Short, String> getInt_strMap()
	{
		return int_strMap;
	}
}
