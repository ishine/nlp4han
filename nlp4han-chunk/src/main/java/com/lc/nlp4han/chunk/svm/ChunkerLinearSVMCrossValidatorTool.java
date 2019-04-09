package com.lc.nlp4han.chunk.svm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.lc.nlp4han.chunk.AbstractChunkAnalysisMeasure;
import com.lc.nlp4han.chunk.AbstractChunkAnalysisSample;
import com.lc.nlp4han.chunk.AbstractChunkSampleParser;
import com.lc.nlp4han.chunk.ChunkAnalysisContextGenerator;
import com.lc.nlp4han.chunk.ChunkAnalysisMeasureBIEO;
import com.lc.nlp4han.chunk.ChunkAnalysisMeasureBIEOS;
import com.lc.nlp4han.chunk.ChunkAnalysisMeasureBIO;
import com.lc.nlp4han.chunk.svm.liblinear.InvalidInputDataException;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosContextGeneratorConf;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosParserBIEO;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosParserBIEOS;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosParserBIO;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosSampleStream;
import com.lc.nlp4han.ml.util.MarkableFileInputStreamFactory;
import com.lc.nlp4han.ml.util.ObjectStream;
import com.lc.nlp4han.ml.util.PlainTextByLineStream;

public class ChunkerLinearSVMCrossValidatorTool
{
	private static final String USAGE = "Usage: ChunkAnalysisLinearLinearSVMCrossValidatorTool [options] -data training_set_file\n"
			+ "options:\n" + "-encoding encoding : set encoding\n" + "-label label : such as BIOE, BIOES\n"
			+ "-v n : n-fold cross validation mode(default 10)\n" + "-s type : set type of solver (default 1)%n"
			+ "  for multi-class classification%n" + "    0 -- L2-regularized logistic regression (primal)%n"
			+ "    1 -- L2-regularized L2-loss support vector classification (dual)%n"
			+ "    2 -- L2-regularized L2-loss support vector classification (primal)%n"
			+ "    3 -- L2-regularized L1-loss support vector classification (dual)%n"
			+ "    4 -- support vector classification by Crammer and Singer%n"
			+ "    5 -- L1-regularized L2-loss support vector classification%n"
			+ "    6 -- L1-regularized logistic regression%n" + "    7 -- L2-regularized logistic regression (dual)%n"
			+ "  for regression%n" + "   11 -- L2-regularized L2-loss support vector regression (primal)%n"
			+ "   12 -- L2-regularized L2-loss support vector regression (dual)%n"
			+ "   13 -- L2-regularized L1-loss support vector regression (dual)%n"
			+ "-c cost : set the parameter C (default 1)%n"
			+ "-p epsilon : set the epsilon in loss function of SVR (default 0.1)%n"
			+ "-e epsilon : set tolerance of termination criterion%n" + "   -s 0 and 2%n"
			+ "       |f'(w)|_2 <= eps*min(pos,neg)/l*|f'(w0)|_2,%n"
			+ "       where f is the primal function and pos/neg are # of%n"
			+ "       positive/negative data (default 0.01)%n" + "   -s 11%n"
			+ "       |f'(w)|_2 <= eps*|f'(w0)|_2 (default 0.001)%n" + "   -s 1, 3, 4 and 7%n"
			+ "       Dual maximal violation <= eps; similar to libsvm (default 0.1)%n" + "   -s 5 and 6%n"
			+ "       |f'(w)|_1 <= eps*min(pos,neg)/l*|f'(w0)|_1,%n"
			+ "       where f is the primal function (default 0.01)%n" + "   -s 12 and 13%n"
			+ "       |f'(alpha)|_1 <= eps |f'(alpha0)|,%n" + "       where f is the dual function (default 0.1)%n"
			+ "-B bias : if bias >= 0, instance x becomes [x; bias]; if < 0, no bias term added (default -1)%n"
			+ "-wi weight: weights adjust the parameter C of different classes (see README for details)%n"
			+ "-C : find parameter C (only for -s 0 and 2)%n" + "-q : quiet mode (no outputs)%n";

	public static void main(String[] args) throws IOException, InvalidInputDataException
	{
		int folds = 10;
		String scheme = "BIEO";
		String encoding = "UTF-8";
		String corpusFile = null;
		List<String> trainArgsList = new ArrayList<String>();

		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-data"))
			{
				corpusFile = args[i + 1];
				i++;
			}
			else if (args[i].equals("-v"))
			{
				folds = Integer.parseInt(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-label"))
			{
				scheme = args[i + 1];
				i++;
			}
			else if (args[i].equals("-encoding"))
			{
				encoding = args[i + 1];
				i++;
			}
			else if (args[i].equals("-s"))
			{
				trainArgsList.add(args[i]);
				trainArgsList.add(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-c"))
			{
				trainArgsList.add(args[i]);
				trainArgsList.add(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-p"))
			{
				trainArgsList.add(args[i]);
				trainArgsList.add(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-e"))
			{
				trainArgsList.add(args[i]);
				trainArgsList.add(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-B"))
			{
				trainArgsList.add(args[i]);
				trainArgsList.add(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-C"))
			{
				trainArgsList.add(args[i]);
			}
			else if (args[i].equals("-q"))
			{
				trainArgsList.add(args[i]);
			}
			else
			{
				if (args[i].startsWith("-w"))
				{
					trainArgsList.add(args[i]);
					trainArgsList.add(args[i + 1]);
					i++;
				}
				else
				{
					System.err.println(USAGE);
					System.exit(1);
				}
			}
		}

		if (corpusFile == null)
		{
			System.err.println(USAGE);
			System.exit(1);
		}

		final java.nio.file.Path docDir = java.nio.file.Paths.get(corpusFile);

		if (!java.nio.file.Files.isReadable(docDir))
		{
			System.out.println("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		trainArgsList.add(corpusFile + ".svm.cv"); // 临时样本文件
		trainArgsList.add(corpusFile + ".model.cv"); // 临时模型文件

		String[] trainArgs = trainArgsList.toArray(new String[trainArgsList.size()]);

		ObjectStream<String> lineStream = new PlainTextByLineStream(
				new MarkableFileInputStreamFactory(new File(corpusFile)), encoding);
		AbstractChunkSampleParser parse = null;
		AbstractChunkAnalysisMeasure measure = null;

		if (scheme.equals("BIEOS"))
		{
			parse = new ChunkerWordPosParserBIEOS();
			measure = new ChunkAnalysisMeasureBIEOS();
		}
		else if (scheme.equals("BIEO"))
		{
			parse = new ChunkerWordPosParserBIEO();
			measure = new ChunkAnalysisMeasureBIEO();
		}
		else
		{
			parse = new ChunkerWordPosParserBIO();
			measure = new ChunkAnalysisMeasureBIO();
		}

		ObjectStream<AbstractChunkAnalysisSample> sampleStream = new ChunkerWordPosSampleStream(lineStream, parse,
				scheme);
		Properties p = SVMSampleUtil.getDefaultConf();
		ChunkAnalysisContextGenerator contextGen = new ChunkerWordPosContextGeneratorConf(p);
		ChunkerSVMCrossValidation crossValidator = new ChunkerSVMCrossValidation(trainArgs);
		ChunkerLinearSVM chunker = new ChunkerLinearSVM();
		
		// 评价
		crossValidator.evaluate(sampleStream, folds, chunker, contextGen, measure, p);

		sampleStream.close();
		
		// 删除临时文件
		deleteFile(trainArgs[trainArgs.length - 1]);
		deleteFile(trainArgs[trainArgs.length - 2]);
	}

	private static void deleteFile(String filePath)
	{
		File file = new File(filePath);
		if (file.exists() && file.isFile())
		{
			file.delete();
		}
	}
}
