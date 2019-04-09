package com.lc.nlp4han.clustering;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestSpaceVectorBasedDistanceCalculator
{
	@Test
	public void testDistance_1()
	{
		Text t1 = new Text("今天操场打球");
		Text t2 = new Text("天气晴朗");
		
		List<Text> ts = new ArrayList<Text>();
		ts.add(t1);
		ts.add(t2);
		
		List<Feature> fs1 = new ArrayList<Feature>();
		fs1.add(new Feature("今天", 1));
		fs1.add(new Feature("操场", 1));
		fs1.add(new Feature("打球", 1));
		Sample s1 = new Sample(fs1);
		
		List<Feature> fs2 = new ArrayList<Feature>();
		fs2.add(new Feature("天气", 1));
		fs2.add(new Feature("晴朗", 1));
		Sample s2 = new Sample(fs2);
		
		t1.setSample(s1);
		t2.setSample(s2);
		
		FeatureGenerator fg = new WordBasedZeroOneFeatureGenerator();
		fg.init(ts);
		
		DistanceCalculator dis = new DistanceCalculatorVSM();
		dis.init(fg);
		double d = dis.getDistance(s1, s2);
		
		assertEquals(2.236, d, 0.001);
	}
	
	@Test
	public void testDistance_2()
	{
		Text t1 = new Text("今天操场打球");
		Text t2 = new Text("今天天气晴朗");
		
		List<Text> ts = new ArrayList<Text>();
		ts.add(t1);
		ts.add(t2);
		
		List<Feature> fs1 = new ArrayList<Feature>();
		fs1.add(new Feature("今天", 1));
		fs1.add(new Feature("操场", 1));
		fs1.add(new Feature("打球", 1));
		Sample s1 = new Sample(fs1);
		
		List<Feature> fs2 = new ArrayList<Feature>();
		fs2.add(new Feature("今天", 1));
		fs2.add(new Feature("天气", 1));
		fs2.add(new Feature("晴朗", 1));
		Sample s2 = new Sample(fs2);
		
		t1.setSample(s1);
		t2.setSample(s2);
		
		FeatureGenerator fg = new WordBasedZeroOneFeatureGenerator();
		fg.init(ts);
		
		DistanceCalculator dis = new DistanceCalculatorVSM();
		dis.init(fg);
		double d = dis.getDistance(s1, s2);
		
		assertEquals(2.0, d, 0.001);
	}
}
