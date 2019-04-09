package com.lc.nlp4han.clustering;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

// 基于向量空间模型的距离计算
public class DistanceCalculatorVSM extends DistanceCalculator
{
	private Map<String, Integer> vectorInfo = null;

	private double[] getSample(Collection<Feature> features)
	{
		if (vectorInfo == null)
			throw new RuntimeException("未初始化！");
		
		double[] vector = new double[vectorInfo.size()];
		
		for (int i=0 ; i<vector.length ; i++)  // 对vector初始化
		{
			vector[i] = 0;
		}
		
		for (Feature f : features)
		{
			int index = vectorInfo.get(f.getKey());
			vector[index] = f.getValue();
		}
		
		return vector;
	}
	
	@Override
	public void init(FeatureGenerator fg)
	{
		if (vectorInfo == null)
			vectorInfo = new HashMap<String, Integer>();
		Map<String, Count> wordInfo = fg.getTextsInfo();
		Set<Entry<String, Count>> es = wordInfo.entrySet();
		for (Entry<String, Count> e : es)
		{
			vectorInfo.put(e.getKey(), vectorInfo.size());
		}
	}
	
	public void init(Collection<Feature> features)
	{
		if (vectorInfo == null)
			vectorInfo = new HashMap<String, Integer>();
		
		for (Feature f : features)
		{
			vectorInfo.put(f.getKey(), vectorInfo.size());
		}
	}

	@Override
	public double getDistance(Sample s1, Sample s2)
	{
		double[] v1 = getSample(s1.getFeatures());
		double[] v2 = getSample(s2.getFeatures());
		double squareOfResult = 0;
		for (int i=0 ; i<v1.length ; i++)
		{
			squareOfResult += (v1[i]-v2[i]) * (v1[i]-v2[i]);
		}
		return Math.sqrt(squareOfResult);
	}
}
