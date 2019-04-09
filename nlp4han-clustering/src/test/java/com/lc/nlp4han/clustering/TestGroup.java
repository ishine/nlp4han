package com.lc.nlp4han.clustering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestGroup
{
	
	@Test
	public void testAddMember()
	{
		Group g1 = new Group();
		Text t = new Text("aa");
		g1.addMember(t);
		
		assertEquals(1, g1.size());
	}
	
	@Test
	public void testRemoveMember()
	{
		Text t1 = new Text("1", "aa");
		Text t2 = new Text("2", "aa");
		Text t3 = new Text("3", "aa");
		List<Text> texts = new ArrayList<Text>();
		texts.add(t1);
		texts.add(t2);
		texts.add(t3);
		
		Group g = new Group(texts);
		
		g.removeMember(t2);
		
		assertTrue(!g.containsText(t2));
		assertTrue(g.containsText(t1));
	}
	
	@Test
	public void testMerge()
	{
		Text t1 = new Text("aa");
		Text t2 = new Text("bb");
		
		Group g1 = new Group();
		g1.addMember(t1);
		
		Group g2 = new Group();
		g2.addMember(t2);
		
		g1.merge(g2);
		
		assertEquals(2, g1.size());
		assertTrue(g1.containsText(t2));
	}
	
	@Test
	public void testGetMembersNames()
	{
		Text t1 = new Text("a", "aa");
		Text t2 = new Text("b", "aa");
		Text t3 = new Text("c", "aa");
		List<Text> texts = new ArrayList<Text>();
		texts.add(t1);
		texts.add(t2);
		texts.add(t3);
		
		Group g = new Group(texts);
		
		List<String> result = g.getMembersNames();
		
		String[] s = new String[] {"a", "b", "c"};
		List<String> expected = Arrays.asList(s);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testClear()
	{
		Text t1 = new Text("a", "aa");
		Text t2 = new Text("b", "aa");
		Text t3 = new Text("c", "aa");
		List<Text> texts = new ArrayList<Text>();
		texts.add(t1);
		texts.add(t2);
		texts.add(t3);
		
		Group g = new Group(texts);
		
		g.clear();
		
		assertEquals(0, g.size());
	}
	
}
