package org.nlp4han.coref.sieve;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestCorefResolver
{
	@Test
	public void testCoreferent()
	{
		String content = "小明正在打球。他没去上课。";
		Document doc = new Document(content); // 待消解的文本
		
		List<Sieve> sieves = new ArrayList<Sieve>(); // 多层筛子
		sieves.add(new ExactStringMatchSieve());
		
		CorefResolver cr = new CorefResolver(sieves);
		
		List<CorefCluster> ccs = cr.coreferent(doc); // 共指消解
		
		assertEquals(1, ccs.size());
		assertEquals(2, ccs.get(0).size());
		assertEquals("小明", ccs.get(0).getFirstMention().getHead());
		assertEquals("他", ccs.get(0).getMember(1).getHead());
	}
}
