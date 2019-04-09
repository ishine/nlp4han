package com.lc.nlp4han.constituent.unlex;

import java.io.File;
import java.io.IOException;

import com.lc.nlp4han.constituent.ConstituentMeasure;
import com.lc.nlp4han.constituent.ConstituentTree;
import com.lc.nlp4han.constituent.PlainTextByTreeStream;
import com.lc.nlp4han.constituent.pcfg.ConstituentTreeStream;
import com.lc.nlp4han.constituent.pcfg.PCFG;
import com.lc.nlp4han.ml.util.CrossValidationPartitioner;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;
import com.lc.nlp4han.ml.util.ObjectStream;

/**
 * 交叉验证类
 * 
 * @author 王宁
 */
public class CrossValidatorParentTool
{

	public void evaluate(ObjectStream<String> sentenceStream, int nFolds, ConstituentMeasure measure,
			double pruneThreshold, boolean secondPrune, boolean prior) throws IOException
	{
		CrossValidationPartitioner<String> partitioner = new CrossValidationPartitioner<String>(sentenceStream, nFolds);
		int run = 1;
		double totalTime = 0;
		while (partitioner.hasNext())
		{
			System.out.println("Run" + run + "...");
			long start = System.currentTimeMillis();
			CrossValidationPartitioner.TrainingSampleStream<String> trainingSampleStream = partitioner.next();
			TreeBank treeBank = new TreeBank();
			String expression;
			while ((expression = trainingSampleStream.read()) != null)
				treeBank.addTree(expression, true);
			GrammarExtractor gExtractor = new GrammarExtractor();
			Grammar g = gExtractor.extractGrammarParentLabel(treeBank, Lexicon.DEFAULT_RAREWORD_THRESHOLD);
			PCFG pcfg = g.getPCFG();
			System.out.println("训练学习时间：" + (System.currentTimeMillis() - start) + "ms");
			
			long start2 = System.currentTimeMillis();
			EvaluatorParentLabel evaluator = new EvaluatorParentLabel(pcfg, pruneThreshold, secondPrune,
					prior);
			evaluator.setMeasure(measure);
			ObjectStream<ConstituentTree> sampleStream = new ConstituentTreeStream(
					trainingSampleStream.getTestSampleStream());
			evaluator.evaluate(sampleStream);
			
			System.out.println("解析评价时间：" + (System.currentTimeMillis() - start2) + "ms");
			totalTime += (System.currentTimeMillis() - start);

			System.out.println(measure);

			run++;
		}
		System.out.println("总体时间： " + totalTime + "ms");
	}

	private static void usage()
	{
		System.out.println(CrossValidatorParentTool.class.getName()
				+ " -train <corpusFile>  [-encoding <encoding>] [-folds <nFolds>] ");
	}

	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			usage();
			return;
		}
		String corpusFile = null;
		int folds = 10;
		String encoding = "utf-8";
		double pruneThreshold = 0.0001;
		boolean secondPrune = false;
		boolean prior = false;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-train"))
			{
				corpusFile = args[i + 1];
				i++;
			}
			if (args[i].equals("-encoding"))
			{
				encoding = args[i + 1];
				i++;
			}
			if (args[i].equals("-folds"))
			{
				folds = Integer.parseInt(args[i + 1]);
				i++;
			}
		}
		try
		{
			ObjectStream<String> sentenceStream = new PlainTextByTreeStream(
					new FileInputStreamFactory(new File(corpusFile)), encoding);

			ConstituentMeasure measure = new ConstituentMeasure();
			CrossValidatorParentTool crossValidator = new CrossValidatorParentTool();
			crossValidator.evaluate(sentenceStream, folds, measure, pruneThreshold, secondPrune, prior);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
