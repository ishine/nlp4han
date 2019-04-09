package com.lc.nlp4han.constituent.lex;

/**
 * 用于存储生成中心节点两侧的数据
 * 
 * @author qyl
 *
 */
public class OccurenceSides extends OccurenceHeadChild
{
	private int direction = 0;// 头结点为0,左侧为1，右侧为2
	
	private String sideLabel = null;// 所求孩子节点的标记
	private String sideHeadPOS = null;// 所求孩子节点的中心词词标记
	private String sideHeadWord;// 所求的孩子节点的中心词
	
	private boolean coor = false;// 并列结构
	private boolean pu = false;// 标点符号，由于只保留了顿号所以我们可以把它当做并列结构
	
	private Distance distance = new Distance();// 距离度量

	public OccurenceSides(String[] strs)
	{
		super(strs);
		this.direction = Integer.parseInt(strs[4]);
		this.sideLabel = strs[5];
		this.sideHeadPOS = strs[6];
		this.sideHeadWord = strs[7];
		this.coor = Boolean.parseBoolean(strs[8]);
		this.pu = Boolean.parseBoolean(strs[9]);
		this.distance = new Distance(Boolean.parseBoolean(strs[10]), Boolean.parseBoolean(strs[11]));
	}

	public OccurenceSides(String headLabel, String parentLabel, String headPOS, String headWord, int direction,
			String sideLabel, String sideHeadPOS, String sideHeadWord, boolean coor, boolean pu, Distance distance)
	{
		super(headLabel, parentLabel, headPOS, headWord);
		this.direction = direction;
		this.sideLabel = sideLabel;
		this.sideHeadPOS = sideHeadPOS;
		this.sideHeadWord = sideHeadWord;
		this.coor = coor;
		this.pu = pu;
		this.distance = distance;
	}

	public int getDirection()
	{
		return direction;
	}

	public void setDirection(int direction)
	{
		this.direction = direction;
	}

	public String getSideLabel()
	{
		return sideLabel;
	}

	public void setSideLabel(String sideLabel)
	{
		this.sideLabel = sideLabel;
	}

	public String getSideHeadPOS()
	{
		return sideHeadPOS;
	}

	public void setSideHeadPOS(String sideHeadPOS)
	{
		this.sideHeadPOS = sideHeadPOS;
	}

	public String getSideHeadWord()
	{
		return sideHeadWord;
	}

	public void setSideHeadWord(String sideHeadWord)
	{
		this.sideHeadWord = sideHeadWord;
	}

	public boolean isCoor()
	{
		return coor;
	}

	public void setCoor(boolean coor)
	{
		this.coor = coor;
	}

	public boolean isPu()
	{
		return pu;
	}

	public void setPu(boolean pu)
	{
		this.pu = pu;
	}

	public Distance getDistance()
	{
		return distance;
	}

	public void setDistance(Distance distance)
	{
		this.distance = distance;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (coor ? 1231 : 1237);
		result = prime * result + direction;
		result = prime * result + ((distance == null) ? 0 : distance.hashCode());
		result = prime * result + (pu ? 1231 : 1237);
		result = prime * result + ((sideHeadPOS == null) ? 0 : sideHeadPOS.hashCode());
		result = prime * result + ((sideHeadWord == null) ? 0 : sideHeadWord.hashCode());
		result = prime * result + ((sideLabel == null) ? 0 : sideLabel.hashCode());
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
		OccurenceSides other = (OccurenceSides) obj;
		if (coor != other.coor)
			return false;
		if (direction != other.direction)
			return false;
		if (distance == null)
		{
			if (other.distance != null)
				return false;
		}
		else if (!distance.equals(other.distance))
			return false;
		if (pu != other.pu)
			return false;
		if (sideHeadPOS == null)
		{
			if (other.sideHeadPOS != null)
				return false;
		}
		else if (!sideHeadPOS.equals(other.sideHeadPOS))
			return false;
		if (sideHeadWord == null)
		{
			if (other.sideHeadWord != null)
				return false;
		}
		else if (!sideHeadWord.equals(other.sideHeadWord))
			return false;
		if (sideLabel == null)
		{
			if (other.sideLabel != null)
				return false;
		}
		else if (!sideLabel.equals(other.sideLabel))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return super.toString() + " " + direction + " " + sideLabel + " " + sideHeadPOS + " " + sideHeadWord + " "
				+ coor + " " + pu + " " + distance;
	}

	@Override
	public String toReadableString()
	{
		return super.toReadableString() + ", dir=" + direction + ", Li=" + sideLabel + ", ti=" + sideHeadPOS + ", wi=" + sideHeadWord + ", corr="
		+ coor + ", pu=" + pu+ ", zerolen=" + distance.isAdjacency()
		+ ", verb=" + distance.isCrossVerb();
	}	
	
}
