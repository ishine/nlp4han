package com.lc.nlp4han.constituent.unlex;

import java.io.File;
import java.io.IOException;

import com.lc.nlp4han.constituent.ConstituentMeasure;
import com.lc.nlp4han.constituent.ConstituentTree;
import com.lc.nlp4han.constituent.PlainTextByTreeStream;
import com.lc.nlp4han.constituent.pcfg.ConstituentTreeStream;
import com.lc.nlp4han.ml.util.CrossValidationPartitioner;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;
import com.lc.nlp4han.ml.util.ObjectStream;

/**
 * @author 王宁
 */
public class CrossValidatorUnlexTool
{
	public void evaluate(ObjectStream<String> sentenceStream, int nFolds, ConstituentMeasure measure, int SMCycle,
			double mergeRate, int EMIterations, double smooth, double pruneThreshold, boolean secondPrune,
			boolean prior) throws IOException
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
				treeBank.addTree(expression, false);
			
			GrammarExtractor gExtractor = new GrammarExtractor();
			Grammar gLatent = gExtractor.extractLatentGrammar(treeBank, Lexicon.DEFAULT_RAREWORD_THRESHOLD,
					SMCycle, EMIterations, mergeRate, smooth);
			
			System.out.println("训练学习时间：" + (System.currentTimeMillis() - start) + "ms");
			
			long start2 = System.currentTimeMillis();
			ConstituentParserUnlex parser = new ConstituentParserUnlex(gLatent,
					pruneThreshold, secondPrune, prior);
			EvaluatorUnlex evaluator = new EvaluatorUnlex(parser);
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
		System.out.println(CrossValidatorUnlexTool.class.getName()
				+ " -train <corpusFile> [-sm <SMCycle>] [-em<EMIterations>] [-merge <mergeRate>] [-smooth <smoothRate>] [-encoding <encoding>] [-folds <nFolds>] ");
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
		int SMCycle = 1;
		int EMIterations = 50;
		double mergeRate = 0.5;
		double smoothRate = 0.01;
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
			if (args[i].equals("-sm"))
			{
				SMCycle = Integer.parseInt(args[i + 1]);
				i++;
			}
			if (args[i].equals("-em"))
			{
				EMIterations = Integer.parseInt(args[i + 1]);
				i++;
			}
			if (args[i].equals("-smooth"))
			{
				smoothRate = Double.parseDouble(args[i + 1]);
				i++;
			}
			if (args[i].equals("-merge"))
			{
				mergeRate = Double.parseDouble(args[i + 1]);
			}
		}
		
		try
		{
			ObjectStream<String> sentenceStream = new PlainTextByTreeStream(
					new FileInputStreamFactory(new File(corpusFile)), encoding);
			
			ConstituentMeasure measure = new ConstituentMeasure();
			
			CrossValidatorUnlexTool crossValidator = new CrossValidatorUnlexTool();
			
			crossValidator.evaluate(sentenceStream, folds, measure, SMCycle, mergeRate, EMIterations, smoothRate,
					pruneThreshold, secondPrune, prior);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
