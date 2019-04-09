package com.lc.nlp4han.chunk.svm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.lc.nlp4han.chunk.AbstractChunkAnalysisMeasure;
import com.lc.nlp4han.chunk.AbstractChunkAnalysisSample;
import com.lc.nlp4han.chunk.AbstractChunkSampleParser;
import com.lc.nlp4han.chunk.ChunkAnalysisContextGenerator;
import com.lc.nlp4han.chunk.ChunkAnalysisErrorPrinter;
import com.lc.nlp4han.chunk.ChunkAnalysisEvaluateMonitor;
import com.lc.nlp4han.chunk.ChunkAnalysisMeasureBIEO;
import com.lc.nlp4han.chunk.ChunkAnalysisMeasureBIEOS;
import com.lc.nlp4han.chunk.ChunkAnalysisMeasureBIO;
import com.lc.nlp4han.chunk.svm.libsvm.svm_model;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosContextGeneratorConf;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosParserBIEO;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosParserBIEOS;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosParserBIO;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosSampleStream;
import com.lc.nlp4han.ml.util.MarkableFileInputStreamFactory;
import com.lc.nlp4han.ml.util.ObjectStream;
import com.lc.nlp4han.ml.util.PlainTextByLineStream;

public class ChunkerSVMEvalTool
{
	private static final String USAGE = "Usage: ChunkAnalysisSVMEvalTool [options] -goal predicting_set_file\n"
			+ "options:\n" + "-label label : such as BIOE, BIOES\n" + "-encoding encoding : set encoding form\n"
			+ "-model model_file : set model file from Disk \n"
			+ "-rmodel model_file : set model file from Resources"
			+ "-transform transformation_file : set transformation file, end with '.info' \n"
			+ "-error error_messages_file : output error messages\n";

	public static void eval(Object model, String goldFile, String path, String encoding, File errorFile,
			ChunkerSVM tagger, AbstractChunkSampleParser parse, AbstractChunkAnalysisMeasure measure, String label)
			throws IOException
	{
		long start = System.currentTimeMillis();

		ChunkAnalysisContextGenerator contextGen = new ChunkerWordPosContextGeneratorConf();
		tagger.setContextgenerator(contextGen);
		tagger.setLabel(label);
		tagger.setSVMStandardInput(path);
		
		tagger.setModel(model);
		
		ChunkerSVMEvaluator evaluator = null;

		if (errorFile != null)
		{
			ChunkAnalysisEvaluateMonitor errorMonitor = new ChunkAnalysisErrorPrinter(new FileOutputStream(errorFile));
			evaluator = new ChunkerSVMEvaluator(tagger, measure, errorMonitor);
		}
		else
			evaluator = new ChunkerSVMEvaluator(tagger);

		evaluator.setMeasure(measure);

		ObjectStream<String> goldStream = new PlainTextByLineStream(
				new MarkableFileInputStreamFactory(new File(goldFile)), encoding);
		ObjectStream<AbstractChunkAnalysisSample> testStream = new ChunkerWordPosSampleStream(goldStream, parse, label);

		start = System.currentTimeMillis();
		evaluator.evaluate(testStream);
		System.out.println("标注时间： " + (System.currentTimeMillis() - start));

		System.out.println(evaluator.getMeasure());
	}

	public static void main(String[] args) throws IOException
	{
		String usage = USAGE;
		String encoding = "utf-8";
		String scheme = "BIEOS";
		String transformationFile = null;
		String modelpath = null;
		String resourceModel = null;
		String errorFile = null;
		String goldFile = null;
		

		for (int i = 0; i < args.length; i++)
		{
			if ("-encoding".equals(args[i]))
			{
				encoding = args[i + 1];
				i++;
			}
			else if ("-label".equals(args[i]))
			{
				scheme = args[i + 1];
				i++;
			}
			else if ("-transform".equals(args[i]))
			{
				transformationFile = args[i + 1];
				i++;
			}
			else if ("-model".equals(args[i]))
			{
				modelpath = args[i + 1];
				i++;
			}
			else if ("-rmodel".equals(args[i]))
			{
				resourceModel = args[i + 1];
				i++;
			}
			else if ("-error".equals(args[i]))
			{
				errorFile = args[i + 1];
				i++;
			}
			else if ("-gold".equals(args[i]))
			{
				goldFile = args[i + 1];
				i++;
			}
			else
			{
				System.err.println(usage);
				System.exit(1);
			}
		}

		if (goldFile == null)
		{
			System.err.println(usage);
			System.exit(1);
		}

		final java.nio.file.Path docDir = java.nio.file.Paths.get(goldFile);

		if (!java.nio.file.Files.isReadable(docDir))
		{
			System.out.println("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}
		
		svm_model model = null;
		if (resourceModel != null && modelpath != null)
		{
			System.out.println("\"-model\" and \"-rmodel\" can only exist one ");
			System.exit(1);
		}
		else if (modelpath != null)
		{
			model = ModelLoadingUtil.loadLibSVMModelFromDisk(modelpath);
		}
		else
		{
			model = ModelLoadingUtil.loadLibSVMModelFromResources(resourceModel);
		}

		AbstractChunkSampleParser parse;
		AbstractChunkAnalysisMeasure measure;
		ChunkerLibSVM tagger = new ChunkerLibSVM();

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

		if (errorFile != null)
			eval(model, goldFile, transformationFile, encoding, new File(errorFile), tagger, parse, measure,
					scheme);
		else
			eval(model, goldFile, transformationFile, encoding, null, tagger, parse, measure, scheme);

	}
}
