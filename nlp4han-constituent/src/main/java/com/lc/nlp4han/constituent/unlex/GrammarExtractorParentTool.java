package com.lc.nlp4han.constituent.unlex;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 专门用于提取带父节点标注的语法
 * 
 * @author 王宁
 */
public class GrammarExtractorParentTool
{
	public static Grammar getGrammar(String trainFilePath, String encoding, int rareWordThreshold) throws IOException
	{
		GrammarExtractor gExtractor = new GrammarExtractor();
		return gExtractor.extractGrammarParentLabel(trainFilePath, encoding, rareWordThreshold);
	}

	public static void main(String[] args)
	{
		String trainFilePath = null;
		String outputFilePath = null;
		String encoding = "utf-8";
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-train"))
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
			
			Grammar g = GrammarExtractorParentTool.getGrammar(trainFilePath, encoding,
					Lexicon.DEFAULT_RAREWORD_THRESHOLD);
			
//			GrammarWriter.writeToFile(g, outputFilePath, true);
			DataOutput out = new DataOutputStream(new FileOutputStream(outputFilePath));
			g.write(out);
			
			System.out.println("提取初始文法完毕");
			
			long end = System.currentTimeMillis();
			long time = end - start;
			System.out.println("提取语法消耗时间：" + time);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void usage()
	{
		System.out.println(GrammarExtractorParentTool.class.getName() + "\n"
				+ " -train <trainFile> -out <outFile>  [-encoing <encoding>]  ");
	}

}
