package com.lc.nlp4han.csc.evaluation;

import java.io.IOException;
import java.util.ArrayList;

import com.lc.nlp4han.csc.util.FileUtils;
import com.lc.nlp4han.csc.util.Sentence;

/**
 * 给定原始文本，标准文本和系统纠错文本，返回各个指标的结果
 */
public class EvaluatorApp
{

	public static void main(String[] args) throws IOException
	{

		int len = args.length;
		if (4 != len && 5 != len)
		{
			System.err.println("错误的参数个数：" + len + "\n示例1:(Evaluator 测试语料路径  黄金语料路径  系统结果路径 文件编码  输出路径)"
					+ "\n示例2:(Evaluator 测试语料路径  黄金语料路径  系统结果路径 文件编码)");
			System.exit(0);
		}

		String originalFile = args[0];
		String goldFile = args[1];
		String resultFile = args[2];
		String encoding = args[3];

		ArrayList<Sentence> original = FileUtils.readSentenceFile(originalFile, encoding);
		ArrayList<Sentence> gold = FileUtils.readSentenceFile(goldFile, encoding);
		ArrayList<Sentence> result = FileUtils.readSentenceFile(resultFile, encoding);

		Evaluation evaluator = new CSCEvaluator(original, gold, result);
		String eval = evaluator.show();

		if (len == 5)
		{
			String output = args[4];
			FileUtils.writeEvaluation(output, encoding, eval);
		}
	}

}
