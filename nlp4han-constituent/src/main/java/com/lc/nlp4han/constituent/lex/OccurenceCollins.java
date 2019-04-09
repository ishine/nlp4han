package com.lc.nlp4han.constituent.lex;

/**
 * 作为父类，减少在从CTB中抽取文法时的重复代码
 * 
 * @author qyl
 *
 */
public class OccurenceCollins
{
	private String parentLabel = null;// 父节点的非终结符标记
	private String headPOS = null;// 中心词词性标记,在NPB中为上一个修饰符的pos
	private String headWord = null;// 中心词,在NPB中为上一个修饰符的word

	public OccurenceCollins(String parentLabel, String headPOS, String headWord)
	{
		this.parentLabel = parentLabel;
		this.headPOS = headPOS;
		this.headWord = headWord;
	}

	public OccurenceCollins(String parentLabel)
	{
		this.parentLabel = parentLabel;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((headPOS == null) ? 0 : headPOS.hashCode());
		result = prime * result + ((headWord == null) ? 0 : headWord.hashCode());
		result = prime * result + ((parentLabel == null) ? 0 : parentLabel.hashCode());
		return result;
	}

	public String getParentLabel()
	{
		return parentLabel;
	}

	public void setParentLabel(String parentLabel)
	{
		this.parentLabel = parentLabel;
	}

	public String getHeadPOS()
	{
		return headPOS;
	}

	public void setHeadPOS(String headPOS)
	{
		this.headPOS = headPOS;
	}

	public String getHeadWord()
	{
		return headWord;
	}

	public void setHeadWord(String headWord)
	{
		this.headWord = headWord;
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
		OccurenceCollins other = (OccurenceCollins) obj;
		if (headPOS == null)
		{
			if (other.headPOS != null)
				return false;
		}
		else if (!headPOS.equals(other.headPOS))
			return false;
		if (headWord == null)
		{
			if (other.headWord != null)
				return false;
		}
		else if (!headWord.equals(other.headWord))
			return false;
		if (parentLabel == null)
		{
			if (other.parentLabel != null)
				return false;
		}
		else if (!parentLabel.equals(other.parentLabel))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return parentLabel + " " + headPOS + " " + headWord;
	}

	public String toReadableString()
	{
		return "P=" + parentLabel + ", t=" + headPOS + ", w=" + headWord;
	}

}
