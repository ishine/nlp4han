package com.lc.nlp4han.chunk.svm;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.lc.nlp4han.chunk.AbstractChunkAnalysisSample;
import com.lc.nlp4han.chunk.Chunk;
import com.lc.nlp4han.chunk.ChunkAnalysisContextGenerator;
import com.lc.nlp4han.chunk.Chunker;
import com.lc.nlp4han.chunk.svm.liblinear.InvalidInputDataException;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosSample;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosSampleEvent;
import com.lc.nlp4han.ml.model.Event;
import com.lc.nlp4han.ml.util.ObjectStream;

/**
 * 基于词和词性的组块分析模型训练类，用于SVM框架
 * 
 * @author 杨智超
 *
 */
public abstract class ChunkerSVM implements Chunker
{
	private SVMFeatureLabelInfo ci = null;
	
	private ChunkAnalysisContextGenerator contextgenerator;
	private String label;

	public ChunkerSVM()
	{

	}

	public ChunkerSVM(ChunkAnalysisContextGenerator contextgenerator, String label)
	{
		super();
		this.contextgenerator = contextgenerator;
		this.label = label;
	}

	public ChunkerSVM(ChunkAnalysisContextGenerator contextgenerator, String filePath, String encoding, String label) throws IOException
	{
		this(contextgenerator, label);

		ci = new SVMFeatureLabelInfo(filePath, encoding);

	}

	public abstract void setModel(Object model);

	public abstract Object getModel();

	/**
	 * 设置数据转换信息SVMStandardInput
	 * 
	 * @param ssi
	 */
	public void setSVMStandardInput(SVMFeatureLabelInfo ci)
	{
		this.ci = ci;
	}

	/**
	 * 读取组块文件，生成数据转换信息
	 * 
	 * @param filePath
	 * @throws IOException 
	 */
	public void setSVMStandardInput(String filePath) throws IOException
	{

		this.ci = new SVMFeatureLabelInfo(filePath, "utf-8");

	}

	public void setContextgenerator(ChunkAnalysisContextGenerator contextgenerator)
	{
		this.contextgenerator = contextgenerator;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * 根据模型路径，加载model
	 * 
	 * @param modelPath
	 * @throws IOException 
	 */
	public abstract void setModel(String modelPath) throws IOException;
	
	
	/**
	 * 根据输入流，设置model
	 * @param input
	 */
	public abstract void setModel(InputStream input) throws IOException;

	@Override
	public Chunk[] parse(String sentence)
	{
		String[] wordTags = sentence.split(" +");
		List<String> words = new ArrayList<>();
		List<String> poses = new ArrayList<>();

		for (String wordTag : wordTags)
		{
			words.add(wordTag.split("/")[0]);
			poses.add(wordTag.split("/")[1]);
		}

		String[] chunkTypes = null;
		try
		{
			chunkTypes = tag(words.toArray(new String[words.size()]), poses.toArray(new String[poses.size()]));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		AbstractChunkAnalysisSample sample = new ChunkerWordPosSample(words.toArray(new String[words.size()]),
				poses.toArray(new String[poses.size()]), chunkTypes);
		sample.setTagScheme(label);

		return sample.toChunk();
	}

	@Override
	public Chunk[][] parse(String sentence, int k)
	{
		return new Chunk[][] { parse(sentence) };
	}

	/**
	 * 返回给定词组和词性的组块类型
	 * 
	 * @param words
	 *            待确定组块类型的词组
	 * @param poses
	 *            词组对应的词性数组
	 * @return 组块类型
	 */
	public String[] tag(String[] words, String[] poses) throws IOException
	{
		String[] chunkTags = new String[words.length];
		String line = null;

		for (int i = 0; i < words.length; i++)
		{
			String[] context = contextgenerator.getContext(i, words, chunkTags, poses);

			line = "1 " + SVMSampleUtil.toSVMSample(context, ci); // <label> <index1>:<value1> <index2>:<value2>
																	// ...；预测时，label可以为任意值

			String tag = predict(line, getModel());
			chunkTags[i] = tag;

		}
		return chunkTags;
	}

	/**
	 * 根据模型model，调用libsvm预测line的结果，line为libsvm的标准输入格式，形如 2 4:5 7:3 8:2....
	 */
	public String predict(String line, Object model) throws IOException
	{
		double v = predictOneLine(line, model);
		String result = transform(String.valueOf(v));
		return result;
	}

	/**
	 * 调用model，预测结果
	 * 
	 * @param line
	 *            line为libsvm的标准输入格式，形如 2 4:5 7:3 8:2....
	 * @param model
	 *            模型
	 * @return 分类结果，数字类型
	 * @throws IOException
	 */
	public abstract double predictOneLine(String line, Object model) throws IOException;

	/**
	 * 将libsvm预测的结果（数字）转换成组块标注
	 */
	private String transform(String v)
	{
		int t = str2int(v);
		String result = ci.getClassLabel(t);
		return result;
	}

	private int str2int(String str)
	{
		return Integer.valueOf(str.trim().split("\\.")[0]);
	}

	/**
	 * 训练模型
	 * 
	 * @param sampleStream
	 *            文件流
	 * @param arg
	 *            训练参数
	 * @param contextGen
	 *            特征生成器
	 * @throws IOException
	 * @throws InvalidInputDataException
	 */
	public void train(ObjectStream<AbstractChunkAnalysisSample> sampleStream, String[] arg,
			ChunkAnalysisContextGenerator contextGen) throws IOException, InvalidInputDataException
	{
		generateSVMSamples(sampleStream, arg, contextGen);

		train(arg);
	}

	private void generateSVMSamples(ObjectStream<AbstractChunkAnalysisSample> sampleStream, String[] arg,
			ChunkAnalysisContextGenerator contextGen) throws RuntimeException, IOException
	{
		ObjectStream<Event> es = new ChunkerWordPosSampleEvent(sampleStream, contextGen);
		
		this.ci = new SVMFeatureLabelInfo(es);

		es.reset();
		String[] input = SVMSampleUtil.toSVMSamples(es, ci);

		saveFile(arg[arg.length - 2], input, "utf-8");
	}

	/**
	 * 根据训练参数进行训练
	 * 
	 * @param arg
	 * @throws IOException 
	 */
	public abstract void train(String[] arg) throws IOException;

	private void saveFile(String saveFilePath, String[] datum, String encoding) throws IOException
	{
		BufferedWriter bw = null;

		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFilePath), encoding));
		for (int i = 0; i < datum.length; i++)
		{
			bw.write(datum[i]);
			bw.write("\n");
		}

		bw.flush();
		bw.close();

	}
}
