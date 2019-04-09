package com.lc.nlp4han.srl.propbank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.ml.model.Event;
import com.lc.nlp4han.ml.util.AbstractEventStream;
import com.lc.nlp4han.ml.util.ObjectStream;

/**
 * 为一步生成的模型生成事件
 * @author 王馨苇
 *
 */
public class SRLEventStreamForOneStep extends AbstractEventStream<SRLSample<HeadTreeNode>>{
	
	private SRLContextGenerator generator;
	
	/**
	 * 构造
	 * @param samples 样本流
	 * @param generator 上下文产生器
	 */
	public SRLEventStreamForOneStep(ObjectStream<SRLSample<HeadTreeNode>> samples, SRLContextGenerator generator) {
		super(samples);
		this.generator = generator;
	}

	/**
	 * 生成事件
	 */
	@Override
	protected Iterator<Event> createEvents(SRLSample<HeadTreeNode> sample) {
		String[] labelinfo = sample.getLabelInfo();
		TreeNodeWrapper<HeadTreeNode>[] argumenttree = sample.getArgumentTree();
		TreeNodeWrapper<HeadTreeNode>[] predicatetree = sample.getPredicateTree();
		String[][] ac = sample.getAdditionalContext();
		List<Event> events = generateEvents(argumenttree, predicatetree, labelinfo, ac);
        return events.iterator();
	}

	/**
	 * 事件生成
	 * @param argumenttree 以论元为根节点的树
	 * @param predicatetree 以谓词为根节点的树
	 * @param labelinfo 标记序列
	 * @param ac
	 * @return
	 */
	private List<Event> generateEvents(TreeNodeWrapper<HeadTreeNode>[] argumenttree, TreeNodeWrapper<HeadTreeNode>[] predicatetree, String[] labelinfo, String[][] ac) {

		List<Event> events = new ArrayList<Event>(labelinfo.length);
		for (int i = 0; i < labelinfo.length; i++) {
			String[] context = generator.getContext(i, argumenttree, labelinfo, predicatetree);
			events.add(new Event(labelinfo[i], context));
		}
		return events;
	}
}

