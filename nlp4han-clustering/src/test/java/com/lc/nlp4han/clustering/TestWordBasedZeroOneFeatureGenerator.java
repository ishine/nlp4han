package com.lc.nlp4han.clustering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class TestWordBasedZeroOneFeatureGenerator
{
	@Test
	public void testGetFeatures()
	{
		Text t1 = new Text("1-1", "今天去操场打球。");  // name="1-1", content="今天去操场打球。"
		
		FeatureGenerator fg = new WordBasedZeroOneFeatureGenerator();
		
		List<Feature> features1 = fg.getFeatures(t1);
		
		assertTrue(features1.contains(new Feature("今天", 1.0)));
		assertTrue(features1.contains(new Feature("操场", 1.0)));
		assertTrue(features1.contains(new Feature("打球", 1.0)));
		assertEquals(3, features1.size());
		
	}
}
