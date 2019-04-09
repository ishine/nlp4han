package com.lc.nlp4han.clustering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.lc.nlp4han.segment.WordSegFactory;
import com.lc.nlp4han.segment.WordSegmenter;
import com.lc.nlp4han.util.CharTypeUtil;

/**
 * 该特征生成器是基于分词，用于生成key为词，value为0或1的特征
 * 
 * @author 杨智超
 *
 */
public class WordBasedZeroOneFeatureGenerator implements FeatureGenerator
{
	private Set<String> stopWords = new HashSet<String>();
	private Map<String, Count> textsInfo = new HashMap<String, Count>();

	@Override
	public void init(List<Text> texts)
	{
		for (int i = 0; i < texts.size(); i++)
		{
			Text t = texts.get(i);
			Map<String, Integer> m = getWords(t);
			Set<Entry<String, Integer>> es = m.entrySet();
			for (Entry<String, Integer> e : es)
			{
				String key = e.getKey();
				if (textsInfo.containsKey(key))
				{
					Count c = textsInfo.get(key);
					c.tn += e.getValue();
					c.dn++;
				}
				else
				{
					Count c = new Count(e.getValue(), 1);
					textsInfo.put(key, c);
				}
			}
		}
		textsInfo = pruning(textsInfo);
	}

	private <T> Map<String, T> pruning(Map<String, T> words)
	{
		Map<String, T> result = new HashMap<String, T>();
		Set<Entry<String, T>> es = words.entrySet();

		for (Entry<String, T> e : es)
		{
			if (isPruned(e.getKey()))
				continue;
			else
				result.put(e.getKey(), e.getValue());
		}

		return result;
	}

	/**
	 * 对文本进行分词
	 * 
	 * @param text
	 *            待分词的文本
	 * @return 所有词条及其个数
	 */
	private Map<String, Integer> getWords(Text text)
	{
		Map<String, Integer> fm = new HashMap<String, Integer>();
		WordSegmenter segmenter;
		String[] words;
		try
		{
			segmenter = WordSegFactory.getWordSegmenter();
			words = segmenter.segment(text.getContent()); // 分词

			for (int i = 0; i < words.length; i++) // 统计各词的个数
			{
				if (fm.containsKey(words[i]))
				{
					int v = fm.get(words[i]);
					v++;
					fm.put(words[i], v);
				}
				else
				{
					fm.put(words[i], 1);
				}
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return fm;
	}

	@Override
	public List<Feature> getFeatures(Text text)
	{
		List<Feature> result = new ArrayList<Feature>();

		WordSegmenter segmenter;
		String[] words = null;

		try
		{
			segmenter = WordSegFactory.getWordSegmenter();
			words = segmenter.segment(text.getContent());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		HashSet<String> tempSet = new HashSet<String>(); // 用于去重

		for (int i = 0; i < words.length; i++)
		{
			if (!tempSet.contains(words[i]))
			{
				tempSet.add(words[i]);

				if (!isPruned(words[i]))
				{
					Feature f = new Feature(words[i], 1.0);
					result.add(f);
				}
			}
		}

		return result;
	}

	private boolean isPruned(String word)
	{
		if (CharTypeUtil.isChinesePunctuation(word))
			return true;
		if (isStopWord(word))
			return true;
		for (int i = 0; i < word.length(); i++)
		{
			if (isChinese(word.charAt(i)))
				return false;
			if (CharTypeUtil.isLetter(word.charAt(i)))
				return false;
		}
		return true;
	}

	private boolean isChinese(char c)
	{
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C// jdk1.7
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D// jdk1.7
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT)
		{
			return true;
		}
		return false;
	}

	private boolean isStopWord(String word)
	{
		if (stopWords.size() < 1)
			loadStopWordList("停用词.txt", "utf-8");
		if (stopWords.contains(word))
			return true;
		else
			return false;
	}

	private void loadStopWordList(String fileName, String encoding)
	{
		InputStream is = WordBasedTF_IDFFeatureGenerator.class.getClassLoader().getResourceAsStream(fileName);
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new InputStreamReader(is, encoding));
			String tmp = null;
			while ((tmp = br.readLine()) != null)
			{
				stopWords.add(tmp);
			}
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public boolean isInitialized()
	{
		if (textsInfo == null || textsInfo.size() < 1)
			return false;
		else
			return true;
	}

	@Override
	public Map<String, Count> getTextsInfo()
	{
		return textsInfo;
	}

}
