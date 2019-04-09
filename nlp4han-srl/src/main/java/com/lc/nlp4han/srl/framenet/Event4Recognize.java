package com.lc.nlp4han.srl.framenet;

import java.util.ArrayList;

/**
 * 识别过程的事件
 * 
 * @author qyl
 *
 */
public class Event4Recognize
{
	// 特征
	// 中心词
	private String headWord;
	// path,在句法解析树中，谓词到role的路径
	private ArrayList<String> path;
	// t,谓词/目标词
	private Predicate predicate;

	// 概率值
	private double value;

	public Event4Recognize()
	{

	}

	public Event4Recognize(String headWord, ArrayList<String> path, Predicate predicate,  double value)
	{
		this.headWord = headWord;
		this.path = path;
		this.predicate = predicate;
		this.value = value;
	}

}
