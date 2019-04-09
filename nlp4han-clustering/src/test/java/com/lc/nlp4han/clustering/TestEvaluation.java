package com.lc.nlp4han.clustering;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TestEvaluation
{
	
	@Test
	public void testGetExpectedResult()
	{
		Text t1 = new Text("1-1", "aa");  // name="1-1", content="aa"
		Text t2 = new Text("1-2", "aa");
		Text t3 = new Text("2-1", "aa");
		Text t4 = new Text("2-2", "aa");
		Text t5 = new Text("3-1", "aa");
		List<Text> texts = new ArrayList<Text>();
		texts.add(t1);
		texts.add(t2);
		texts.add(t3);
		texts.add(t4);
		texts.add(t5);
		
		Evaluation e = new Evaluation();
		e.init(texts);  // 初始化，根据Text中的name将文本分类成期望的结果
		List<Set<Text>> actual = e.getExpectedResult();
		
		
		Set<Text> expected1 = new HashSet<Text>();
		expected1.add(t1);
		expected1.add(t2);
		Set<Text> expected2 = new HashSet<Text>();
		expected2.add(t3);
		expected2.add(t4);
		Set<Text> expected3 = new HashSet<Text>();
		expected3.add(t5);
		
		List<Set<Text>> expected = new ArrayList<Set<Text>>();
		expected.add(expected1);
		expected.add(expected2);
		expected.add(expected3);
		
		assertEquals(expected, actual);
	}
	
	
	@Test
	public void testPurity_RI_FMeasures_NMI()
	{
		List<Text> texts = new ArrayList<Text>();
		Text t1_1 = new Text("1-1", "xxxxx");
		texts.add(t1_1);
		Text t1_2 = new Text("1-2", "xxxxx");
		texts.add(t1_2);
		Text t1_3 = new Text("1-3", "xxxxx");
		texts.add(t1_3);
		Text t1_4 = new Text("1-4", "xxxxx");
		texts.add(t1_4);
		Text t1_5 = new Text("1-5", "xxxxx");
		texts.add(t1_5);
		Text t1_6 = new Text("1-6", "xxxxx");
		texts.add(t1_6);
		Text t1_7 = new Text("1-7", "xxxxx");
		texts.add(t1_7);
		Text t1_8 = new Text("1-8", "xxxxx");
		texts.add(t1_8);
		Text t2_1 = new Text("2-1", "xxxxx");
		texts.add(t2_1);
		Text t2_2 = new Text("2-2", "xxxxx");
		texts.add(t2_2);
		Text t2_3 = new Text("2-3", "xxxxx");
		texts.add(t2_3);
		Text t2_4 = new Text("2-4", "xxxxx");
		texts.add(t2_4);
		Text t2_5 = new Text("2-5", "xxxxx");
		texts.add(t2_5);
		Text t3_1 = new Text("3-1", "xxxxx");
		texts.add(t3_1);
		Text t3_2 = new Text("3-2", "xxxxx");
		texts.add(t3_2);
		Text t3_3 = new Text("3-3", "xxxxx");
		texts.add(t3_3);
		Text t3_4 = new Text("3-4", "xxxxx");
		texts.add(t3_4);
		
		
		List<Group> actualResult = new ArrayList<Group>();
		Group g1 = new Group();  // 5个"1"类，1个"2"类
		g1.addMember(t1_1);
		g1.addMember(t1_2);
		g1.addMember(t1_3);
		g1.addMember(t1_4);
		g1.addMember(t1_5);
		g1.addMember(t2_1);
		
		Group g2 = new Group();  // 1个"1"类，4个"2"类，1个"3"类
		g2.addMember(t1_6);
		g2.addMember(t2_2);
		g2.addMember(t2_3);
		g2.addMember(t2_4);
		g2.addMember(t2_5);
		g2.addMember(t3_1);
		
		Group g3 = new Group();  // 2个"1"类，3个"3"类
		g3.addMember(t1_7);
		g3.addMember(t1_8);
		g3.addMember(t3_2);
		g3.addMember(t3_3);
		g3.addMember(t3_4);
		
		actualResult.add(g1);
		actualResult.add(g2);
		actualResult.add(g3);
		
		Evaluation eval = new Evaluation(texts, actualResult);
		
		double purity = eval.purity();
		assertEquals("0.71", String.format("%.2f", purity));
		
		double ri = eval.RI();
		assertEquals("0.68", String.format("%.2f", ri));
		
		double f5 = eval.FMeasure(5);
		assertEquals("0.46", String.format("%.2f", f5));
		
		double nmi = eval.NMI();
		assertEquals("0.36", String.format("%.2f", nmi));
		
	}
	
}
