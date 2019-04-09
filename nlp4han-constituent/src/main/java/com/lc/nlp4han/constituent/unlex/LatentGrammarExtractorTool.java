package com.lc.nlp4han.constituent.unlex;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 获得Grammar的工具类
 * 
 * @author 王宁
 * 
 */
public class LatentGrammarExtractorTool
{
	public static Grammar getGrammar(String treeBankPath, String encoding, int SMCycle, double mergeRate,
			int EMIterations, double smooth, int rareWordThreshold) throws IOException
	{
		GrammarExtractor gExtractor = new GrammarExtractor();
		if (SMCycle < 0)
		{
			throw new Error("SMCycle不能小于0");
		}
		else
		{
			return gExtractor.extractLatentGrammar(treeBankPath, encoding, rareWordThreshold, SMCycle,
					EMIterations, mergeRate, smooth);
		}
	}

	public static void main(String[] args)
	{
		String trainFilePath = null;
		String outputFilePath = null;
		String encoding = "utf-8";
		int iterations = 50;// em算法迭代次数
		int SMCycle = 1;
		double mergeRate = 0.5;
		double smooth = 0.01;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-data"))
			{
				trainFilePath = args[i + 1];
				i++;
			}
			if (args[i].equals("-out"))
			{
				outputFilePath = args[i + 1];
				i++;
			}

			if (args[i].equals("-encoding"))
			{
				encoding = args[i + 1];
				i++;
			}
			if (args[i].equals("-sm"))
			{
				SMCycle = Integer.parseInt(args[i + 1]);
				i++;
			}
			if (args[i].equals("-em"))
			{
				iterations = Integer.parseInt(args[i + 1]);
				i++;
			}
			if (args[i].equals("-smooth"))
			{
				smooth = Double.parseDouble(args[i + 1]);
				i++;
			}
			if (args[i].equals("-merge"))
			{
				mergeRate = Double.parseDouble(args[i + 1]);
			}
		}
		
		if (trainFilePath == null || outputFilePath == null)
		{
			usage();
			System.exit(0);
		}
		
		try
		{
			long start = System.currentTimeMillis();
			System.out.println("开始提取初始文法");
			
			Grammar g = LatentGrammarExtractorTool.getGrammar(trainFilePath, encoding, SMCycle, mergeRate,
					iterations, smooth, Lexicon.DEFAULT_RAREWORD_THRESHOLD);
			
			DataOutput out = new DataOutputStream(new FileOutputStream(outputFilePath));
			g.write(out);
			
			System.out.println("提取初始文法完毕");
			
			long end = System.currentTimeMillis();
			long time = end - start;
			System.out.println("提取语法消耗时间：" + time);
			System.out.println("输出文件：" + outputFilePath);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void usage()
	{
		System.out.println(LatentGrammarExtractorTool.class.getName() + "\n"
				+ " -train <trainFile> -out <outFile> [-sm <SMCycle>] [-em <EMIterations>] [-merge <mergeRate>] [-smooth <smoothRate>]");
	}
}
