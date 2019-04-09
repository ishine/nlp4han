package com.lc.nlp4han.csc.model;

import java.util.ArrayList;

import com.lc.nlp4han.csc.util.Sentence;

/**
 * 噪音通道模型接口
 */
public interface NoisyChannelModel {

	/**
	 * 返回给定句子的最优纠正后的句子
	 * @param sentence	待纠正的句子
	 * @return			纠正后的句子
	 */
	public Sentence getBestSentence(Sentence sentence);
	
	/**
	 * 返回给定句子的k个候选句子列表，根据句子的得分从高到低，从前往后排列
	 * @param sentence	待纠正的句子
	 * @param k			返回的候选句子个数
	 * @return			k个候选句子列表
	 */
	public ArrayList<Sentence> getBestKSentence(Sentence sentence, int k);
}
