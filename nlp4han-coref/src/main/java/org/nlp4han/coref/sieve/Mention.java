package org.nlp4han.coref.sieve;

import org.nlp4han.coref.hobbs.Attribute;

import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreeNodeUtil;

public class Mention
{
	private int mentionID;
	private String head;
	private int headIndex;
	private int sentenceIndex;
	
	private TreeNode headNode;
	
	private boolean isPronoun;
	
	private Attribute attribute;
	
	private Entity entity;
	
	private String grammaticalRole;
	
	public Mention()
	{
		
	}
	
	public Mention(int sentenceIndex, int headIndex)
	{
		// TODO Auto-generated method stub
	}
	
	public String getHead()
	{
		// TODO Auto-generated method stub
		return head;
	}
	
	public void setHead(String head, int sentenceIndex, int headIndex)
	{
		this.head = head;
		this.sentenceIndex = sentenceIndex;
		this.headIndex = headIndex;
	}

	public int getMentionID()
	{
		return mentionID;
	}

	public void setMentionID(int mentionID)
	{
		this.mentionID = mentionID;
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

	public boolean isPronoun()
	{
		return isPronoun;
	}

	public void setPronoun(boolean isPronoun)
	{
		this.isPronoun = isPronoun;
	}

	public Attribute getAttribute()
	{
		return attribute;
	}

	public void setAttribute(Attribute attribute)
	{
		this.attribute = attribute;
	}

	public Entity getEntity()
	{
		return entity;
	}

	public void setEntity(Entity entity)
	{
		this.entity = entity;
	}

	public String getGrammaticalRole()
	{
		return grammaticalRole;
	}

	public void setGrammaticalRole(String grammaticalRole)
	{
		this.grammaticalRole = grammaticalRole;
	}

	public void setHead(String head)
	{
		this.head = head;
	}

	public TreeNode getHeadNode()
	{
		return headNode;
	}

	public void setHeadNode(TreeNode headNode)
	{
		this.headNode = headNode;
		
		this.head = TreeNodeUtil.getString(headNode);
		
		TreeNode leaf = TreeNodeUtil.getHead(headNode);
		this.headIndex = TreeNodeUtil.siteOfLeaves(leaf);
	}
	
	public String toString()
	{
		return head;
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
		Mention other = (Mention) obj;
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
	
	
}
