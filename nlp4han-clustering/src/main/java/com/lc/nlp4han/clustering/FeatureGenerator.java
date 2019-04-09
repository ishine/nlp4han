package com.lc.nlp4han.clustering;

import java.util.List;
import java.util.Map;

public interface FeatureGenerator
{
	/**
	 * 根据所有文本，进行初始化，收集文本信息，用于生成Feature
	 * @param texts 所有文本
	 */
	public void init(List<Text> texts);
	
	/**
	 * 生成文本的特征
	 * @param text 文本
	 * @return 生成的特征列表
	 */
	public List<Feature> getFeatures(Text text);
	
	/**
	 * 是否已初始化
	 * @return 
	 */
	public boolean isInitialized();
	
	/**
	 * 获取从所有文本中获取的相关信息
	 * @return
	 */
	public Map<String, Count> getTextsInfo();
}
