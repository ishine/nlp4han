package org.nlp4han.coref.sieve;

import org.nlp4han.coref.hobbs.Attribute;

public class Entity
{
	private int entityID;
	private String head;
	private int headIndex;
	private int sentenceIndex;
	
	private Attribute attribute;

	public int getEntityID()
	{
		return entityID;
	}

	public void setEntityID(int entityID)
	{
		this.entityID = entityID;
	}

	public String getHead()
	{
		return head;
	}

	public void setHead(String head)
	{
		this.head = head;
	}

	public int getHeadIndex()
	{
		return headIndex;
	}

	public void setHeadIndex(int headIndex)
	{
		this.headIndex = headIndex;
	}

	public int getSentenceIndex()
	{
		return sentenceIndex;
	}

	public void setSentenceIndex(int sentenceIndex)
	{
		this.sentenceIndex = sentenceIndex;
	}

	public Attribute getAttribute()
	{
		return attribute;
	}

	public void setAttribute(Attribute attribute)
	{
		this.attribute = attribute;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		result = prime * result + headIndex;
		result = prime * result + sentenceIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (head == null)
		{
			if (other.head != null)
				return false;
		}
		else if (!head.equals(other.head))
			return false;
		if (headIndex != other.headIndex)
			return false;
		if (sentenceIndex != other.sentenceIndex)
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return head + "(" + sentenceIndex + ", " + headIndex + ")";
	}
}
