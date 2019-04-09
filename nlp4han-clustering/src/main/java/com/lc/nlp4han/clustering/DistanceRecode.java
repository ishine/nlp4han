package com.lc.nlp4han.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * 记录两两文本间的距离
 * @author 杨智超
 *
 */
public class DistanceRecode
{
	private List<List<Double>> doubleDimensionalArray = null;  // 二维数组，记录每两个Text间的距离
	private Map<Text, Integer> allTexts = null;  //  记录所有的Text，每个Text对应的vaule为doubleDimensionalArray中的索引号
	
	public DistanceRecode(List<Text> texts, DistanceCalculator dc)
	{
		doubleDimensionalArray = new ArrayList<List<Double>>();
		allTexts = new HashMap<Text, Integer>(); 
		
		for (int i=0 ; i<texts.size() ; i++)  // allTexts中存入所有Text
		{
			allTexts.put(texts.get(i), allTexts.size());
		}
		
		for (int i=0 ; i<allTexts.size() ; i++)  // 初始化doubleDimensionalArray
		{
			List<Double> tmp = new ArrayList<Double>();
			for (int j=0 ; j<=i ; j++)
			{
				tmp.add(0.0);
			}
			doubleDimensionalArray.add(tmp);
		}
		
		Set<Entry<Text, Integer>> ts = allTexts.entrySet();
		Iterator<Entry<Text, Integer>> it1 = ts.iterator();
		
		while (it1.hasNext())
		{
			Entry<Text, Integer> e1 = it1.next();
			
			Iterator<Entry<Text, Integer>> it2 = ts.iterator();
			while (it2.hasNext())
			{
				Entry<Text, Integer> e2 = it2.next();
				
				if (e1.getValue()-e2.getValue() > 0)
				{
					double distance = dc.getDistance(e1.getKey(), e2.getKey());
					doubleDimensionalArray.get(e1.getValue()).set(e2.getValue(), distance);
				}
			}
		}
	}
	
	public double getDistance(Text t1, Text t2)
	{
		Integer index1 = allTexts.get(t1);
		Integer index2 = allTexts.get(t2);
		
		if (index1 == null || index2 == null || index1 >= allTexts.size() || index2 >= allTexts.size())
		{
			throw new RuntimeException("参数错误！");
		}
		
		if (index1 == index2)
			return 0;
		else if (index1 > index2)
			return doubleDimensionalArray.get(index1).get(index2);
		else
			return doubleDimensionalArray.get(index2).get(index1);
	}
	
	public Set<Text> textSet()
	{
		return allTexts.keySet();
	}
}
