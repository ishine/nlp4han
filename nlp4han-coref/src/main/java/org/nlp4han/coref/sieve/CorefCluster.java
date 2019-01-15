package org.nlp4han.coref.sieve;

import java.util.List;

import org.nlp4han.coref.hobbs.Attribute;

public class CorefCluster
{
	private List<Mention> members;
	private Attribute attribute;
	private Mention firstMention;
	
	public void add(Mention mention)
	{
		
	}
	
	public boolean contains(Mention mention)
	{
		return false;
	}
	
	public Mention remove(Mention mention)
	{
		return null;
	}
	
	public List<Mention> getMembers()
	{
		return members;
	}

	public int size()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public Mention getFirstMention()
	{
		// TODO Auto-generated method stub
		return firstMention;
	}

	public Mention getMember(int index)
	{
		// TODO Auto-generated method stub
		return members.get(index);
	}
	
	public void merge(CorefCluster cluster)
	{
		
	}
}
