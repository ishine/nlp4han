package com.lc.nlp4han.srl.propbank;

import java.util.Arrays;

import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.ml.util.Evaluator;
import com.lc.nlp4han.ml.util.Sequence;

/**
 * 基于一步训练模型的评估类
 * @author 王馨苇
 *
 */
public class SRLEvaluatorForOneStep extends Evaluator<SRLSample<HeadTreeNode>>{

	private SRLMEForOneStep tagger;
	private SRLMeasure measure;
	
	public SRLEvaluatorForOneStep(SRLMEForOneStep tagger) {
		this.tagger = tagger;
	}
	
	public SRLEvaluatorForOneStep(SRLMEForOneStep tagger, SRLEvaluateMonitor... evaluateMonitors) {
		super(evaluateMonitors);
		this.tagger = tagger;
	}
	
	/**
	 * 设置评估指标的对象
	 * @param measure 评估指标计算的对象
	 */
	public void setMeasure(SRLMeasure measure){
		this.measure = measure;
	}
	
	/**
	 * 得到评估的指标
	 * @return
	 */
	public SRLMeasure getMeasure(){
		return this.measure;
	}

	@Override
	protected SRLSample<HeadTreeNode> processSample(SRLSample<HeadTreeNode> sample) {
		HeadTreeNode node = sample.getTree();
		TreeNodeWrapper<HeadTreeNode>[] argumenttree = sample.getArgumentTree();
		TreeNodeWrapper<HeadTreeNode>[] predicatetree = sample.getPredicateTree();
		String[] labelinfo = sample.getLabelInfo();
		String[] labelinforef = PostTreatmentUtil.NULL_1012NULL(labelinfo);
//		for (int i = 0; i < labelinforef.length; i++) {
//			if(!labelinfo[i].equals(labelinforef[i])){
//				System.out.print(labelinforef[i]);
//			}
//		}
//		System.out.println();
		Sequence result = tagger.topSequences(argumenttree, predicatetree);
		String[] newlabelinfo = null;
		newlabelinfo = result.getOutcomes().toArray(new String[result.getOutcomes().size()]);
		if(sample.getIsPruning() == true){
			newlabelinfo = PostTreatmentUtil.postTreat(argumenttree, result, PostTreatmentUtil.getSonTreeCount(predicatetree[0].getTree().getParent()));
		}else{
			newlabelinfo = PostTreatmentUtil.postTreat(argumenttree, result, argumenttree.length);
		}
//
//		for (int i = 0; i < newlabelinfo.length; i++) {
//			System.out.print(newlabelinfo[i]);
//		}
//		System.out.println();
		measure.update(labelinforef, newlabelinfo);
		SRLSample<HeadTreeNode> newsample = new SRLSample<HeadTreeNode>(node, Arrays.asList(argumenttree), Arrays.asList(predicatetree), Arrays.asList(newlabelinfo));
		return newsample;
	}
}
