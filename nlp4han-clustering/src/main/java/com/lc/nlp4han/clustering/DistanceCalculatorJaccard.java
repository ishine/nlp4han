package com.lc.nlp4han.clustering;

import java.util.Iterator;
import java.util.Set;

// 基于Jaccard系数的距离计算
public class DistanceCalculatorJaccard extends DistanceCalculator
{
	@Override
	public double getDistance(Sample s1, Sample s2)
	{
		int m = 0;  // 交集数量
		int n = 0;  // 并集数量
		Set<String> keys = s1.getKeySet();
		
		Iterator<String> it = keys.iterator();
		while (it.hasNext())
		{
			String key = it.next();
			if (s2.containsKey(key))
				m++;
			else
				n++;
		}
		n += s2.size();
		return 1 - m*1.0/n;  // 距离 = 1 - Jaccard系数
	}

}
