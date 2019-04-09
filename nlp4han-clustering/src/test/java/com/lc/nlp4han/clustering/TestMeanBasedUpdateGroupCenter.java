package com.lc.nlp4han.clustering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestMeanBasedUpdateGroupCenter
{
	@Test
	public void testUpdateCenter()
	{
		Feature f1 = new Feature("a", 1.2);
		Feature f2 = new Feature("a", 2.3);
		Feature f3 = new Feature("b", 0.5);
		Feature f4 = new Feature("b", 3.3);
		Feature f5 = new Feature("c", 4.2);
		
		
		Text t1 = new Text("");  // t1的特征为a=1.2  b=0.5
		Sample s1 = new Sample();
		s1.add(f1);
		s1.add(f3);
		t1.setSample(s1);
		
		Text t2 = new Text("");  // t2的特征为a=2.3  b=3.3
		Sample s2 = new Sample();
		s2.add(f2);
		s2.add(f4);
		t2.setSample(s2);
		
		Text t3 = new Text("");  // t3的特征为a=1.2  c=4.2
		Sample s3 = new Sample();
		s3.add(f1);
		s3.add(f5);
		t3.setSample(s3);
		
		Group g = new Group();
		g.addMember(t1);
		g.addMember(t2);
		g.addMember(t3);
		g.setCenter(new Sample());
		
		List<Group> gs = new ArrayList<Group>();
		gs.add(g);
		
		UpdateGroupCenter ugc = new MeanBasedUpdateGroupCenter();
		boolean result = ugc.updateCenter(gs);
		
		assertTrue(result);
		assertEquals(1.5667, g.getCenter().getValue("a"), 0.0001);
		assertEquals(1.2667, g.getCenter().getValue("b"), 0.0001);
		assertEquals(1.4, g.getCenter().getValue("c"), 0.0001);
	}
}
