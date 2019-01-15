package org.nlp4han.coref.sieve;

import java.util.List;

public class MentionGeneratorWrapper implements MentionGenerator
{
	protected MentionGenerator generator;
	
	@Override
	public Document generate(Document doc)
	{
		return doc;
	}
	
	public void addMentions(Document doc, List<List<Mention>> mentions) 
	{
		if (doc.getMentionsBySentences() != null && doc.getMentionsBySentences().size() != mentions.size())
		{
			throw new RuntimeException("参数错误！");
		}
		
		if (doc.getMentionsBySentences() == null)
		{
			doc.setMentions(mentions);
		}
		else
		{
			List<List<Mention>> ms = doc.getMentionsBySentences();
			
			for (int i=0 ; i<ms.size() ; i++)
			{
				List<Mention> temp = ms.get(i);
				
				for (Mention m : mentions.get(i))
				{
					if (!temp.contains(m))
					{
						temp.add(m);
					}
				}
			}
		}

	}
}
