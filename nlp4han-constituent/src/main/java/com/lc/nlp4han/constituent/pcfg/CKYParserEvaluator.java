package com.lc.nlp4han.constituent.pcfg;

import java.util.ArrayList;

import com.lc.nlp4han.constituent.ConstituentMeasure;
import com.lc.nlp4han.constituent.ConstituentParser;
import com.lc.nlp4han.constituent.ConstituentTree;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreeNodeUtil;
import com.lc.nlp4han.constituent.lex.ConstituentParseLexPCFG;
import com.lc.nlp4han.constituent.lex.LexPCFG;
import com.lc.nlp4han.ml.util.Evaluator;

public class CKYParserEvaluator extends Evaluator<ConstituentTree>
{
	/**
	 * 句法分析模型得到一颗句法树
	 */
	private ConstituentParser cky;

	/**
	 * 句法树中的短语分析评估
	 */
	private ConstituentMeasure measure;

	private long count = 0;
	private long totalTime = 0;

	public ConstituentMeasure getMeasure()
	{
		return measure;
	}

	public void setMeasure(ConstituentMeasure measure)
	{
		this.measure = measure;
	}

	public CKYParserEvaluator(ConstituentParser cky)
	{
		this.cky = cky;
	}

	public CKYParserEvaluator(LexPCFG lexpcfg, double pruneThreshold, boolean secondPrune,boolean prior)
	{
		this.cky = new ConstituentParseLexPCFG(lexpcfg,pruneThreshold,secondPrune,prior);
	}

	@Override
	protected ConstituentTree processSample(ConstituentTree sample)
	{
		TreeNode rootNodeRef = sample.getRoot();
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> poses = new ArrayList<String>();
		TreeNodeUtil.getWordsAndPOSFromTree(words, poses, rootNodeRef);

		String[] words1 = new String[words.size()];
		String[] poses1 = new String[poses.size()];
		for (int i = 0; i < words.size(); i++)
		{
			words1[i] = words.get(i);
			poses1[i] = poses.get(i);
		}

		long start = System.currentTimeMillis();

		ConstituentTree treePre = cky.parse(words1, poses1);

		long thisTime = System.currentTimeMillis() - start;
		totalTime += thisTime;
		count++;

		System.out.println(
				"句子长度：" + words.size() + " 平均解析时间：" + (totalTime / count) + "ms" + " 本句解析时间：" + thisTime + "ms");

		try
		{
			if (treePre == null)
			{
				System.out.println("无法解析的句子： " + rootNodeRef.toString());
				measure.countNodeDecodeTrees(null);
				measure.update(rootNodeRef, new TreeNode());
			}
			else
			{
				measure.update(rootNodeRef, treePre.getRoot());
			}
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}

		return treePre;
	}

}
