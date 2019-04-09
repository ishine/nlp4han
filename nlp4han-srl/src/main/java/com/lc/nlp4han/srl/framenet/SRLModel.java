package com.lc.nlp4han.srl.framenet;

import java.util.ArrayList;

/**
 * 语义角色标注模型
 * @author qyl
 *
 */
public class SRLModel
{
	/**
	 * 获取角色分类的概率
	 * @return
	 */
	public double getPro4Classify(Event4Classify ec) {
		return 0;		
	}
	
	/**
	 * 获取角色识别的概率
	 * @return
	 */
	public double getPro4Recognize(Event4Recognize er) {
		return 0;	
	}

	/**
	 * 获取框架元素组的先验概率
	 * @return
	 */
	public double getPro4FEGPrior(Event4PriorFEG ep) {
		return 0;
	}
	
	/**
	 * 以目标词为条件获取role的概率
	 * @return
	 */
	public double getPro4Predicate(String role,Predicate pridicate) {
		return 0;	
	}
	
	/**
	 * 以短语类型pt，控制类型gov,目标词为条件获取role的概率
	 * @return
	 */
	public double getPro4PtGovPredicate(String phraseType,boolean govVP,Predicate predicate) {
		return 0;
	}

	/**
	 * 以短语类型pt，位置position,噪声voice为条件获取role的概率
	 * @return
	 */
	public double getPro4PtPositionVoice(String role,String phraseType,boolean beforePred,boolean active) {
		return 0;
	}
	
	/**
	 * 以短语类型pt，位置position,噪声voice,目标词predicate为条件获取role的概率
	 * @return
	 */
	public double getPro4getPro4PtPositionVoicePre(String role,String phraseType,boolean beforePred,boolean voice,Predicate predicate) {
		return 0;
	}
	
	/**
	 * 以中心词为条件获取role的概率
	 * @return
	 */
	public double getPro4Head(String role,String headWord) {
		return 0;
	}
	
	/**
	 *  以中心词,目标词为条件获取role的概率
	 * @return
	 */
	public double getPro4HeadPre(String role,String headWord,Predicate predicate) {
		return 0;
	}
	
	/**
	 *  以中心词,目标词,短语类型为条件获取role的概率
	 * @return
	 */
	public double getPro4HeadPrePt(String role,String headWord,Predicate predicate,String phraseType) {
		return 0;
	}
	
	/**
	 * 以路径为条件获，获取在识别过程中的概率
	 * @return
	 */
	public double getFePro4Path(ArrayList<String> path) {
		return 0;
	}
	
	/**
	 * 以路径和谓词为条件，获取在识别过程中的概率
	 * @return
	 */
	public double getFePro4PathPre(ArrayList<String> path,Predicate predicate) {
		return 0;
	}
	
	/**
	 * 以中心词和谓词为条件，获取在识别过程中的概率
	 * @return
	 */
   public double getFePro4HeadPre(String headWord,Predicate predicate) {
	   return 0;
   }
}
