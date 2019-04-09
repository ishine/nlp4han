package com.lc.nlp4han.csc.evaluation;

/**
 * 评价指标接口
 */
public interface Evaluation
{

	/**
	 * 错误率(没有错的句子被修改的概率)
	 * 
	 * @return
	 */
	public double getFalsePositiveRate();

	/**
	 * 检测的准确率
	 * 
	 * @return 检测的准确率
	 */
	public double getDetectAccuracy();

	/**
	 * 检测的精确率
	 * 
	 * @return 检测的精确率
	 */
	public double getDetectPrecision();

	/**
	 * 检测的召回率
	 * 
	 * @return 检测的召回率
	 */
	public double getDetectRecall();

	/**
	 * 检测的F值
	 * 
	 * @return 检测的F值
	 */
	public double getDetectF();

	/**
	 * 纠正的准确率
	 * 
	 * @return 纠正的准确率
	 */
	public double getCorrectAccuracy();

	/**
	 * 纠正的精确率
	 * 
	 * @return 纠正的精确率
	 */
	public double getCorrectPrecision();

	/**
	 * 纠正的召回率
	 * 
	 * @return 纠正的召回率
	 */
	public double getCorrectRecall();

	/**
	 * 纠正的F值
	 * 
	 * @return 纠正的F值
	 */
	public double getCorrectF();

	/**
	 * 返回评价指标
	 * 
	 * @return 评价指标
	 */
	public String show();
}
