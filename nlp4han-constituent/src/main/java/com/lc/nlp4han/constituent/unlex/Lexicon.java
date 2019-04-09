package com.lc.nlp4han.constituent.unlex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 用来保存preTerminalRule，以及用来预测一个未知词型的词性标记的概率P(word|tag)
 * 
 * @author 王宁
 * 
 */
public class Lexicon
{
	public static int DEFAULT_RAREWORD_THRESHOLD = 10;
	private HashSet<PreterminalRule> preRules;
	private HashMap<String, Integer> dictionary;
	private ArrayList<String> rareWord;
	// for parse
	private double[][] subTag2UNKScores;// subTag2UNKScores[tag][subTag]表示已知某未知词的词性标注是tag，tag_i-->UNK的概率

	// private ArrayList<Short> tagWithRareWord;
	// private ArrayList<Integer> rareWordCount;//
	// rareWordCount[i]表示tagOfRareWord[i]对应的tag包含的rareword的个数
	// private int allRareWord;
	// private double[] scores;// 一个未知词是某tag的概率

	public Lexicon(HashSet<PreterminalRule> preRules, HashSet<String> dictionary, ArrayList<Short> tagWithRareWord,
			ArrayList<Integer> rareWordCount, ArrayList<String> rareWord, int allRareWord)
	{
		this.preRules = preRules;
		// this.tagWithRareWord = tagWithRareWord;
		// this.rareWordCount = rareWordCount;
		// this.allRareWord = allRareWord;
		// this.scores = new double[tagWithRareWord.size()];
		this.dictionary = new HashMap<>();
		this.rareWord = rareWord;
		init(dictionary);
	}

	public Lexicon()
	{
		this.preRules = new HashSet<PreterminalRule>();
		this.dictionary = new HashMap<>();
	}

	public void init(HashSet<String> dictionary)
	{
		int index = 0;
		for (String word : dictionary)
		{
			this.dictionary.put(word, index);
			index++;
		}
	}

	// public void calculateScore()
	// {
	// for (int i = 0; i < tagWithRareWord.size(); i++)
	// {
	// scores[i] = rareWordCount.get(i) / allRareWord;
	// }
	// }

	public void add(PreterminalRule preRule)
	{
		preRules.add(preRule);
		if (!dictionary.containsKey(preRule.getWord()))
			dictionary.put(preRule.getWord(), dictionary.size());
	}

	public boolean hasRecorded(String word)
	{
		return dictionary.containsKey(word);
	}

	public int wordIntValue(String word)
	{
		return dictionary.get(word);
	}

	public HashMap<String, Integer> getDictionary()
	{
		return dictionary;
	}

	public void setDictionary(HashMap<String, Integer> dictionary)
	{
		this.dictionary = dictionary;
	}

	public HashSet<PreterminalRule> getPreRules()
	{
		return preRules;
	}

	public void setPreRules(HashSet<PreterminalRule> preRules)
	{
		this.preRules = preRules;
	}

	public ArrayList<String> getRareWord()
	{
		return rareWord;
	}

	public void setRareWord(ArrayList<String> rareWord)
	{
		this.rareWord = rareWord;
	}

	public double[][] getSubTag2UNKScores()
	{
		return subTag2UNKScores;
	}

	public void setSubTag2UNKScores(double[][] subTag2UNKScores)
	{
		this.subTag2UNKScores = subTag2UNKScores;
	}

}
