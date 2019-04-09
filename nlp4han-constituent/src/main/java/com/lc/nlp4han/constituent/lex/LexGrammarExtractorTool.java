package com.lc.nlp4han.constituent.lex;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 提取LexGrammar的命令行应用程序
 */
public class LexGrammarExtractorTool
{
	public static void main(String[] args) throws IOException
	{
		if (args.length < 1)
		{
			return;
		}
		String frompath = null;
		String topath = null;
		String encoding = "UTF-8";
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
			ExtractLexGrammarToFile(frompath, topath, encoding);
		}
		else
		{
			System.out.println(new LexGrammarExtractor().extractGrammar(frompath, encoding).toString());
		}
	}

	/**
	 * 从树库中提取文法，然后存入文件指定中
	 */
	private static void ExtractLexGrammarToFile(String fromPath, String toPath, String inCoding) throws IOException
	{
//		LexPCFGModelIOUtil.writeModel(new LexGrammarExtractor().extractGrammar(fromPath, inCoding), toPath);

		DataOutputStream out = new DataOutputStream(new FileOutputStream(toPath));

		LexPCFG pcfg = new LexGrammarExtractor().extractGrammar(fromPath, inCoding);
		pcfg.write(out);
	}
}
