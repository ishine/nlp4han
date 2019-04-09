package com.lc.nlp4han.clustering;

import java.io.IOException;
import java.util.List;

/**
 * 评价应用程序
 * @author 杨智超
 *
 */
public class EvaluationApplicationTool
{
	private static final String USAGE = "Usage: EvaluationApplicationTool [options] -data data_file -k groups_number [-encoding encoding]";
	
	/**
	 * 解析输入命令行
	 */
	private static String[] parseArgs(String[] args)
	{
		String usage = USAGE;

		String encoding = "utf-8";

		String docPath = null;
		
		String k = null;

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
			else if ("-k".equals(args[i]))
			{
				k = args[i + 1];
				i++;
			}
			else
			{
				System.err.println(usage);
				System.exit(1);
			}

		}

		if (docPath == null || k==null)
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

		String[] result = new String[3];
		result[0] = docPath;
		result[1] = encoding;
		result[2] = k;

		return result;
	}
	
	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();
		
		String[] params = parseArgs(args);
		
		List<Text> texts = Text.getTexts(params[0], false, params[1]);
		
		List<Group> groups = KMeans.run(texts, Integer.parseInt(params[2]));
		
		long endTime = System.currentTimeMillis();
		System.out.println("耗时：" + (endTime - startTime)/1000 + "秒");
		
		Evaluation eval = new Evaluation(texts, groups);
		System.out.println("F1: " + eval.FMeasure(1));
		System.out.println("Purity: " + eval.purity());
		System.out.println("NMI: " + eval.NMI());
		System.out.println("RI: " + eval.RI());
	}
}
