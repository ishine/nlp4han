package com.lc.nlp4han.clustering;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestText
{
	
	@Test
	public void testConstructor_1()
	{
		String name = "1-1";
		String content = "自然语言处理";
		Text t = new Text(name, content);
		
		assertEquals("1-1", t.getName());
		assertEquals("自然语言处理", t.getContent());
	}
	
	@Test
	public void testConstructor_2()
	{
		String content = "自然语言处理";
		
		Text t = new Text(content);
		
		assertEquals("自然语言处理", t.getContent());
	}
	
}
