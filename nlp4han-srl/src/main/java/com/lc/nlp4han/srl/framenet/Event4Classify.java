package com.lc.nlp4han.srl.framenet;
/**
 * 分类阶段的事件
 * @author qyl
 *
 */
public class Event4Classify
{
	//特征
	//中心词
	private String headWord;
	//voice,此处特质主动/被动
	private boolean active;
	//位置，此处记录该角色在谓词的前后位置
	private boolean beforePred;
	//gov,只有两个值：S/VP,而且被限制于NP使用
	private boolean govVP;
	//Phrase Type,短语类型
	private String phraseType;
	//目标词/谓词
	private Predicate predicate;
	
	//语义角色/框架元素
	private String role;
	
	//概率值
	private double value;

	public Event4Classify()
	{
		
	}
	
	public Event4Classify(String headWord, boolean active, boolean beforePred, boolean govVP, String phraseType,
			Predicate predicate, String role, double value)
	{
		this.headWord = headWord;
		this.active = active;
		this.beforePred = beforePred;
		this.govVP = govVP;
		this.phraseType = phraseType;
		this.predicate = predicate;
		this.role = role;
		this.value = value;
	}
}
