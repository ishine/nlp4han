package com.lc.nlp4han.constituent;

/**
 * 成分树分析器
 * 
 * @author 刘小峰
 * @author 王馨苇
 *
 */
public interface ConstituentParser
{

	/**
	 * 得到最好的成分树
	 * 
	 * @param words
	 *            分词序列
	 * @param poses
	 *            词性标记
	 * @return 不能解析返回null
	 */
	ConstituentTree parse(String[] words, String[] poses);


	/**
	 * 得到最好的K个成分树
	 * @param words
	 *            词语
	 * @param poses
	 *            词性标记
	 * @param k
	 *            最好的K个结果
	 * 
	 * @return 不能解析返回null
	 */
	ConstituentTree[] parse(String[] words, String[] poses, int k);
}
