package com.lc.nlp4han.srl.propbank;

import java.util.Arrays;
import java.util.List;

import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.ml.util.Evaluator;

/**
 * 基于分步训练模型的评估类(包含NULL标签)
 * @author 王馨苇
 *
 */
public class SRLEvaluatorForByStepWithNull extends Evaluator<SRLSample<HeadTreeNode>>{

	private SRLMEForIdentification taggerIden;
	private SRLMEForClassificationWithNull taggerClas;
	private SRLMeasure measure;
	
	public SRLEvaluatorForByStepWithNull(SRLMEForIdentification taggerIden, SRLMEForClassificationWithNull taggerClas) {
		this.taggerIden = taggerIden;
		this.taggerClas = taggerClas;
	}
	
	public SRLEvaluatorForByStepWithNull(SRLMEForIdentification taggerIden, SRLMEForClassificationWithNull taggerClas, SRLEvaluateMonitor... evaluateMonitors) {
		super(evaluateMonitors);
		this.taggerIden = taggerIden;
		this.taggerClas = taggerClas;
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
		String[] labelinfoRef = PostTreatmentUtil.NULL_1012NULL(sample.getLabelInfo());
//		for (int i = 0; i < labelinfoRef.length; i++) {
//			System.out.print(labelinfoRef[i]+"\t");
//		}
//		System.out.println();
		String[] labelinfoIdenRef = PostTreatmentUtil.NULL_1012NULL(sample.getIdentificationLabelInfo());
//		for (int i = 0; i < labelinfoIdenRef.length; i++) {
//			System.out.print(labelinfoIdenRef[i]+"\t");
//		}
//		System.out.println();
		String[] labelinfoIdenPre = PostTreatmentUtil.NULL_1012NULL(taggerIden.tag(argumenttree, predicatetree));
//		for (int i = 0; i < labelinfoIdenPre.length; i++) {
//			System.out.print(labelinfoIdenPre[i]+"\t");
//		}
//		System.out.println();
		measure.updateForIden(labelinfoIdenRef, labelinfoIdenPre);
		List<Integer> index = SRLSample.filterNotNULLLabelIndex(labelinfoIdenPre);
		TreeNodeWrapper<HeadTreeNode>[] argumenttreefortest = SRLSample.getArgumentTreeFromIndex(argumenttree, index);
		String[] labelinfoClasRef = SRLSample.getLabelFromIndex(labelinfoRef, index);
		String[] labelinfoClasPre = taggerClas.tag(argumenttreefortest, predicatetree);
//		for (int i = 0; i < labelinfoClasRef.length; i++) {
//			System.out.print(labelinfoClasRef[i] + "\t");
//		}
//		System.out.println();
//		for (int i = 0; i < labelinfoClasPre.length; i++) {
//			System.out.print(labelinfoClasPre[i] + "\t");
//		}
//		System.out.println();
		measure.updateForClas(labelinfoClasRef, labelinfoClasPre);
		String[] labelinfoPre = new String[labelinfoRef.length];
		for (int i = 0; i < labelinfoPre.length; i++) {
			labelinfoPre[i] = "NULL";
		}
		for (int i = 0; i < index.size(); i++) {
			labelinfoPre[index.get(i)] = labelinfoClasPre[i];
		}
//		for (int i = 0; i < labelinfoPre.length; i++) {
//			System.out.print(labelinfoPre[i]+"\t");
//		}
//		System.out.println();
//		System.out.println();
		measure.update(labelinfoRef, labelinfoPre);
		SRLSample<HeadTreeNode> newsample = new SRLSample<HeadTreeNode>(node, Arrays.asList(argumenttree), Arrays.asList(predicatetree), Arrays.asList(labelinfoPre));
		return newsample;
	}
}
