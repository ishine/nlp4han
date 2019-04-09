package com.lc.nlp4han.chunk.svm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.lc.nlp4han.chunk.AbstractChunkAnalysisSample;
import com.lc.nlp4han.chunk.AbstractChunkSampleParser;
import com.lc.nlp4han.chunk.ChunkAnalysisContextGenerator;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosContextGeneratorConf;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosParserBIEO;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosParserBIEOS;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosParserBIO;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosSampleEvent;
import com.lc.nlp4han.chunk.wordpos.ChunkerWordPosSampleStream;
import com.lc.nlp4han.ml.model.Event;
import com.lc.nlp4han.ml.util.MarkableFileInputStreamFactory;
import com.lc.nlp4han.ml.util.ObjectStream;
import com.lc.nlp4han.ml.util.PlainTextByLineStream;
import com.lc.nlp4han.ml.util.StringInputStreamFactory;

public class SVMSampleUtil
{
	/**
	 * 获取事件流
	 * 
	 * @param path
	 *            组块语料文件路径
	 * @param scheme
	 *            组块标记格式，BIEO，BIEOS等
	 * @param encoding
	 *            训练文件编码格式
	 * @param properties
	 *            特征配置文件
	 * @return 事件流
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static ObjectStream<Event> getEventStream(String path, String encoding, String scheme, Properties properties)
			throws FileNotFoundException, IOException
	{
		ObjectStream<String> lineStream = new PlainTextByLineStream(new MarkableFileInputStreamFactory(new File(path)),
				encoding);

		return getEventStream(lineStream, scheme, properties);
	}

	/**
	 * 获取事件流
	 * 
	 * @param content
	 *            字符串内容
	 * @param scheme
	 *            组块标记格式，BIEO，BIEOS等
	 * @param properties
	 *            特征配置文件
	 * @return 事件流
	 * @throws IOException
	 */
	public static ObjectStream<Event> getEventStream(String content, String scheme, Properties properties)
			throws IOException
	{
		ObjectStream<String> lineStream = new PlainTextByLineStream(new StringInputStreamFactory(content), "UTF8");

		return getEventStream(lineStream, scheme, properties);
	}

	private static ObjectStream<Event> getEventStream(ObjectStream<String> os, String scheme, Properties properties)
			throws IOException
	{
		AbstractChunkSampleParser parse = null;

		if (scheme.equals("BIEOS"))
			parse = new ChunkerWordPosParserBIEOS();
		else if (scheme.equals("BIEO"))
			parse = new ChunkerWordPosParserBIEO();
		else
			parse = new ChunkerWordPosParserBIO();

		ObjectStream<AbstractChunkAnalysisSample> sampleStream = new ChunkerWordPosSampleStream(os, parse, scheme);
		ChunkAnalysisContextGenerator contextGen = new ChunkerWordPosContextGeneratorConf(properties);
		ObjectStream<Event> es = new ChunkerWordPosSampleEvent(sampleStream, contextGen);
		return es;
	}

	/**
	 * 将多个特征转换成一条SVM的输入样本，无label值。转成数据格式如："index1:value1 index2:value2...";
	 * 
	 * @param features
	 *            特征
	 * @param ci
	 *            转换信息
	 * @return SVM样本
	 */
	public static String toSVMSample(String[] features, SVMFeatureLabelInfo ci)
	{
		StringBuilder intInput = new StringBuilder();

		int[] order = new int[features.length]; // 用来记录每个特征对应的index

		for (int i = 0; i < features.length; i++)
		{
			order[i] = ci.getFeatureIndex(features[i]);
		}

		Arrays.sort(order);

		for (int i = 0; i < order.length; i++)
		{
			if (order[i] != -1)
			{
				intInput.append(order[i] + ":1 "); // 此处为赋value值
			}
		}

		return intInput.toString().trim();
	}

	/**
	 * 将一个Event转换成一条SVM的输入样本。转成数据格式如："label1 index1:value1 index2:value2...";
	 * 
	 * @param event
	 *            事件
	 * @param ci
	 *            转换信息
	 * @return SVM样本
	 */
	public static String toSVMSample(Event event, SVMFeatureLabelInfo ci)
	{
		StringBuilder result = new StringBuilder();
		String[] contexts = event.getContext();
		if (event != null)
		{
			int label = ci.getClassIndex(event.getOutcome());
			result.append(label);

			String str = toSVMSample(contexts, ci);
			result.append(" " + str);
		}
		return result.toString();
	}

	/**
	 * 将事件流转换成SVM输入样本。转成数据格式如："label1 index1:value1 index2:value2...";
	 * 
	 * @param es
	 *            事件流
	 * @param ci
	 *            转换信息
	 * @return SVM输入样本
	 * @throws IOException 
	 */
	public static String[] toSVMSamples(ObjectStream<Event> es, SVMFeatureLabelInfo ci) throws IOException
	{
		List<String> inputList = new ArrayList<String>();

		Event temp = null;

		while ((temp = es.read()) != null)
		{
			String sample = toSVMSample(temp, ci);
			inputList.add(sample);
		}

		String[] input = new String[inputList.size()];

		inputList.toArray(input);

		return input;
	}

	/**
	 * 获取默认的特征配置文件
	 */
	public static Properties getDefaultConf() throws IOException
	{
		Properties featureConf = new Properties();
		InputStream featureStream = ChunkerWordPosContextGeneratorConf.class.getClassLoader()
				.getResourceAsStream("com/lc/nlp4han/chunk/svm/feature.properties");
		featureConf.load(featureStream);
		return featureConf;
	}

	/**
	 * 读取语料，转换成SVM标准输入格式并保存，同时生成转换信息类并序列化，返回转换信息类ConversionInformation
	 * 
	 * @param filePath
	 *            组块语料文件路径
	 * @param encoding
	 *            语料文件编码格式
	 * @param scheme
	 *            组块标记格式，BIEO，BIEOS等
	 * @param properties
	 *            特征配置文件
	 * @param saveFilePath
	 *            SVM输入样本数据存储路径，注意：转换信息序列化的文件路径为saveFilePath + ".info"
	 * @return 转换信息类
	 * @throws IOException
	 */
	public static SVMFeatureLabelInfo convert(String filePath, String encoding, String scheme, Properties properties,
			String saveFilePath) throws IOException
	{
		ObjectStream<Event> es = getEventStream(filePath, encoding, scheme, properties);

		SVMFeatureLabelInfo tfi = new SVMFeatureLabelInfo(es);

		es.reset();

		String[] input = toSVMSamples(es, tfi);

		es.close();

		saveFile(saveFilePath, input, "utf-8");

		tfi.write(saveFilePath + ".info", "utf-8");

		return tfi;
	}

	/**
	 * 读取语料，转换成SVM标准输入格式并保存，同时生成转换信息类并序列化，返回转换信息类ConversionInformation。此方法使用默认特征配置文件。
	 * 
	 * @param filePath
	 *            组块语料文件路径
	 * @param encoding
	 *            语料文件编码格式
	 * @param scheme
	 *            组块标记格式，BIEO，BIEOS等
	 * @param saveFilePath
	 *            SVM输入样本数据存储路径，注意：转换信息序列化的文件路径为saveFilePath + ".info"
	 * @return 转换信息类
	 * @throws IOException
	 */
	public static SVMFeatureLabelInfo convert(String filePath, String encoding, String scheme, String saveFilePath)
			throws IOException
	{
		Properties properties = getDefaultConf();
		
		return convert(filePath, encoding, scheme, properties, saveFilePath);
	}

	/**
	 * 根据参数，读取语料，转换成SVM标准输入格式并保存，同时生成转换信息类并序列化，返回转换信息类ConversionInformation。此方法使用默认特征配置文件。
	 * 
	 * @param args
	 *            参数
	 * @return 转换信息类
	 * @throws IOException
	 */
	public static SVMFeatureLabelInfo convert(String[] args) throws IOException
	{
		String[] params = parseArgs(args);
		
		Properties featureConf = getDefaultConf();
		SVMFeatureLabelInfo ci = convert(params[0], params[1], params[2], featureConf, params[3]);
		
		return ci;
	}

	/**
	 * 解析输入命令行
	 */
	private static String[] parseArgs(String[] args)
	{
		String usage = "Usage: SVMStandardInput [options] -data data_file\n" + "options:\n"
				+ "-label label : such as BIOE, BIOES\n" + "-encoding encoding : set encoding form\n"
				+ "-save save_file : set save file path\n";

		String encoding = "utf-8";

		String docPath = null;

		String scheme = "BIEOS";

		String savePath = null;

		for (int i = 0; i < args.length; i++)
		{
			if ("-encoding".equals(args[i]))
			{
				encoding = args[i + 1];
				i++;
			}
			else if ("-data".equals(args[i]))
			{
				docPath = args[i + 1];
				i++;
			}
			else if ("-label".equals(args[i]))
			{
				scheme = args[i + 1];
				i++;
			}
			else if ("-save".equals(args[i]))
			{
				savePath = args[i + 1];
				i++;
			}
			else
			{
				System.err.println(usage);
				System.exit(1);
			}

		}

		if (docPath == null)
		{
			System.err.println(usage);
			System.exit(1);
		}

		final java.nio.file.Path docDir = java.nio.file.Paths.get(docPath);

		if (!java.nio.file.Files.isReadable(docDir))
		{
			System.out.println("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		String[] result = null;

		result = new String[4];
		result[0] = docPath;
		result[1] = encoding;
		result[2] = scheme;
		if (savePath != null)
			result[3] = savePath;
		else
			result[3] = docPath + ".svm";

		return result;
	}

	private static void saveFile(String saveFilePath, String[] datum, String encoding)
	{
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFilePath), encoding));
			for (int i = 0; i < datum.length; i++)
			{
				bw.write(datum[i]);
				bw.write("\n");
			}

			bw.flush();
			bw.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (null != bw)
				{
					bw.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

}
