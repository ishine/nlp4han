package com.lc.nlp4han.segment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.lc.nlp4han.ml.util.ModelWrapper;
import com.lc.nlp4han.segment.maxent.WordSegContextGeneratorConf;
import com.lc.nlp4han.segment.maxent.WordSegmenterME;

/**
 * 中文分词器工厂类
 * 
 * @author 刘小峰
 *
 */
public class WordSegFactory
{
	private static WordSegmenter wordSegmenter;

	private WordSegFactory()
	{
	}

	/**
	 * 装入中文分词模型，并生成中文分词器
	 * 
	 * @return 中文分词器
	 * @throws IOException
	 */
	public static WordSegmenter getWordSegmenter() throws IOException
	{
		if (wordSegmenter != null)
			return wordSegmenter;

		String modelName = "com/lc/nlp4han/segment/ctb8-seg.model";

		InputStream modelIn = WordSegFactory.class.getClassLoader().getResourceAsStream(modelName);
		ModelWrapper model = new ModelWrapper(modelIn);

		wordSegmenter = new WordSegmenterME(model, new WordSegContextGeneratorConf());

		return wordSegmenter;
	}

	public static void main(String[] args) throws IOException
	{
		WordSegmenter segmenter = WordSegFactory.getWordSegmenter();

		if (args.length < 1)
		{
			String text = "我喜欢自然语言处理。";
			String[] words = segmenter.segment(text);
			for (String w : words)
				System.out.println(w);
		}
		else
		{
			String file = args[0];
			String encoding = args[1];

			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
			SentenceSegmenter sentenceSegmenter = new SentenceSegmenterRule();
			while ((line = reader.readLine()) != null)
			{
				String[] sentences = sentenceSegmenter.segment(line);
				for (String sentence : sentences)
				{
					sentence = sentence.trim();
					String[] words = segmenter.segment(sentence);
					for (String w : words)
						System.out.print(w + "  ");
					
					System.out.println();
				}
			}

			reader.close();
		}
	}
}
