package com.lc.nlp4han.csc.wordseg;

import java.util.ArrayList;

import com.lc.nlp4han.csc.util.Sentence;

/**
 * 分词的抽象类，提供公用的抽象方法
 */
public abstract class AbstractWordSegment
{

	/**
	 * 给定句子返回分词后的词列表
	 * 
	 * @param sentence
	 *            待分词的句子
	 * @return 分词后的词列表
	 */
	public abstract ArrayList<String> segment(Sentence sentence);
}
