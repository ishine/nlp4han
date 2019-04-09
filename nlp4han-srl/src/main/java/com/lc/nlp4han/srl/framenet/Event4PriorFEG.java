package com.lc.nlp4han.srl.framenet;

import java.util.ArrayList;

/**
 * Frame Element Group关于目标词的先验事件
 * @author qyl
 *
 */
public class Event4PriorFEG
{
	//Frame Element Group,框架元素组
	private ArrayList<String> list=new ArrayList<String>();
	
	//谓词
	private Predicate predicate;
	
	public Event4PriorFEG()
	{
	}

	public Event4PriorFEG(ArrayList<String> list, Predicate predicate)
	{
		this.list = list;
		this.predicate=predicate;
	} 
}
