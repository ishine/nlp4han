package com.lc.nlp4han.clustering;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestJaccardCoefficientBasedDistanceCalculator
{
	@Test
	public void testGetDistance_1()
	{
		List<Feature> fs1 = new ArrayList<Feature>();
		fs1.add(new Feature("今天", 1));
		fs1.add(new Feature("操场", 1));
		fs1.add(new Feature("打球", 1));
		Sample s1 = new Sample(fs1);
		
		List<Feature> fs2 = new ArrayList<Feature>();
		fs2.add(new Feature("今天", 1));
		fs2.add(new Feature("天气", 1));
		fs2.add(new Feature("真好", 1));
		Sample s2 = new Sample(fs2);
		
		
		DistanceCalculator dis = new DistanceCalculatorJaccard();
		double d = dis.getDistance(s1, s2);  // 距离 = 1-Jaccard系数
		
		assertEquals(0.8, d, 0.001);
	}
	
}
