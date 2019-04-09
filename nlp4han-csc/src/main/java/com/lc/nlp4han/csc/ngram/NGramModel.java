package com.lc.nlp4han.csc.ngram;

import com.lc.nlp4han.csc.util.Sentence;

/**
 * n元模型接口 ，提供计算ngram概率和句子概率的方法
 */
public interface NGramModel
{

	/**
	 * 返回句子的n元概率
	 * 
	 * @param sentence
	 *            待计算概率的串
	 * @param n
	 *            n元的最高阶
	 * @return 句子的n元概率
	 */
	public double getSentenceLogProb(Sentence sentence, int n);

	/**
	 * 返回n元模型的最大阶数
	 * 
	 * @return n元模型的最大阶数
	 */
	public int getOrder();

	/**
	 * 返回n元串的概率
	 * 
	 * @param grams
	 *            待求概率的串
	 * @return n元串的概率
	 */
	public double getNGramLogProb(String[] grams, int n);
}
