package com.lc.nlp4han.srl.framenet;

import java.util.ArrayList;

/**
 * 语义角色标注过程中使用的谓词类
 * @author qyl
 *
 */
public class Predicate
{
	
	public Predicate()
	{
		
	}
    
	/**
	 * 添加谓词，包含词和在句子中索引
	 * @param word
	 * @param index
	 */
	public void addSinglePredicateWord(String word,int index) {
		
	}
	
	/**
	 * 获取谓词/谓词短语对应的索引数组
	 * @return
	 */
	public int[] getPredicateIndexs() {
       return null;
	}
	
	/**
	 * 获取谓词短语/谓词
	 * @return
	 */
	public ArrayList<String> getPredicate(){
		return null;		
	}
	
	/**
	 * 获取谓词短语中单个谓词的索引
	 * @param predicate
	 * @return
	 */
	public int getIndex4SinglePredicateWord(String preWord) {
		return 0;
	}
}
