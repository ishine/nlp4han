package com.lc.nlp4han.constituent.unlex;

import java.io.File;
import java.io.IOException;

import com.lc.nlp4han.constituent.ConstituentMeasure;
import com.lc.nlp4han.constituent.ConstituentParser;
import com.lc.nlp4han.constituent.ConstituentTree;
import com.lc.nlp4han.constituent.PlainTextByTreeStream;
import com.lc.nlp4han.constituent.pcfg.ConstituentParserCKYLoosePCNF;
import com.lc.nlp4han.constituent.pcfg.ConstituentTreeStream;
import com.lc.nlp4han.constituent.pcfg.PCFG;
import com.lc.nlp4han.ml.util.CrossValidationPartitioner;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;
import com.lc.nlp4han.ml.util.ObjectStream;

public class CrossValidatorLatentSimple
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
			TreeBank treeBank2 = new TreeBank();
			String expression;
			while ((expression = trainingSampleStream.read()) != null)
			{
				treeBank.addTree(expression, false);
				treeBank2.addTree(expression, false);
			}
			
			GrammarExtractor gExtractor = new GrammarExtractor();
			Grammar g = gExtractor.extractLatentGrammar(treeBank, Lexicon.DEFAULT_RAREWORD_THRESHOLD, 0, 50,
					0.5, 0.01);
			PCFG pcfg = g.getPCFG();
			GrammarExtractor gExtractor2 = new GrammarExtractor();
			Grammar gLatent = gExtractor2.extractLatentGrammar(treeBank2, Lexicon.DEFAULT_RAREWORD_THRESHOLD,
					1, 50, 0.5, 0.01);
			System.out.println("训练学习时间：" + (System.currentTimeMillis() - start) + "ms");
			
			long start2 = System.currentTimeMillis();
			ConstituentParserCKYLoosePCNF pcfgParser = new ConstituentParserCKYLoosePCNF(pcfg, pruneThreshold, secondPrune,
					prior);
			ConstituentParser parser = new ConstituentParserLatentSimple(pcfgParser, gLatent);
			EvaluatorLatentSimple evaluator = new EvaluatorLatentSimple(parser);
			evaluator.setMeasure(measure);
			ObjectStream<ConstituentTree> sampleStream = new ConstituentTreeStream(
					trainingSampleStream.getTestSampleStream());
			evaluator.evaluate(sampleStream);
			System.out.println("解析评价时间：" + (System.currentTimeMillis() - start2) + "ms");
			totalTime += (System.currentTimeMillis() - start);

			System.out.println(measure);
			g = null;
			pcfg = null;
			treeBank = null;
			gExtractor = null;
			run++;
		}
		System.out.println("总体时间： " + totalTime + "ms");
	}

	private static void usage()
	{
		System.out.println(CrossValidatorLatentSimple.class.getName()
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
			CrossValidatorLatentSimple crossValidator = new CrossValidatorLatentSimple();
			crossValidator.evaluate(sentenceStream, folds, measure, pruneThreshold, secondPrune, prior);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
