package com.lc.nlp4han.segpos;

import com.lc.nlp4han.ml.util.Evaluator;

/**
 * 评估器
 * @author 王馨苇
 *
 */
public class WordSegAndPosEvaluator extends Evaluator<WordSegAndPosSample>{

	private WordSegAndPosME tagger;
	private WordSegAndPosMeasure measure;
	
	/**
	 * 构造
	 * @param tagger 训练得到的模型
	 */
	public WordSegAndPosEvaluator(WordSegAndPosME tagger) {
		this.tagger = tagger;
	}
	
	/**
	 * 构造
	 * @param tagger 训练得到的模型
	 * @param evaluateMonitors 评估的监控管理器
	 */
	public WordSegAndPosEvaluator(WordSegAndPosME tagger,WordSegAndPosEvaluateMonitor... evaluateMonitors) {
		super(evaluateMonitors);
		this.tagger = tagger;
	}
	
	/**
	 * 设置评估指标的对象
	 * @param measure 评估指标计算的对象
	 */
	public void setMeasure(WordSegAndPosMeasure measure){
		this.measure = measure;
	}
	
	/**
	 * 得到评估的指标
	 * @return
	 */
	public WordSegAndPosMeasure getMeasure(){
		return this.measure;
	}
	
	/**
	 * 评估得到指标
	 * @param reference 参考的语料
	 */
	@Override
	protected WordSegAndPosSample processSample(WordSegAndPosSample reference) {
		String[] wordsRef = reference.getWords();
		String[] posesRef = reference.getPoses();
		String[] charactersRef = reference.getCharacters();
		String[][] acRef = reference.getAditionalContext();

		//此时读取的生语料，没有分词和词性标注
		String[] tagsandposesPre = tagger.tagAll(charactersRef,acRef);

		String[] tagsPre = WordSegAndPosSample.toTag(tagsandposesPre); 
		String[] wordsPre = WordSegAndPosSample.toWord(charactersRef, tagsandposesPre);
		String[] posesPre = WordSegAndPosSample.toPos(tagsandposesPre);

//		for (int i = 0; i < wordsPre.length; i++) {
//			System.out.print(wordsPre[i]+"/"+posesPre[i]);
//		}
//		System.out.println();
//		for (int i = 0; i < tagsandposesPre.length; i++) {
//			System.out.print(tagsandposesPre[i]+"\t");
//		}
//		System.out.println();
		WordSegAndPosSample prediction = new WordSegAndPosSample(charactersRef,tagsPre,wordsPre,posesPre);
//		measure.updateTag(wordsRef, wordsPre);
		measure.updateSegAndPos(wordsRef, posesRef, wordsPre, posesPre);
		return prediction;
	}

}
