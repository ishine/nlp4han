package com.lc.nlp4han.segpos;

import com.lc.nlp4han.pos.POSTagger;
import com.lc.nlp4han.segment.WordSegmenter;

/**
 * 组合分词和词性标注接口
 * 
 * @author 刘小峰
 * @author 王馨苇
 *
 */
public interface WordSegAndPos extends WordSegmenter, POSTagger {

	/**
	 * 对生语料进行分词和词性标记
	 * @param words 未切分的句子
	 * @return word/tag序列，两者间以wordPosSepChar连接
	 */
	public String[] segmentAndTag(String sentence, char wordPosSepChar);
	
}
