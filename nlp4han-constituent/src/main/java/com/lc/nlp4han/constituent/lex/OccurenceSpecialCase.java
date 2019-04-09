package com.lc.nlp4han.constituent.lex;

/**
 * 此类特殊规则为并列结构和PU(标点符号：顿号)相关
 * 
 * @author qyl
 *
 */
public class OccurenceSpecialCase extends OccurenceCollins
{
	private String CCPOS = null;// 并列结构中的连词的词性标注
	
	private String CCword = null;// 连词
	
	private String leftLabel = null;// 并列结构左侧标记
	private String rightLabel = null;// 并列结构右侧标记
	
	private String lheadWord = null;// 并列结构左侧的中心词
	private String rheadWord = null;// 并列结构右侧的中心词
	
	private String lheadPOS = null;// 并列结构左侧的中心词词性
	private String rheadPOS = null;// 并列结构右侧的中心词词性

	public OccurenceSpecialCase(String[] strs)
	{
		super(strs[0]);
		this.CCPOS = strs[1];
		this.CCword = strs[2];
		this.leftLabel = strs[3];
		this.rightLabel = strs[4];
		this.lheadWord = strs[5];
		this.rheadWord = strs[6];
		this.lheadPOS = strs[7];
		this.rheadPOS = strs[8];
	}

	public OccurenceSpecialCase(String parentLabel, String cCPOS, String cCword, String leftLabel, String rightLabel,
			String lheadWord, String rheadWord, String lheadPOS, String rheadPOS)
	{
		super(parentLabel);
		CCPOS = cCPOS;
		CCword = cCword;
		this.leftLabel = leftLabel;
		this.rightLabel = rightLabel;
		this.lheadWord = lheadWord;
		this.rheadWord = rheadWord;
		this.lheadPOS = lheadPOS;
		this.rheadPOS = rheadPOS;
	}

	public String getCCPOS()
	{
		return CCPOS;
	}

	public void setCCPOS(String cCPOS)
	{
		CCPOS = cCPOS;
	}

	public String getCCword()
	{
		return CCword;
	}

	public void setCCword(String cCword)
	{
		CCword = cCword;
	}

	public String getLeftLabel()
	{
		return leftLabel;
	}

	public void setLeftLabel(String leftLabel)
	{
		this.leftLabel = leftLabel;
	}

	public String getRightLabel()
	{
		return rightLabel;
	}

	public void setRightLabel(String rightLabel)
	{
		this.rightLabel = rightLabel;
	}

	public String getLheadWord()
	{
		return lheadWord;
	}

	public void setLheadWord(String lheadWord)
	{
		this.lheadWord = lheadWord;
	}

	public String getRheadWord()
	{
		return rheadWord;
	}

	public void setRheadWord(String rheadWord)
	{
		this.rheadWord = rheadWord;
	}

	public String getLheadPOS()
	{
		return lheadPOS;
	}

	public void setLheadPOS(String lheadPOS)
	{
		this.lheadPOS = lheadPOS;
	}

	public String getRheadPOS()
	{
		return rheadPOS;
	}

	public void setRheadPOS(String rheadPOS)
	{
		this.rheadPOS = rheadPOS;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((CCPOS == null) ? 0 : CCPOS.hashCode());
		result = prime * result + ((CCword == null) ? 0 : CCword.hashCode());
		result = prime * result + ((leftLabel == null) ? 0 : leftLabel.hashCode());
		result = prime * result + ((lheadPOS == null) ? 0 : lheadPOS.hashCode());
		result = prime * result + ((lheadWord == null) ? 0 : lheadWord.hashCode());
		result = prime * result + ((rheadPOS == null) ? 0 : rheadPOS.hashCode());
		result = prime * result + ((rheadWord == null) ? 0 : rheadWord.hashCode());
		result = prime * result + ((rightLabel == null) ? 0 : rightLabel.hashCode());
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
		OccurenceSpecialCase other = (OccurenceSpecialCase) obj;
		if (CCPOS == null)
		{
			if (other.CCPOS != null)
				return false;
		}
		else if (!CCPOS.equals(other.CCPOS))
			return false;
		if (CCword == null)
		{
			if (other.CCword != null)
				return false;
		}
		else if (!CCword.equals(other.CCword))
			return false;
		if (leftLabel == null)
		{
			if (other.leftLabel != null)
				return false;
		}
		else if (!leftLabel.equals(other.leftLabel))
			return false;
		if (lheadPOS == null)
		{
			if (other.lheadPOS != null)
				return false;
		}
		else if (!lheadPOS.equals(other.lheadPOS))
			return false;
		if (lheadWord == null)
		{
			if (other.lheadWord != null)
				return false;
		}
		else if (!lheadWord.equals(other.lheadWord))
			return false;
		if (rheadPOS == null)
		{
			if (other.rheadPOS != null)
				return false;
		}
		else if (!rheadPOS.equals(other.rheadPOS))
			return false;
		if (rheadWord == null)
		{
			if (other.rheadWord != null)
				return false;
		}
		else if (!rheadWord.equals(other.rheadWord))
			return false;
		if (rightLabel == null)
		{
			if (other.rightLabel != null)
				return false;
		}
		else if (!rightLabel.equals(other.rightLabel))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return super.getParentLabel() + " " + CCPOS + " " + CCword + " " + leftLabel + " " + rightLabel + " "
				+ lheadWord + " " + rheadWord + " " + lheadPOS + " " + rheadPOS;
	}

}
