package com.lc.nlp4han.constituent.lex;

/**
 * 用于存储生成头结点的数据 在解析过程还可用于向上生成单位规则
 * 
 * @author qyl
 *
 */
public class OccurenceHeadChild extends OccurenceCollins
{

	private String headLabel = null;// 中心节点标记

	public OccurenceHeadChild(String[] strs)
	{
		super(strs[0], strs[1], strs[2]);
		this.headLabel = strs[3];
	}

	public OccurenceHeadChild(String headLabel, String parentLabel, String headPOS, String headWord)
	{
		super(parentLabel, headPOS, headWord);
		this.headLabel = headLabel;
	}

	public String getHeadLabel()
	{
		return headLabel;
	}

	public void setHeadLabel(String headLabel)
	{
		this.headLabel = headLabel;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((headLabel == null) ? 0 : headLabel.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OccurenceHeadChild other = (OccurenceHeadChild) obj;
		if (headLabel == null)
		{
			if (other.headLabel != null)
				return false;
		}
		else if (!headLabel.equals(other.headLabel))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return super.toString() + " " + headLabel;
	}

	@Override
	public String toReadableString()
	{
		return super.toReadableString() + ", H=" + headLabel;
	}

	
}
