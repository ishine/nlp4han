package org.nlp4han.coref.centering;

import java.util.ArrayList;
import java.util.List;

import org.nlp4han.coref.sieve.Mention;

/**
 * 中心数值（Cb、Cf、Cp）
 * 
 * @author 杨智超
 *
 */
public class Center
{
	private Mention Cb; // 回指中心（backward looking center）
	private List<Mention> Cf; // 下指中心（forward looking center）
	private Mention Cp; // 优选中心（preferred center）

	/**
	 * 通过会话中的实体集和将其中的代词替换成先行词后的实体集创建会话的Center类
	 * 
	 * @param mentions
	 * @param newMentions
	 */
	public Center(List<Mention> mentions, List<Mention> newMentions)
	{// 注意：参数不能为null，当两个参数相等时，为第一句话，Cb为undefined(null) 此处需修改
		generateCf(newMentions);
		generateCb(mentions, newMentions);
		generateCp(mentions, newMentions);
	}

	public Mention getCb()
	{
		return Cb;
	}

	public List<Mention> getCf()
	{
		return Cf;
	}

	public Mention getCp()
	{
		return Cp;
	}

	/**
	 * 生成回指中心Cb
	 * 
	 * @param mentions
	 * @param newMentions
	 */
	public void generateCb(List<Mention> mentions, List<Mention> newMentions)
	{
		if (!mentions.equals(newMentions))
		{
			List<Mention> anaphorMentions = new ArrayList<Mention>();
			for (int i = 0; i < newMentions.size(); i++)
			{
				if (!mentions.get(i).equals(newMentions.get(i)))
					anaphorMentions.add(newMentions.get(i));
			}

			List<Mention> es = MentionUtil.GrammaticalRoleBasedSort(anaphorMentions);
			if (es != null && es.size() > 0)
				Cb = es.get(0);
			else
				Cb = null;
		}
		else
			Cb = null;

	}

	/**
	 * 生成下指中心Cf
	 * 
	 * @param newMention
	 */
	public void generateCf(List<Mention> newMentions)
	{
		if (newMentions == null)
			throw new RuntimeException("输入不能为null");
		Cf = newMentions;
	}

	/**
	 * 生成优选中心Cp
	 * 
	 * @param newMentions
	 * @param mentions
	 */
	public void generateCp(List<Mention> mentions, List<Mention> newMentions)
	{
		if (Cf == null || Cf.size() < 1)
			throw new RuntimeException("Cf错误");
		List<Mention> e = MentionUtil.GrammaticalRoleBasedSort(mentions);
		int index = mentions.indexOf(e.get(0));
		Cp = newMentions.get(index);
	}

	@Override
	public String toString()
	{
		String strCf;
		if (Cf != null && Cf.size() > 0)
		{
			StringBuilder strbCf = new StringBuilder();
			strbCf.append("(");
			boolean isFirst = true;
			for (Mention e : Cf)
			{
				if (isFirst)
				{
					strbCf.append(e.getHead());
					isFirst = false;
					continue;
				}
				strbCf.append(", " + e.getHead());
			}
			strbCf.append(")");
			strCf = strbCf.toString();
		}
		else
			strCf = "( )";
		if (Cb != null)
			return "[Cb=" + Cb.getHead() + ", Cf=" + strCf + ", Cp=" + Cp.getHead() + "]";
		else
			return "[Cf=" + strCf + ", Cp=" + Cp.getHead() + "]";
	}

}
