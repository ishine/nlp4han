package org.nlp4han.coref.sieve;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class TestMentionGenerator
{
	@Test
	public void testGenerate()
	{
		String content = "小明正在打球。他没去上课。";
		Document doc = new Document(content);
		
		MentionGenerator generator = new GrammaticalRoleBasedMentionGenerator(); // 实体提取器，可嵌套多层提取器
		doc = generator.generate(doc); // 对文本提取实体，实体会存储Document类内
		
		List<List<Mention>> ms = doc.getMentionsBySentences();
		
		assertEquals(2, ms.size());
		
		assertEquals("小明", ms.get(0).get(0).getHead());
		assertEquals("他", ms.get(1).get(0).getHead());
	}
}
