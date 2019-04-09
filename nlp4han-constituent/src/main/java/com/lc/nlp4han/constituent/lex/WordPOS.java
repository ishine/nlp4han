package com.lc.nlp4han.constituent.lex;

/**
 * 单词和词性标注的结合，或者只有词性作为统计数据存储的结构
 * 
 * @author qyl
 *
 */
public class WordPOS
{
	private String word = null;
	private String pos = null;

	public WordPOS(String word, String pos)
	{
		this.word = word;
		this.pos = pos;
	}

	public String getWord()
	{
		return word;
	}

	public void setWord(String word)
	{
		this.word = word;
	}

	public String getPos()
	{
		return pos;
	}

	public void setPos(String pos)
	{
		this.pos = pos;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + ((word == null) ? 0 : word.hashCode());
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
		WordPOS other = (WordPOS) obj;
		if (pos == null)
		{
			if (other.pos != null)
				return false;
		}
		else if (!pos.equals(other.pos))
			return false;
		if (word == null)
		{
			if (other.word != null)
				return false;
		}
		else if (!word.equals(other.word))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return word + " " + pos;
	}
}
