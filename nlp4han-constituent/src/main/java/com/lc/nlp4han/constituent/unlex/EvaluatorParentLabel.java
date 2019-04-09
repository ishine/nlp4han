package com.lc.nlp4han.constituent.unlex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.lc.nlp4han.constituent.ConstituentMeasure;
import com.lc.nlp4han.constituent.ConstituentParser;
import com.lc.nlp4han.constituent.ConstituentTree;
import com.lc.nlp4han.constituent.PlainTextByTreeStream;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.pcfg.ConstituentParserCKYLoosePCNF;
import com.lc.nlp4han.constituent.pcfg.ConstituentTreeStream;
import com.lc.nlp4han.constituent.pcfg.PCFG;
import com.lc.nlp4han.constituent.pcfg.UncompatibleGrammar;
import com.lc.nlp4han.constituent.TreeNodeUtil;
import com.lc.nlp4han.ml.util.Evaluator;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;
import com.lc.nlp4han.ml.util.ObjectStream;

/**
 * 带父节点标记的PCFG解析评价
 * 
 * @author 王宁
 * 
 */
public class EvaluatorParentLabel extends Evaluator<ConstituentTree>
{
	private ConstituentParser parser;

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

	public EvaluatorParentLabel(ConstituentParser cky)
	{
		this.parser = cky;
	}

	public EvaluatorParentLabel(PCFG p2nf, double pruneThreshold, boolean secondPrune, boolean prior) throws UncompatibleGrammar
	{
		this.parser = new ConstituentParserCKYLoosePCNF(p2nf, pruneThreshold, secondPrune, prior);
	}

	@Override
	protected ConstituentTree processSample(ConstituentTree sample)
	{
		TreeNode rootNodeRef = sample.getRoot();
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> poses = new ArrayList<String>();
		TreeUtil.addParentLabel(rootNodeRef);
		TreeNodeUtil.getWordsAndPOSFromTree(words, poses, rootNodeRef);
		TreeUtil.removeParentLabel(rootNodeRef);
		String[] words1 = new String[words.size()];
		String[] poses1 = new String[poses.size()];
		for (int i = 0; i < words.size(); i++)
		{
			words1[i] = words.get(i);
			poses1[i] = poses.get(i);
		}

		long start = System.currentTimeMillis();

		ConstituentTree treePre = parser.parse(words1, poses1);
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
				TreeBinarization.unbinarize(treePre.getRoot());
				measure.update(rootNodeRef, TreeUtil.removeParentLabel(treePre.getRoot()));
			}
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}

		return treePre;
	}

	private static void usage()
	{
		System.out.println(EvaluatorParentLabel.class.getName() + "\n"
				+ "-train <trainFile> -gold <goldFile> [-trainEncoding <trainEncoding>] [-goldEncoding <trainEncoding>] [-em <emIterations>]");
	}

	public static void eval(String trainF, String goldF, String trainEn, String goldEn, int iterations,
			double pruneThreshold, boolean secondPrune, boolean prior) throws IOException
	{
		long start = System.currentTimeMillis();
		Grammar g = GrammarExtractorParentTool.getGrammar(trainF, trainEn, Lexicon.DEFAULT_RAREWORD_THRESHOLD);
		long end = System.currentTimeMillis();
		EvaluatorParentLabel evaluator = new EvaluatorParentLabel(g.getPCFG(), pruneThreshold, secondPrune,
				prior);
		ConstituentMeasure measure = new ConstituentMeasure();
		evaluator.setMeasure(measure);
		ObjectStream<String> treeStream = new PlainTextByTreeStream(new FileInputStreamFactory(new File(goldF)),
				goldEn);
		ObjectStream<ConstituentTree> sampleStream = new ConstituentTreeStream(treeStream);
		evaluator.evaluate(sampleStream);

		ConstituentMeasure measureRes = evaluator.getMeasure();
		System.out.println(end - start);
		System.out.println(measureRes);
	}

	public static void main(String[] args)
	{
		String trainFilePath = null;
		String goldFilePath = null;
		String trainEncoding = "utf-8";
		String goldEncoding = "utf-8";
		double pruneThreshold = 0.0001;
		boolean secondPrune = false;
		boolean prior = false;
		int iterations = 50;// em算法迭代次数
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-train"))
			{
				trainFilePath = args[i + 1];
				i++;
			}
			if (args[i].equals("-gold"))
			{
				goldFilePath = args[i + 1];
				i++;
			}
			if (args[i].equals("-trainEncoding"))
			{
				trainEncoding = args[i + 1];
				i++;
			}
			if (args[i].equals("-goldEncoding"))
			{
				goldEncoding = args[i + 1];
				i++;
			}
			if (args[i].equals("-em"))
			{
				iterations = Integer.parseInt(args[i + 1]);
				i++;
			}
			if (args[i].equals("-pruneThreshold"))
			{
				pruneThreshold = Double.parseDouble(args[i + 1]);
				i++;
			}
			if (args[i].equals("-secondPrune"))
			{
				secondPrune = Boolean.getBoolean(args[i + 1]);
				i++;
			}
			if (args[i].equals("-prior"))
			{
				prior = Boolean.getBoolean(args[i + 1]);
				i++;
			}
		}
		if (trainFilePath == null || goldFilePath == null)
		{
			usage();
			System.exit(0);
		}
		try
		{
			eval(trainFilePath, goldFilePath, trainEncoding, goldEncoding, iterations, pruneThreshold, secondPrune,
					prior);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
