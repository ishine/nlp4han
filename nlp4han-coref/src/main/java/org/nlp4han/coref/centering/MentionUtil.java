package org.nlp4han.coref.centering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.nlp4han.coref.sieve.Mention;

public class MentionUtil
{
	/**
	 * 将Mention集进行排序，此处按照语法角色的优先级进行排序
	 * 
	 * @param mentions
	 * @return
	 */
	public static List<Mention> GrammaticalRoleBasedSort(List<Mention> mentions)
	{
		if (mentions != null && mentions.size() > 0)
		{
			List<Mention> result = new ArrayList<Mention>();
			result.addAll(mentions);
			Collections.sort(result, new GrammaticalRoleBasedMentionComparator());
			if (result.size() != mentions.size())
				throw new RuntimeException("结果错误！");
			return result;
		}
		return null;
	}
	
	/**
	 * 将Mention集进行排序，此处按照Mention的位置进行排序
	 * @param mentions
	 * @return
	 */
	public static List<Mention> sort(List<Mention> mentions)
	{
		if (mentions != null && mentions.size() > 0)
		{
			List<Mention> result = new ArrayList<Mention>();
			result.addAll(mentions);
			Collections.sort(result, new LocationBasedMentionComparator());
			if (result.size() != mentions.size())
				throw new RuntimeException("结果错误！");
			return result;
		}
		return null;
	} 
	
	private static class GrammaticalRoleBasedMentionComparator implements Comparator<Mention>
	{
		@Override
		public int compare(Mention arg0, Mention arg1)
		{
			String[] grammaticalRolePriority = { "SBJ", "OBJ", "IO" };
			if (arg0.getGrammaticalRole().equals(arg1.getGrammaticalRole()))
			{
				if (arg0.getMentionID() != arg1.getMentionID())
					return arg0.getMentionID() - arg1.getMentionID();
				else if (arg0.getSentenceIndex() != arg1.getSentenceIndex())
					return arg0.getSentenceIndex() - arg1.getSentenceIndex();
				else 
					return arg0.getHeadIndex() - arg1.getHeadIndex();
			}
			else
			{
				int s1 = -1;
				int s2 = -1;
				for (int i = 0; i < grammaticalRolePriority.length; i++)
				{
					if (arg0.getGrammaticalRole().equals(grammaticalRolePriority[i]))
						s1 = i;
					if (arg1.getGrammaticalRole().equals(grammaticalRolePriority[i]))
						s2 = i;
					if (s1 != -1 && s2 != -1)
						break;
				}
				return s1 - s2;
			}
		}

	}
	
	private static class LocationBasedMentionComparator implements Comparator<Mention>
	{
		@Override
		public int compare(Mention arg0, Mention arg1)
		{
			
			if (arg0.getSentenceIndex() != arg1.getSentenceIndex())
				return arg0.getSentenceIndex() - arg1.getSentenceIndex();
			else if (arg0.getHeadIndex() != arg1.getHeadIndex())
				return arg0.getHeadIndex() - arg1.getHeadIndex();
			else
				return arg0.getMentionID() - arg1.getMentionID();
		}

	}
}

