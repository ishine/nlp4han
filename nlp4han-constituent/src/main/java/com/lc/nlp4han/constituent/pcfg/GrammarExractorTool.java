package com.lc.nlp4han.constituent.pcfg;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文法提取应用
 * 
 * 支持提取PCFG文法
 *
 */
public class GrammarExractorTool
{
	public static void main(String[] args) throws IOException
	{
		if (args.length < 1)
		{
			return;
		}
		String frompath = null;
		String topath = null;
		String encoding = null;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-data"))
			{
				frompath = args[i + 1];
				i++;
			}
			if (args[i].equals("-out"))
			{
				topath = args[i + 1];
				i++;
			}
			if (args[i].equals("-encoding"))
			{
				encoding = args[i + 1];
				i++;
			}
		}

		if (topath != null)
		{
			ExtractGrammarToFile(frompath, topath, encoding);
		}
		else
		{
			System.out.println(GrammarExtractor.getPCFG(frompath, encoding).toString());
		}
	}

	/**
	 * 从树库中提取文法，然后存入文件指定中
	 */
	private static void ExtractGrammarToFile(String fromPath, String toPath, String inCoding) throws IOException
	{
		DataOutputStream out = new DataOutputStream(new FileOutputStream(toPath));

		PCFG pcfg = GrammarExtractor.getPCFG(fromPath, inCoding);
		pcfg.write(out);

	}
}
