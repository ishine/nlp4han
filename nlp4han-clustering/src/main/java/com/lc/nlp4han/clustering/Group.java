package com.lc.nlp4han.clustering;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Group
{
	private List<Text> members;
	private Sample center;

	public Group()
	{
		members = new LinkedList<Text>();
	}
	
	public Group(Text text)
	{
		if (members == null)
			members = new LinkedList<Text>();
		members.add(text);
	}
	
	public Group(List<Text> texts)
	{
		if (members == null)
			members = new LinkedList<Text>();
		members.addAll(texts);
	}

	public void addMember(Text t)
	{
		this.members.add(t);
	}

	public void merge(Group g2)
	{
		List<Text> ms = g2.getMembers();
		members.addAll(ms);
	}

	public Boolean removeMember(Text text)
	{
		return members.remove(text);
	}

	public List<Text> getMembers()
	{
		return members;
	}

	public void setMembers(List<Text> members)
	{
		this.members = members;
	}

	public Sample getCenter()
	{
		return center;
	}

	public void setCenter(Sample center)
	{
		this.center = center;
	}
	
	public boolean containsText(Text text)
	{
		if (members != null)
			return members.contains(text);
		else
			return false;
	}

	@Override
	public String toString()
	{
		return "Group [members=" + members + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((members == null) ? 0 : members.hashCode());
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
		Group other = (Group) obj;
		if (members == null)
		{
			if (other.members != null)
				return false;
		}
		else if (!members.equals(other.members))
			return false;
		return true;
	}
	
	public List<String> getMembersNames()
	{
		List<String> result = new ArrayList<String>();
		for (Text t : members)
		{
			result.add(t.getName());
		}
		return result;
	}
	
	public int size()
	{
		if (members != null)
			return members.size();
		else
			return 0;
	}
	
	public void clear()
	{
		if (members != null)
			members.clear();
	}
	
}
