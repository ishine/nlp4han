package com.lc.nlp4han.chunk.svm;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import com.lc.nlp4han.chunk.AbstractChunkAnalysisMeasure;
import com.lc.nlp4han.chunk.AbstractChunkAnalysisSample;
import com.lc.nlp4han.chunk.ChunkAnalysisContextGenerator;
import com.lc.nlp4han.chunk.svm.liblinear.InvalidInputDataException;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosSampleStream;
import com.lc.nlp4han.ml.util.CrossValidationPartitioner;
import com.lc.nlp4han.ml.util.ObjectStream;

public class ChunkerSVMCrossValidation
{

	/**
	 * 训练的参数集
	 */
	private final String[] args;

	/**
	 * 构造方法
	 * 
	 * @param encoding
	 *            编码格式
	 * @param params
	 *            训练的参数
	 * @param monitor
	 *            监听器
	 */
	public ChunkerSVMCrossValidation(String[] args)
	{
		this.args = args;
	}

	/**
	 * n折交叉验证评估
	 * 
	 * @param sampleStream
	 *            样本流
	 * @param nFolds
	 *            折数
	 * @param contextGenerator
	 *            上下文生成器
	 * @param measure
	 *            组块分析评价器
	 * @throws IOException
	 * @throws InvalidInputDataException
	 */
	public void evaluate(ObjectStream<AbstractChunkAnalysisSample> sampleStream, int nFolds, ChunkerSVM chunker,
			ChunkAnalysisContextGenerator contextGenerator, AbstractChunkAnalysisMeasure measure, Properties properties)
			throws IOException, InvalidInputDataException
	{
		CrossValidationPartitioner<AbstractChunkAnalysisSample> partitioner = new CrossValidationPartitioner<AbstractChunkAnalysisSample>(
				sampleStream, nFolds);

		int run = 1;

		String modelPath = args[args.length - 1];

		// 小于折数的时候
		while (partitioner.hasNext())
		{
			System.out.println("Run" + run + "...");

			String label = ((ChunkerWordPosSampleStream) sampleStream).getScheme();
			CrossValidationPartitioner.TrainingSampleStream<AbstractChunkAnalysisSample> trainingSampleStream = partitioner
					.next();
			HashSet<String> dict = getDict(trainingSampleStream);
			trainingSampleStream.reset();
			measure.setDictionary(dict);

			chunker.setContextgenerator(contextGenerator);
			chunker.setLabel(label);
			
			long start = System.currentTimeMillis();
			chunker.train(trainingSampleStream, args, contextGenerator);		
			System.out.println("训练时间： " + (System.currentTimeMillis() - start));

			chunker.setModel(modelPath);
			ChunkerSVMEvaluator evaluator = new ChunkerSVMEvaluator(chunker, measure);
			evaluator.setMeasure(measure);

			start = System.currentTimeMillis();
			evaluator.evaluate(trainingSampleStream.getTestSampleStream());
			System.out.println("标注时间： " + (System.currentTimeMillis() - start));

			System.out.println(measure);
			run++;
		}

	}

	/**
	 * 获取词典
	 * 
	 * @param sampleStream
	 *            样本流
	 * @return 词典
	 * @throws IOException
	 */
	private HashSet<String> getDict(ObjectStream<AbstractChunkAnalysisSample> sampleStream) throws IOException
	{
		HashSet<String> dictionary = new HashSet<String>();
		AbstractChunkAnalysisSample sample = null;
		while ((sample = sampleStream.read()) != null)
		{
			String[] words = sample.getTokens();

			for (String word : words)
				dictionary.add(word);
		}

		return dictionary;
	}

}
