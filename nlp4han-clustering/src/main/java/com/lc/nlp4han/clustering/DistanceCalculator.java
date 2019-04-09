package com.lc.nlp4han.clustering;

import java.util.Collection;

public abstract class DistanceCalculator
{
	public void init(FeatureGenerator fg)
	{
	}
	
	public void init(Collection<Feature> fg)
	{	
	}
	
	public abstract double getDistance(Sample s1, Sample s2);
	
	
	public double getDistance(Text t1, Text t2)
	{
		if (t1 == null || t2 == null || t1.getSample()==null || t2.getSample()==null)
		{
			throw new RuntimeException("参数错误！");
		}
		
		Sample s1 = t1.getSample();
		Sample s2 = t2.getSample();
		
		return getDistance(s1, s2);
	}
	
	public double getDistance(Text t1, Group g1) 
	{
		if (t1 == null || g1 == null || t1.getSample()==null || g1.getCenter()==null)
		{
			throw new RuntimeException("参数错误！");
		}
		
		Sample s2 = g1.getCenter();
		return getDistance(t1.getSample(), s2);
	}
	
	public double getDistance(Group g1, Group g2)
	{
		if (g1==null || g2==null || g1.getCenter()==null || g2.getCenter()==null || g1.getMembers().size()<1 || g2.getMembers().size()<1)
		{
			throw new RuntimeException("参数错误！");
		}
		
		Sample s1 = g1.getCenter();
		Sample s2 = g2.getCenter();
		return getDistance(s1, s2);
	}
}
