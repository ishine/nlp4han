package com.lc.nlp4han.constituent.lex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.PlainTextByTreeStream;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreeNodeUtil;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;

/**
 * 由于特定阶段的解析算法的效率低下，尤其对长句子而言，故我可以先选择部分短句子进行
 * 验证或者测试
 * @author qyl
 *
 */
public class SelectSentenceTool
{
	public static void main(String[] args) throws IOException
	{
		if (args.length < 1)
		{
			return;
		}
		String corpusFile = null;
		String encoding = "UTF-8";
		String goalFile = null;
		int  length = 30;
		int  num = 1000; 
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-data"))
			{
				corpusFile = args[i + 1];
				i++;
			}
			else if (args[i].equals("-encoding"))
			{
				encoding = args[i + 1];
				i++;
			}
			else if (args[i].equals("-out"))
			{
				goalFile = args[i + 1];
				i++;
			}
			else if (args[i].equals("-length"))
			{
				length = Integer.parseInt(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-num"))
			{
				num =  Integer.parseInt(args[i + 1]);
				i++;
			}
		}
		
		GetSelectTrainFile(corpusFile, encoding, goalFile,length,num);
	}

	private static void GetSelectTrainFile(String corpusFile, String encoding, String goalFile,int length,int num) throws UnsupportedOperationException, FileNotFoundException, IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(goalFile), encoding));	
		PlainTextByTreeStream ptbt = new PlainTextByTreeStream(new FileInputStreamFactory(new File(corpusFile)),
				encoding);
		
		String bracketStr = ptbt.read();
		int count=0;
		while (bracketStr.length() != 0)
		{
			TreeNode rootNode = BracketExpUtil.generateTree(bracketStr);
			String[] words=TreeNodeUtil.getetWords(rootNode);
			if(words.length<=length) {
				bw.append(rootNode.toString()+'\n');
				count++;
			}
			if(count>num) {
				break;
			}
			bracketStr = ptbt.read();
		}
		bw.close();
		ptbt.close();
	}
}
