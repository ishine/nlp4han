package com.lc.nlp4han.constituent.unlex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.lc.nlp4han.constituent.GrammarWritable;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.pcfg.PCFG;
import com.lc.nlp4han.constituent.pcfg.PRule;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;
import com.lc.nlp4han.ml.util.PlainTextByLineStream;

/**
 * 带隐藏标记的PCFG文法
 * 
 * @author 王宁
 * 
 */
public class Grammar implements GrammarWritable
{
	private String StartSymbol = "ROOT";

	private HashSet<BinaryRule> bRules;
	private HashSet<UnaryRule> uRules;
	private Lexicon lexicon;// 包含preRules
	
	// 相同父节点的规则放在一个map中
	private HashMap<Short, HashMap<PreterminalRule, PreterminalRule>> preRuleBySameHead; // 内map<ruleHashcode/rule>
	private HashMap<Short, HashMap<BinaryRule, BinaryRule>> bRuleBySameHead;
	private HashMap<Short, HashMap<UnaryRule, UnaryRule>> uRuleBySameHead;

	private NonterminalTable nonterminalTable;

	public Grammar(HashSet<BinaryRule> bRules, HashSet<UnaryRule> uRules, Lexicon lexicon,
			NonterminalTable nonterminalTable)
	{
		this.bRules = bRules;
		this.uRules = uRules;
		this.lexicon = lexicon;
		this.bRuleBySameHead = new HashMap<Short, HashMap<BinaryRule, BinaryRule>>();
		this.uRuleBySameHead = new HashMap<Short, HashMap<UnaryRule, UnaryRule>>();
		this.preRuleBySameHead = new HashMap<Short, HashMap<PreterminalRule, PreterminalRule>>();
		this.nonterminalTable = nonterminalTable;
		init();
		// grammarExam();
	}

	public Grammar()
	{
		this.bRules = new HashSet<>();
		this.uRules = new HashSet<>();
		this.lexicon = new Lexicon();

		this.bRuleBySameHead = new HashMap<Short, HashMap<BinaryRule, BinaryRule>>();
		this.uRuleBySameHead = new HashMap<Short, HashMap<UnaryRule, UnaryRule>>();
		this.preRuleBySameHead = new HashMap<Short, HashMap<PreterminalRule, PreterminalRule>>();
	}

	public Grammar(String modelPath, String encoding) throws FileNotFoundException, IOException
	{
		this(new PlainTextByLineStream(new FileInputStreamFactory(new File(modelPath)), encoding));
	}

	public Grammar(PlainTextByLineStream stream) throws IOException
	{
		read(stream);
	}

	/**
	 * 返回CFG，其中规则包含一元规则、二元规则
	 * 
	 * @return
	 */
	public PCFG getPCFG()
	{
		PCFG pcfg = new PCFG();
		
		for (UnaryRule uRule : uRules)
		{
			for (String aRule : uRule.toStringRules(this))
			{
				String[] arr = aRule.split(" ");
				double score = Double.parseDouble(arr[arr.length - 1]);
				if (score != 0)
				{
					PRule pRule = new PRule(Double.parseDouble(arr[arr.length - 1]), arr[0], arr[2]);
					pcfg.add(pRule);
				}
			}
		}

		for (BinaryRule bRule : bRules)
		{
			for (String aRule : bRule.toStringRules(this))
			{
				String[] arr = aRule.split(" ");
				double score = Double.parseDouble(arr[arr.length - 1]);
				if (score != 0)
				{
					PRule pRule = new PRule(score, arr[0], arr[2], arr[3]);
					pcfg.add(pRule);
				}
			}

		}

		for (PreterminalRule preRule : lexicon.getPreRules())
		{
			for (String aRule : preRule.toStringRules(this))
			{
				String[] arr = aRule.split(" ");
				double score = Double.parseDouble(arr[arr.length - 1]);
				if (score != 0)
				{
					PRule pRule = new PRule(score, arr[0], arr[2]);
					pcfg.add(pRule);
				}
			}
		}
		
		Set<String> nonTerminalSet = this.getAllSubNonterminalSym();
		pcfg.setNonTerminalSet(nonTerminalSet);
		pcfg.setStartSymbol("ROOT");
		
		return pcfg;
	}

	private HashSet<String> getAllSubNonterminalSym()
	{
		HashSet<String> nonTerminalSet = new HashSet<String>();
		for (short symbol = 0; symbol < this.getNumSymbol(); symbol++)
		{
			String symStr = this.symbolStrValue(symbol);
			for (short subSym = 0; subSym < this.getNumSubSymbol(symbol); subSym++)
			{
				if (subSym == 0 && this.getNumSubSymbol(symbol) == 1)
				{
					nonTerminalSet.add(symStr);
				}
				else
				{
					nonTerminalSet.add(symStr + "_" + subSym);
				}
			}
		}
		return nonTerminalSet;
	}

	private void init()
	{
		for (BinaryRule bRule : bRules)
		{

			if (!bRuleBySameHead.containsKey(bRule.parent))
			{
				bRuleBySameHead.put(bRule.parent, new HashMap<BinaryRule, BinaryRule>());
			}

			bRuleBySameHead.get(bRule.parent).put(bRule, bRule);

		}

		for (UnaryRule uRule : uRules)
		{
			if (!uRuleBySameHead.containsKey(uRule.parent))
			{
				uRuleBySameHead.put(uRule.parent, new HashMap<UnaryRule, UnaryRule>());
			}
			uRuleBySameHead.get(uRule.parent).put(uRule, uRule);

		}

		for (PreterminalRule preRule : lexicon.getPreRules())
		{
			if (!preRuleBySameHead.containsKey(preRule.parent))
			{
				preRuleBySameHead.put(preRule.parent, new HashMap<PreterminalRule, PreterminalRule>());
			}
			preRuleBySameHead.get(preRule.parent).put(preRule, preRule);

		}
	}

//	public TreeMap<String, Double> calculateSameParentRuleScoreSum()
//	{
//		TreeMap<String, Double> sameParentRuleScoreSum = new TreeMap<>();
//		for (short symbol : this.nonterminalTable.getInt_strMap().keySet())
//		{
//			if (this.bRuleBySameHead.containsKey(symbol))
//			{
//				for (BinaryRule bRule : this.bRuleBySameHead.get(symbol).keySet())
//				{
//					for (Map.Entry<String, Double> entry : bRule.getParent_i_ScoceSum(this).entrySet())
//					{
//						sameParentRuleScoreSum.merge(entry.getKey(), entry.getValue(),
//								new BiFunction<Double, Double, Double>()
//								{
//									@Override
//									public Double apply(Double t, Double u)
//									{
//										return t + u;
//									}
//								});
//					}
//				}
//			}
//
//			if (this.uRuleBySameHead.containsKey(symbol))
//			{
//				for (UnaryRule uRule : this.uRuleBySameHead.get(symbol).keySet())
//				{
//					for (Map.Entry<String, Double> entry : uRule.getParent_i_ScoceSum(this).entrySet())
//					{
//						sameParentRuleScoreSum.merge(entry.getKey(), entry.getValue(),
//								(score, newScore) -> score + newScore);
//					}
//				}
//			}
//
//			if (this.preRuleBySameHead.containsKey(symbol))
//			{
//				for (PreterminalRule preRule : this.preRuleBySameHead.get(symbol).keySet())
//				{
//					for (Map.Entry<String, Double> entry : preRule.getParent_i_ScoceSum(this).entrySet())
//					{
//						sameParentRuleScoreSum.merge(entry.getKey(), entry.getValue(),
//								(score, newScore) -> score + newScore);
//					}
//				}
//			}
//		}
//
//		return sameParentRuleScoreSum;
//	}

	public void grammarExam()
	{
		int berrcount = 0, uerrcount = 0, perrcount = 0;
		int berrcount1 = 0, uerrcount1 = 0, perrcount1 = 0;
		for (BinaryRule bRule : bRules)
		{
			if (bRule == bRuleBySameHead.get(bRule.parent).get(bRule))
			{
				// System.out.println(true);
			}
			else
			{
				berrcount++;
				System.err.println(false + "********************二元规则集bRuleBySameHead" + berrcount);
			}
		}

		for (UnaryRule uRule : uRules)
		{
			if (uRule == uRuleBySameHead.get(uRule.parent).get(uRule))
			{
				// System.out.println(true);
			}
			else
			{
				uerrcount++;
				System.err.println(false + "********************一元规则uRuleBySameHead" + uerrcount);
			}
		}

		for (PreterminalRule preRule : lexicon.getPreRules())
		{
			if (preRule == preRuleBySameHead.get(preRule.parent).get(preRule))
			{
				// System.out.println(true);
			}
			else
			{
				perrcount++;
				System.err.println(false + "********************预终结符号规则preRuleBySameHead" + perrcount);
			}

		}
	}

	/**
	 * @param symbol
	 * @return 该符号对应的String
	 */
	public String symbolStrValue(short symbol)
	{
		return nonterminalTable.stringValue(symbol);
	}

	/**
	 * @param symbol
	 * @return 该符号对应的short值
	 */
	public short symbolIntValue(String symbol)
	{
		return nonterminalTable.intValue(symbol);
	}

	public int wordIntValue(String word)
	{
		return lexicon.wordIntValue(word);
	}

	public void add(BinaryRule bRule)
	{
		bRules.add(bRule);
		short parent = bRule.getParent();
		if (!bRuleBySameHead.containsKey(parent))
		{
			HashMap<BinaryRule, BinaryRule> inner = new HashMap<>();
			bRuleBySameHead.put(parent, inner);
		}
		
		bRuleBySameHead.get(parent).put(bRule, bRule);
	}

	public void add(UnaryRule uRule)
	{
		uRules.add(uRule);
		short parent = uRule.getParent();
		if (!uRuleBySameHead.containsKey(parent))
		{
			HashMap<UnaryRule, UnaryRule> inner = new HashMap<>();
			uRuleBySameHead.put(parent, inner);
		}
		
		uRuleBySameHead.get(parent).put(uRule, uRule);
	}

	public void add(PreterminalRule preRule)
	{
		lexicon.add(preRule);
		short parent = preRule.getParent();
		if (!preRuleBySameHead.containsKey(parent))
		{
			HashMap<PreterminalRule, PreterminalRule> inner = new HashMap<>();
			preRuleBySameHead.put(parent, inner);
		}
		
		preRuleBySameHead.get(parent).put(preRule, preRule);
	}

	public BinaryRule readBRule(String[] rule)
	{

		String parent = rule[0].split("_")[0];
		String lChild = rule[2].split("_")[0];
		String rChild = rule[3].split("_")[0];
		double score = Double.parseDouble(rule[4]);
		short index_pSubSym;
		short index_lCSubSym;
		short index_rCSubSym;
		short numSymP = nonterminalTable.getNumSubsymbolArr().get(symbolIntValue(parent));
		short numSymLC = nonterminalTable.getNumSubsymbolArr().get(symbolIntValue(lChild));
		short numSymRC = nonterminalTable.getNumSubsymbolArr().get(symbolIntValue(rChild));
		if (numSymP == 1)
			index_pSubSym = 0;
		else
			index_pSubSym = Short.parseShort(rule[0].split("_")[1]);

		if (numSymLC == 1)
			index_lCSubSym = 0;
		else
			index_lCSubSym = Short.parseShort(rule[2].split("_")[1]);

		if (numSymRC == 1)
			index_rCSubSym = 0;
		else
			index_rCSubSym = Short.parseShort(rule[3].split("_")[1]);
		BinaryRule bRule = new BinaryRule(symbolIntValue(parent), symbolIntValue(lChild), symbolIntValue(rChild));
		if (bRules.contains(bRule))
		{
			for (BinaryRule theRule : bRules)
			{
				if (bRule.equals(theRule))
				{
					bRule = theRule;
					break;
				}
			}
		}
		else
		{
			bRule.initScores(numSymP, numSymLC, numSymRC);
			this.add(bRule);
		}

		bRule.setScore(index_pSubSym, index_lCSubSym, index_rCSubSym, score);
		return bRule;
	}

	public UnaryRule readURule(String[] rule)
	{
		String parent = rule[0].split("_")[0];
		String child = rule[2].split("_")[0];
		double score = Double.parseDouble(rule[3]);
		short index_pSubSym;
		short index_cSubSym;
		short numSymP = nonterminalTable.getNumSubsymbolArr().get(symbolIntValue(parent));
		short numSymC = nonterminalTable.getNumSubsymbolArr().get(symbolIntValue(child));
		if (numSymP == 1)
			index_pSubSym = 0;
		else
			index_pSubSym = Short.parseShort(rule[0].split("_")[1]);
		if (numSymC == 1)
			index_cSubSym = 0;
		else
			index_cSubSym = Short.parseShort(rule[2].split("_")[1]);
		UnaryRule uRule = new UnaryRule(symbolIntValue(parent), symbolIntValue(child));
		if (uRules.contains(uRule))
		{
			for (UnaryRule theRule : uRules)
			{
				if (uRule.equals(theRule))
				{
					uRule = theRule;
					break;
				}
			}
		}
		else
		{
			uRule.initScores(numSymP, numSymC);
			this.add(uRule);
		}
		uRule.setScore(index_pSubSym, index_cSubSym, score);
		return uRule;
	}

	public PreterminalRule readPreRule(String[] rule)
	{
		String parent = rule[0].split("_")[0];
		String word = rule[2];
		double score = Double.parseDouble(rule[3]);
		short index_pSubSym;
		short numSymP = nonterminalTable.getNumSubsymbolArr().get(symbolIntValue(parent));
		if (numSymP == 1)
			index_pSubSym = 0;
		else
			index_pSubSym = Short.parseShort(rule[0].split("_")[1]);
		PreterminalRule preRule = new PreterminalRule(symbolIntValue(parent), word);
		if (lexicon.getPreRules().contains(preRule))
		{
			for (PreterminalRule theRule : lexicon.getPreRules())
			{
				if (preRule.equals(theRule))
				{
					preRule = theRule;
					break;
				}
			}
		}
		else
		{
			preRule.initScores(numSymP);
			this.add(preRule);
		}
		preRule.setScore(index_pSubSym, score);
		return preRule;
	}

	public void printRules()
	{
		for (BinaryRule rule : this.getbRules())
		{
			for (String str : rule.toStringRules(this))
			{
				System.out.println(str);
			}
		}

		for (UnaryRule rule : this.getuRules())
		{
			for (String str : rule.toStringRules(this))
			{
				System.out.println(str);
			}
		}

		for (PreterminalRule rule : this.getLexicon().getPreRules())
		{
			for (String str : rule.toStringRules(this))
			{
				System.out.println(str);
			}
		}
	}

	public short getNumSubSymbol(short symbol)
	{
		return nonterminalTable.getNumSubsymbolArr().get(symbol);
	}

	public boolean hasBRule(BinaryRule bRule)
	{
		return bRules.contains(bRule);
	}

	public boolean hasURule(UnaryRule uRule)
	{
		return uRules.contains(uRule);
	}

	public boolean hasPreRule(PreterminalRule preRule)
	{
		return lexicon.getPreRules().contains(preRule);
	}

	public HashSet<BinaryRule> getbRules()
	{
		return bRules;
	}

	public HashSet<UnaryRule> getuRules()
	{
		return uRules;
	}

	public Lexicon getLexicon()
	{
		return lexicon;
	}

	public HashSet<PreterminalRule> getPreRules()
	{
		return lexicon.getPreRules();
	}

	public void setNontermianalTable(NonterminalTable nonterminalTable)
	{
		this.nonterminalTable = nonterminalTable;
	}

	public boolean hasNonterminalTable()
	{
		return !(this.nonterminalTable == null);
	}

	public String getStartSymbol()
	{
		return StartSymbol;
	}

	public void setStartSymbol(String startSymbol)
	{
		StartSymbol = startSymbol;
	}

	public short getNumSymbol()
	{
		return nonterminalTable.getNumSymbol();
	}

	public Short[] allNonterminalIntValArr()
	{
		return nonterminalTable.getInt_strMap().keySet().toArray(new Short[nonterminalTable.getInt_strMap().size()]);
	}

	public ArrayList<Short> allPreterminal()
	{
		return nonterminalTable.getIntValueOfPreterminalArr();
	}

	public ArrayList<Short> getNumSubsymbolArr()
	{
		return nonterminalTable.getNumSubsymbolArr();
	}

	public void setNumSubsymbolArr(ArrayList<Short> newNumSubsymbolArr)
	{
		nonterminalTable.setNumSubsymbolArr(newNumSubsymbolArr);
	}

	public void setbRules(HashSet<BinaryRule> bRules)
	{
		this.bRules = bRules;
	}

	public void setuRules(HashSet<UnaryRule> uRules)
	{
		this.uRules = uRules;
	}

	public void setLexicon(Lexicon lexicon)
	{
		this.lexicon = lexicon;
	}

	public Set<PreterminalRule> getPreRuleSetBySameHead(short parent)
	{
		if (preRuleBySameHead.containsKey(parent))
			return preRuleBySameHead.get(parent).keySet();
		else
			return null;
	}

	public PreterminalRule getRule(PreterminalRule tempPreRule)
	{
		short parent = tempPreRule.getParent();
		if (preRuleBySameHead.containsKey(parent))
			return preRuleBySameHead.get(parent).get(tempPreRule);
		else
			return null;
	}

	public Set<BinaryRule> getbRuleSetBySameHead(short parent)
	{
		if (bRuleBySameHead.containsKey(parent))
			return bRuleBySameHead.get(parent).keySet();
		else
			return null;
	}

	public BinaryRule getRule(BinaryRule tempBRule)
	{
		short parent = tempBRule.getParent();
		if (bRuleBySameHead.containsKey(parent))
			return bRuleBySameHead.get(parent).get(tempBRule);
		else
			return null;
	}

	public Set<UnaryRule> getuRuleSetBySameHead(short parent)
	{
		if (uRuleBySameHead.containsKey(parent))
			return uRuleBySameHead.get(parent).keySet();
		else
			return null;
	}

	public UnaryRule getRule(UnaryRule tempURule)
	{
		short parent = tempURule.getParent();
		if (uRuleBySameHead.containsKey(parent))
			return uRuleBySameHead.get(parent).get(tempURule);
		else
			return null;
	}

	public void setSubTag2UNKScores(double[][] subTag2UNKScores)
	{
		lexicon.setSubTag2UNKScores(subTag2UNKScores);
	}

	public double getSubtag2UNKScores(short tag, short subT)
	{
		return getTag2UNKScores(tag)[subT];
	}

	public double[] getTag2UNKScores(short tag)
	{
		return lexicon.getSubTag2UNKScores()[tag];
	}

	public boolean isRareWord(String word)
	{
		return lexicon.getRareWord().contains(word);
	}

	public boolean hasPreterminalSymbol(short preterminalSymbol)
	{
		return nonterminalTable.hasPreterminalSymbol(preterminalSymbol);
	}

	// 非终结符及其分裂后子符号数
	public String toStringAllSymbol()
	{
		StringBuilder str1 = new StringBuilder("预终结符号：");
		for (short preterminalSym : this.allPreterminal())
		{
			str1 = str1.append(
					"[" + this.symbolStrValue(preterminalSym) + "," + this.getNumSubSymbol(preterminalSym) + "]" + " ");
		}
		str1.append("\n");
		
		str1.append("非预终结，其他符号：");
		for (short nonterminalSym : this.allNonterminalIntValArr())
		{
			if (!this.hasPreterminalSymbol(nonterminalSym))
			{
				str1.append("[" + this.symbolStrValue(nonterminalSym) + "," + this.getNumSubSymbol(nonterminalSym) + "]"
						+ " ");
			}
		}
		
		return str1.toString();
	}

	/**
	 * 将非终结符号是原始符号的Treenode的转换成对应的AnnotationTreeNode
	 * 
	 * @param tree
	 * @return
	 */
	public AnnotationTreeNode toAnnotationTreeNode(TreeNode tree)
	{
		return AnnotationTreeNode.getInstance(tree, nonterminalTable);
	}

	private TreeMap<String, Map<String, Double>[]> getSortedPreRules()
	{
		TreeMap<String, Map<String, Double>[]> allPreRules = new TreeMap<>();
		for (Short symbol : this.allNonterminalIntValArr())
		{
			if (this.hasPreterminalSymbol(symbol))
			{
				@SuppressWarnings("unchecked")
				Map<String, Double>[] preRulesBySameSubHead = new HashMap[this.getNumSubSymbol(symbol)];
				String symbolStr = this.symbolStrValue(symbol);
				short numSubSymbol = this.getNumSubSymbol(symbol);
				for (int i = 0; i < preRulesBySameSubHead.length; i++)
				{
					preRulesBySameSubHead[i] = new HashMap<String, Double>();
				}
				
				for (PreterminalRule preRule : this.getPreRuleSetBySameHead(symbol))
				{
					String[] subRuleOfpreRule = preRule.toStringRules(this);
					int c = subRuleOfpreRule.length / numSubSymbol;
					int index = -1;
					for (int j = 0; j < subRuleOfpreRule.length; j++)
					{
						if (j % c == 0)
							index++;
						preRulesBySameSubHead[index].put(subRuleOfpreRule[j], Double
								.parseDouble(subRuleOfpreRule[j].substring(subRuleOfpreRule[j].lastIndexOf(" "))));
					}
				}
				
				allPreRules.put(symbolStr, preRulesBySameSubHead);
			}
		}
		
		return allPreRules;
	}

	private TreeMap<String, Map<String, Double>[]> getSortedBAndURules()
	{
		TreeMap<String, Map<String, Double>[]> allBAndURules = new TreeMap<>();
		for (Short symbol : this.allNonterminalIntValArr())
		{
			if (!this.hasPreterminalSymbol(symbol))
			{
				String symbolStr = this.symbolStrValue(symbol);
				short numSubSymbol = this.getNumSubSymbol(symbol);
				@SuppressWarnings("unchecked")
				Map<String, Double>[] bAndURulesBySameSubHead = new HashMap[numSubSymbol];
				for (int i = 0; i < bAndURulesBySameSubHead.length; i++)
				{
					bAndURulesBySameSubHead[i] = new HashMap<String, Double>();
				}

				if (this.getbRuleSetBySameHead(symbol) != null)
				{
					for (BinaryRule bRule : this.getbRuleSetBySameHead(symbol))
					{
						String[] subRulesOfbRule = bRule.toStringRules(this);
						int c = subRulesOfbRule.length / numSubSymbol;
						int index = -1;
						for (int j = 0; j < subRulesOfbRule.length; j++)
						{
							if (j % c == 0)
								index++;
							bAndURulesBySameSubHead[index].put(subRulesOfbRule[j], Double.parseDouble(
									subRulesOfbRule[j].substring(subRulesOfbRule[j].lastIndexOf(" ") + 1)));
						}
					}
				}

				if (this.getuRuleSetBySameHead(symbol) != null)
				{
					for (UnaryRule uRule : this.getuRuleSetBySameHead(symbol))
					{
						String[] subRuleOfuRule = uRule.toStringRules(this);
						int c = subRuleOfuRule.length / numSubSymbol;
						int index = -1;
						for (int j = 0; j < subRuleOfuRule.length; j++)
						{
							if (j % c == 0)
								index++;
							bAndURulesBySameSubHead[index].put(subRuleOfuRule[j], Double
									.parseDouble(subRuleOfuRule[j].substring(subRuleOfuRule[j].lastIndexOf(" "))));
						}
					}
				}
				allBAndURules.put(symbolStr, bAndURulesBySameSubHead);
			}
		}
		return allBAndURules;
	}

	/*
	 * 讲语法规则转化为一条条的派生的规则，并且规则左侧是相同的派生符号的规则按照概率大小排序
	 */
	public String toString()
	{
		StringBuilder grammarStr = new StringBuilder();

		grammarStr.append("--起始符--" + "\n");
		grammarStr.append(this.getStartSymbol() + "\n");
		
		grammarStr.append("--非终结符集--" + "\n");
		for (int symbol = 0; symbol < this.getNumSymbol(); symbol++)
		{
			String sym = this.symbolStrValue((short) symbol);
			if (symbol != this.getNumSymbol() - 1)
			{
				sym += " ";
			}
			grammarStr.append(sym);
		}
		grammarStr.append("\n");
		
		for (int symbol = 0; symbol < this.getNumSymbol(); symbol++)
		{
			short numSubSymbol = this.getNumSubSymbol((short) symbol);
			String numStr = String.valueOf(numSubSymbol);
			if (symbol != this.getNumSymbol() - 1)
			{
				numStr += " ";
			}
			grammarStr.append(numStr);
		}
		grammarStr.append("\n");
		
		for (int i = 0; i < this.allPreterminal().size(); i++)
		{
			short preterminal = this.allPreterminal().get(i);
			String pretermianlStr = String.valueOf(preterminal);
			if (i != this.allPreterminal().size() - 1)
			{
				pretermianlStr += " ";
			}
			grammarStr.append(pretermianlStr);
		}
		grammarStr.append("\r");
		
		TreeMap<String, Map<String, Double>[]> allBAndURules = this.getSortedBAndURules();	
		grammarStr.append("--一元二元规则集--" + "\n");
		for (Map.Entry<String, Map<String, Double>[]> entry : allBAndURules.entrySet())
		{
			for (Map<String, Double> innerEntry : entry.getValue())
			{
				ArrayList<Map.Entry<String, Double>> list = new ArrayList<>(innerEntry.entrySet());
				sortList(list);
				for (Map.Entry<String, Double> subRule : list)
				{
					grammarStr.append(subRule.getKey() + "\n");
				}
			}
		}
		
		TreeMap<String, Map<String, Double>[]> allPreRules = this.getSortedPreRules();
		grammarStr.append("--预终结符规则集--" + "\n");
		for (Map.Entry<String, Map<String, Double>[]> entry : allPreRules.entrySet())
		{
			for (Map<String, Double> innerEntry : entry.getValue())
			{
				ArrayList<Map.Entry<String, Double>> list = new ArrayList<>(innerEntry.entrySet());
				sortList(list);
				for (Map.Entry<String, Double> subRule : list)
				{
					grammarStr.append(subRule.getKey() + "\n");
				}
			}
		}

		grammarStr.append("--end--");
		
		return grammarStr.toString();
	}

	private void sortList(ArrayList<Map.Entry<String, Double>> list)
	{
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>()
		{
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2)
			{
				return -o1.getValue().compareTo(o2.getValue());
			}
		});
	}

	public void read(PlainTextByLineStream stream) throws IOException
	{
		String str = stream.read();
		if (str != null)
			str = str.trim();
		String[] allSymbols = null;// ROOT、......
		Short[] numNonterminal = null;// ROOT 1
		Short[] preSymbolIndex = null;

		if (str.equals("--起始符--"))
		{
			this.setStartSymbol(stream.read().trim());
		}
		str = stream.read().trim();
		if (str.equals("--非终结符集--"))
		{
			if ((str = stream.read()) != null)
			{
				allSymbols = str.trim().split(" ");
			}
			if ((str = stream.read()) != null)
			{
				String[] numNonArr = str.trim().split(" ");
				numNonterminal = new Short[numNonArr.length];
				for (int i = 0; i < numNonArr.length; i++)
				{
					numNonterminal[i] = Short.parseShort(numNonArr[i]);
				}
			}
			if ((str = stream.read()) != null)
			{
				String indexArr[] = str.trim().split(" ");
				preSymbolIndex = new Short[indexArr.length];
				for (int i = 0; i < indexArr.length; i++)
				{
					preSymbolIndex[i] = Short.parseShort(indexArr[i]);
				}
			}
		}
		NonterminalTable nonterminalTable = new NonterminalTable();
		for (String symbol : allSymbols)
		{
			nonterminalTable.putSymbol(symbol);
		}
		nonterminalTable.setIntValueOfPreterminalArr(new ArrayList<Short>(Arrays.asList(preSymbolIndex)));
		nonterminalTable.setNumSubsymbolArr(new ArrayList<Short>(Arrays.asList(numNonterminal)));
		this.setNontermianalTable(nonterminalTable);
		str = stream.read();
		if (str != null)
			str = str.trim();
		String[] rule = null;
		if (str.equals("--一元二元规则集--"))
		{
			while ((str = stream.read()) != null && !str.equals("--预终结符规则集--"))
			{
				str = str.trim();
				rule = str.split(" ");
				if (rule.length == 4)
					this.readURule(rule);
				else if (rule.length == 5)
					this.readBRule(rule);
			}
		}

		if (str.equals("--预终结符规则集--"))
		{
			while ((str = stream.read()) != null)
			{
				str = str.trim();
				if(str.equals("--end--")) {
					break;
				}
				rule = str.split(" ");
				if (rule.length == 4)
					this.readPreRule(rule);
			}
		}
		stream.close();
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		TreeMap<String, Map<String, Double>[]> allBAndURules = this.getSortedBAndURules();
		TreeMap<String, Map<String, Double>[]> allPreRules = this.getSortedPreRules();

		out.writeUTF("--起始符--" + "\n");
		out.writeUTF(this.getStartSymbol() + "\n");
		
		out.writeUTF("--非终结符集--" + "\n");
		StringBuilder sym = new StringBuilder();
		for (int symbol = 0; symbol < this.getNumSymbol(); symbol++)
		{
			sym.append(this.symbolStrValue((short) symbol));
			if (symbol != this.getNumSymbol() - 1)
			{
				sym.append(" ");
			}
		}
		out.writeUTF(sym.toString() + "\n");
		
		StringBuilder numSubStr = new StringBuilder();
		for (int symbol = 0; symbol < this.getNumSymbol(); symbol++)
		{
			short numSubSymbol = this.getNumSubSymbol((short) symbol);
			numSubStr.append(String.valueOf(numSubSymbol));
			if (symbol != this.getNumSymbol() - 1)
			{
				numSubStr.append(" ");
			}
		}
		out.writeUTF(numSubStr.toString() + "\n");
		
		StringBuilder pretermianlStr = new StringBuilder();
		for (int i = 0; i < this.allPreterminal().size(); i++)
		{
			short preterminal = this.allPreterminal().get(i);
			pretermianlStr.append(String.valueOf(preterminal));
			if (i != this.allPreterminal().size() - 1)
			{
				pretermianlStr.append(" ");
			}

		}
		out.writeUTF(pretermianlStr.toString() + "\n");
		
		out.writeUTF("--一元二元规则集--" + "\n");
		for (Map.Entry<String, Map<String, Double>[]> entry : allBAndURules.entrySet())
		{
			for (Map<String, Double> innerEntry : entry.getValue())
			{
				ArrayList<Map.Entry<String, Double>> list = new ArrayList<>(innerEntry.entrySet());
				sortList(list);
				for (Map.Entry<String, Double> subRule : list)
				{
					out.writeUTF(subRule.getKey() + "\n");
				}
			}
		}
		
		out.writeUTF("--预终结符规则集--" + "\n");
		for (Map.Entry<String, Map<String, Double>[]> entry : allPreRules.entrySet())
		{
			for (Map<String, Double> innerEntry : entry.getValue())
			{
				ArrayList<Map.Entry<String, Double>> list = new ArrayList<>(innerEntry.entrySet());
				sortList(list);
				for (Map.Entry<String, Double> subRule : list)
				{
					out.writeUTF(subRule.getKey() + "\n");
				}
			}
		}
		out.writeUTF("--end--");
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		String str = in.readUTF();
		String[] allSymbols = null;// ROOT、......
		Short[] numNonterminal = null;// ROOT 1
		Short[] preSymbolIndex = null;

		if (str != null)
			str = str.trim();
		if (str.equals("--起始符--"))
		{
			this.setStartSymbol(in.readUTF().trim());
		}

		str = in.readUTF().trim();
		if (str.equals("--非终结符集--"))
		{
			if ((str = in.readUTF()) != null)
			{
				allSymbols = str.trim().split(" ");
			}
			if ((str = in.readUTF()) != null)
			{
				String[] numNonArr = str.trim().split(" ");
				numNonterminal = new Short[numNonArr.length];
				for (int i = 0; i < numNonArr.length; i++)
				{
					numNonterminal[i] = Short.parseShort(numNonArr[i]);
				}
			}
			if ((str = in.readUTF()) != null)
			{
				String indexArr[] = str.trim().split(" ");
				preSymbolIndex = new Short[indexArr.length];
				for (int i = 0; i < indexArr.length; i++)
				{
					preSymbolIndex[i] = Short.parseShort(indexArr[i]);
				}
			}
		}

		NonterminalTable nonterminalTable = new NonterminalTable();
		for (String symbol : allSymbols)
		{
			nonterminalTable.putSymbol(symbol);
		}
		nonterminalTable.setIntValueOfPreterminalArr(new ArrayList<Short>(Arrays.asList(preSymbolIndex)));
		nonterminalTable.setNumSubsymbolArr(new ArrayList<Short>(Arrays.asList(numNonterminal)));
		this.setNontermianalTable(nonterminalTable);

		str = in.readUTF();
		if (str != null)
			str = str.trim();
		String[] rule = null;
		if (str.equals("--一元二元规则集--"))
		{
			while ((str = in.readUTF()) != null)
			{
				str = str.trim();
				if (str.equals("--预终结符规则集--"))
					break;
				rule = str.split(" ");
				if (rule.length == 4)
					this.readURule(rule);
				else if (rule.length == 5)
					this.readBRule(rule);
			}
		}

		if (str.equals("--预终结符规则集--"))
		{
			while ((str = in.readUTF()) != null)
			{
				str = str.trim();
				if(str.equals("--end--")) {
					break;
				}
				rule = str.split(" ");
				if (rule.length == 4)
					this.readPreRule(rule);
			}
		}
	}
}
