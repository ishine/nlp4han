package com.lc.nlp4han.clustering;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestWordBasedTF_IDFFeatureGenerator
{
	List<Text> texts = new ArrayList<Text>();
	@Before
	public void before()
	{
		Text t1 = new Text("1-1", "今天去操场打球。");  // name="1-1", content="今天去操场打球。"
		Text t2 = new Text("1-2", "今天在操场散步。");
		Text t3 = new Text("2-1", "小明正在打球。");
		Text t4 = new Text("2-2", "小明正在吃饭。");
		texts.add(t1);
		texts.add(t2);
		texts.add(t3);
		texts.add(t4);
	}
	
	@Test
	public void testGetFeatures()
	{
		FeatureGenerator fg = new WordBasedTF_IDFFeatureGenerator();
		
		if (!fg.isInitialized())
			fg.init(texts);
		List<Feature> features = fg.getFeatures(texts.get(1));
		
		for (Feature f : features)
		{
			if (f.getKey().equals("今天"))
				assertEquals(0.0959, f.getValue(), 0.0001);
			if (f.getKey().equals("操场"))
				assertEquals(0.0959, f.getValue(), 0.0001);
			if (f.getKey().equals("散步"))
				assertEquals(0.2310, f.getValue(), 0.0001);
		}
		
		assertEquals(3, features.size());
	}
}
