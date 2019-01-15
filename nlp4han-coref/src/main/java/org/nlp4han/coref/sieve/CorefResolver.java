package org.nlp4han.coref.sieve;

import java.util.List;

public class CorefResolver
{
	private List<Sieve> sieves;
	private MentionGenerator extractor;

	public CorefResolver(List<Sieve> sieves)
	{
		this.sieves = sieves;
	}
	
	public List<CorefCluster> coreferent(Document doc)
	{
		MentionGenerator generator = new GrammaticalRoleBasedMentionGenerator(); // 实体提取器，可嵌套多层提取器
		doc = generator.generate(doc); // 对文本提取实体，实体会存储Document类内
		return null;
	}
}
