package com.lc.nlp4han.constituent.lex;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 添加词性标注的概率，并在解析时作为先验概率
 * 
 * @author qyl
 *
 */
public class LexPCFGPrior extends LexPCFG
{
	private HashMap<String, Double> priorMap = new HashMap<String, Double>();

	public LexPCFGPrior()
	{
	}

	public LexPCFGPrior(String startSymbol, HashSet<String> posSet, HashMap<WordPOS, Integer> wordMap,
			HashMap<String, HashSet<String>> posesOfWord, HashMap<OccurenceCollins, RuleAmountsInfo> headGenMap,
			HashMap<OccurenceCollins, HashSet<String>> parentList,
			HashMap<OccurenceCollins, RuleAmountsInfo> sidesGenMap,
			HashMap<OccurenceCollins, RuleAmountsInfo> stopGenMap,
			HashMap<OccurenceCollins, RuleAmountsInfo> specialGenMap)
	{
		super(startSymbol, posSet, wordMap, posesOfWord, headGenMap, parentList, sidesGenMap, stopGenMap,
				specialGenMap);
	}

	public LexPCFGPrior(HashMap<String, Double> priorMap)
	{
		super();
		this.priorMap = priorMap;
	}

	public HashMap<String, Double> getPriorMap()
	{
		return priorMap;
	}

	public double getPosPro(String pos)
	{
		return priorMap.get(pos);
	}

	public void setPriorMap(HashMap<String, Double> priorMap)
	{
		this.priorMap = priorMap;
	}

}
