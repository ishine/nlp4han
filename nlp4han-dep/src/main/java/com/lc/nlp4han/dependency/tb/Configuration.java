package com.lc.nlp4han.dependency.tb;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 转换配置类
 * 
 * 配置由三部分构成：词栈、词缓冲区和关系集合
 *
 */
public abstract class Configuration
{
	protected ArrayDeque<Vertice> stack = new ArrayDeque<Vertice>();
	protected LinkedList<Vertice> wordsBuffer = new LinkedList<Vertice>();
	protected ArrayList<Arc> arcs = new ArrayList<Arc>();

	public Configuration(LinkedList<Vertice> wordsBuffer)
	{
		stack.push(wordsBuffer.get(0));
		wordsBuffer.remove(0);

		this.wordsBuffer = wordsBuffer;
	}

	public Configuration(String[] words, String[] pos)
	{
		if (words.length != 0)
		{
			wordsBuffer = Vertice.getWordsBuffer(words, pos);
			stack.push(wordsBuffer.get(0));
			wordsBuffer.remove(0);
		}
	}

	public Configuration()
	{
	}

	/**
	 * 通过基本操作对当前conf进行转换
	 * 
	 * @return 转换后的conf
	 */
	public abstract void transfer(Action actType);

	/**
	 * 判断是否reduce
	 * 
	 * @return 有关系返回true
	 */
	public abstract boolean canReduce(String[] dependencyIndices);

	public void initialConf(String[] words, String[] pos)
	{
		wordsBuffer.clear();
		stack.clear();
		arcs.clear();
		if (words.length != 0)
		{
			wordsBuffer = Vertice.getWordsBuffer(words, pos);
			stack.push(wordsBuffer.get(0));
			wordsBuffer.remove(0);
		}
	}

	public void generateConfByActions(String[] wordpos, String[] priorActions)
	{
		String[] words = new String[wordpos.length / 2 + 1];
		String[] poses = new String[wordpos.length / 2 + 1];
		for (int i = 0; i < words.length; i++)
		{
			String[] word_pos = wordpos[i].split("/");
			words[i] = word_pos[0];
			poses[i] = word_pos[1];
		}
		
		initialConf(words, poses);
		
		for (String preAction : priorActions)
		{
			Action at = Action.toType(preAction);
			transfer(at);
		}
	}

	public boolean isFinalConf()
	{
		if (wordsBuffer.isEmpty() && stack.size() == 1)
			return true;
		else
			return false;
	}

	public void addArc(Arc arc)
	{
		arcs.add(arc);
	}

	/**
	 * 缓冲区头词移入到栈
	 */
	public void shift()
	{
		if (wordsBuffer.size() != 0)
		{
			stack.push(wordsBuffer.remove(0));
		}
	}

	public String toString()
	{
		Vertice[] vS = stack.toArray(new Vertice[stack.size()]);
		Vertice[] vB = wordsBuffer.toArray(new Vertice[wordsBuffer.size()]);
		
		StringBuilder stackStr = new StringBuilder();
		StringBuilder bufferStr = new StringBuilder();
		for (int i = 0; i < stack.size(); i++)
		{
			stackStr.append(vS[stack.size() - i - 1].toString() + " ");
		}
		
		for (int i = 0; i < wordsBuffer.size(); i++)
		{
			bufferStr.append(vB[i].toString() + " ");
		}

		return "栈底至栈顶元素：" + stackStr.toString() + " ___" + "buffer: " + bufferStr.toString();
	}

	public String arcsToString()
	{
		StringBuilder allArc = new StringBuilder();
		allArc.append("arcs:" + "\r\n");
		for (int i = 0; i < arcs.size(); i++)
		{
			allArc.append(arcs.get(arcs.size() - i - 1).toString() + "\r\n");
		}
		
		return allArc.toString();
	}

	public ArrayDeque<Vertice> getStack()
	{
		return stack;
	}

	public void setStack(ArrayDeque<Vertice> stack)
	{
		this.stack = stack;
	}

	public LinkedList<Vertice> getWordsBuffer()
	{
		return wordsBuffer;
	}

	public void setWordsBuffer(LinkedList<Vertice> wordsBuffer)
	{
		this.wordsBuffer = wordsBuffer;
	}

	public ArrayList<Arc> getArcs()
	{
		return arcs;
	}

	public void setArcs(ArrayList<Arc> arcs)
	{
		this.arcs = arcs;
	}
}
