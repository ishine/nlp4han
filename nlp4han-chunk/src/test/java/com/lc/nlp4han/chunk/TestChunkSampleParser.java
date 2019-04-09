package com.lc.nlp4han.chunk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosParserBIEO;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosParserBIO;

public class TestChunkSampleParser
{
	@Test
	public void testParseBIO()
	{
		ChunkerWordPosParserBIO cwppBIO = new ChunkerWordPosParserBIO();
		
		AbstractChunkAnalysisSample acas = cwppBIO.parse("[上海/NR 浦东/NR]NP [开发/NN 与/CC 法制/NN 建设/NN]NP [同步/VV]VP");
		
		String[] tokens = acas.getTokens();
		String[] tags = acas.getTags();
		Object[] Context = acas.getAditionalContext();
		
		assertEquals(7, tokens.length);
		assertEquals(7, tags.length);
		assertEquals(7, Context.length);
		
		boolean flag = false;
		for (String t : tokens)
		{
			if ("同步".equals(t))
				flag = true;
		}
		
		assertTrue(flag);
		
		flag = false;
		for (String t : tags)
		{
			if ("VP_B".equals(t))
				flag = true;
		}
		
		assertTrue(flag);
	}
	
	@Test
	public void testParseBIEO()
	{
		ChunkerWordPosParserBIEO cwppBIO = new ChunkerWordPosParserBIEO();
		
		AbstractChunkAnalysisSample acas = cwppBIO.parse("[上海/NR 浦东/NR]NP [开发/NN 与/CC 法制/NN 建设/NN]NP [同步/VV]VP");
		
		String[] tokens = acas.getTokens();
		String[] tags = acas.getTags();
		Object[] Context = acas.getAditionalContext();
		
		assertEquals(7, tokens.length);
		assertEquals(7, tags.length);
		assertEquals(7, Context.length);
		
		boolean flag = false;
		for (String t : tokens)
		{
			if ("同步".equals(t))
				flag = true;
		}
		
		assertTrue(flag);
		
		flag = false;
		for (String t : tags)
		{
			if ("VP_B".equals(t))
				flag = true;
		}
		
		assertTrue(flag);
	}
	
}
