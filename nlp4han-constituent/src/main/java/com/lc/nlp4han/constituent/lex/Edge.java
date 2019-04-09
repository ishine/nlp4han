package com.lc.nlp4han.constituent.lex;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 在Lexpcfg解析过程存储数据的边
 * 
 * @author qyl
 *
 */
public class Edge implements Comparable<Edge>
{
	private String label = null;
	
	// head儿子的非终结符符号
	private String headLabel = null;
	private String headWord = null;
	private String headPOS = null;
	
	private int start = -1;
	private int end = -1;
	
	// head左边距离特征
	private Distance lc = null;
	
	// head右边距离特征
	private Distance rc = null;
  
	private boolean coor = false;
	private boolean pu = false;
  
    //边是否接受stop概率
	private boolean stop = false;
	private double prob =-Double.MAX_VALUE;
  
	private ArrayList<Edge> children = null;

	public Edge()
	{
	}

	public Edge(String label, String headLabel, String headWord, String headPOS, int start, int end, Distance lc,
			Distance rc, boolean stop, double pro, ArrayList<Edge> children)
	{
		this.label = label;
		this.headLabel = headLabel;
		this.headWord = headWord;
		this.headPOS = headPOS;
		this.start = start;
		this.end = end;
		this.lc = lc;
		this.rc = rc;
		this.stop = stop;
		this.prob = pro;
		this.children = children;
	}

	// 添加并列结构的构造方法
	public Edge(String label, String headLabel, String headWord, String headPOS, int start, int end, Distance lc,
			Distance rc, boolean coor, boolean pu, boolean stop, double pro, ArrayList<Edge> children)
	{
		this.label = label;
		this.headLabel = headLabel;
		this.headWord = headWord;
		this.headPOS = headPOS;
		this.start = start;
		this.end = end;
		this.lc = lc;
		this.rc = rc;
		this.coor = coor;
		this.pu = pu;
		this.stop = stop;
		this.prob = pro;
		this.children = children;
	}

	public Edge getFirstChild()
	{
		Collections.sort(this.getChildren());
		return this.getChildren().get(0);
	}

	public Edge getChild(int i)
	{
		Collections.sort(this.getChildren());
		if (i > children.size())
		{
			return null;
		}
		return children.get(i);
	}

	public int getChildNum()
	{
		return this.getChildren().size();
	}

	public String getChildLabel(int i)
	{
		Collections.sort(this.getChildren());
		return children.get(i).getLabel();
	}

	public Edge getLastChild()
	{
		Collections.sort(this.getChildren());
		int num = this.getChildren().size();
		return this.getChildren().get(num - 1);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (coor ? 1231 : 1237);
		result = prime * result + ((headLabel == null) ? 0 : headLabel.hashCode());
		result = prime * result + ((headPOS == null) ? 0 : headPOS.hashCode());
		result = prime * result + ((headWord == null) ? 0 : headWord.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((lc == null) ? 0 : lc.hashCode());
		result = prime * result + (pu ? 1231 : 1237);
		result = prime * result + ((rc == null) ? 0 : rc.hashCode());
		result = prime * result + (stop ? 1231 : 1237);
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
		
		Edge other = (Edge) obj;
		if (coor != other.coor)
			return false;
		
		if (headLabel == null)
		{
			if (other.headLabel != null)
				return false;
		}
		else if (!headLabel.equals(other.headLabel))
			return false;
		
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
		
		if (label == null)
		{
			if (other.label != null)
				return false;
		}
		else if (!label.equals(other.label))
			return false;
		
		if (lc == null)
		{
			if (other.lc != null)
				return false;
		}
		else if (!lc.equals(other.lc))
			return false;
		
		if (pu != other.pu)
			return false;
		
		if (rc == null)
		{
			if (other.rc != null)
				return false;
		}
		else if (!rc.equals(other.rc))
			return false;
		
		if (stop != other.stop)
			return false;
		
		return true;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getHeadLabel()
	{
		return headLabel;
	}

	public void setHeadLabel(String headLabel)
	{
		this.headLabel = headLabel;
	}

	public String getHeadWord()
	{
		return headWord;
	}

	public void setHeadWord(String headWord)
	{
		this.headWord = headWord;
	}

	public String getHeadPOS()
	{
		return headPOS;
	}

	public void setHeadPOS(String headPOS)
	{
		this.headPOS = headPOS;
	}

	public int getStart()
	{
		return start;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public int getEnd()
	{
		return end;
	}

	public void setEnd(int end)
	{
		this.end = end;
	}

	public Distance getLc()
	{
		return lc;
	}

	public void setLc(Distance lc)
	{
		this.lc = lc;
	}

	public Distance getRc()
	{
		return rc;
	}

	public void setRc(Distance rc)
	{
		this.rc = rc;
	}

	public boolean isStop()
	{
		return stop;
	}

	public void setStop(boolean stop)
	{
		this.stop = stop;
	}

	public double getProb()
	{
		return prob;
	}

	public void setProb(double pro)
	{
		this.prob = pro;
	}

	public ArrayList<Edge> getChildren()
	{
		return children;
	}

	public void setChildren(ArrayList<Edge> children)
	{
		this.children = children;
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

	@Override
	public String toString()
	{
		return "Edge [label=" + label + ", headLabel=" + headLabel + ", headWord=" + headWord + ", headPOS=" + headPOS
				+ ", start=" + start + ", end=" + end + ", lc=" + lc + ", rc=" + rc + ", stop=" + stop + ", pro=" + prob
				+ "]";
	}

	@Override
	public int compareTo(Edge o)
	{// 起始符在前的，孩子节点的顺序排在前面
		if (start < o.getStart())
		{
			return -1;
		}

		if (start > o.getStart())
		{
			return 1;
		}

		return 0;
	}

}
