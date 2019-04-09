package com.lc.nlp4han.constituent.pcfg;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.lc.nlp4han.constituent.GrammarWritable;

/**
 * 上下文无关文法
 * 
 * 包含：开始符，重写规则，非终结符集，终结符集
 */
public class CFG implements GrammarWritable
{
	private String startSymbol = null;
	protected HashMap<String, Double> posProb = new HashMap<String, Double>();// 词性标注-概率映射
	private Set<String> nonTerminalSet = new HashSet<String>();// 非终结符集
	private Set<String> terminalSet = new HashSet<String>();// 终结符集
	private Set<RewriteRule> ruleSet = new HashSet<RewriteRule>();// 规则集

	private HashMap<String, HashSet<RewriteRule>> LHS2Rules = new HashMap<String, HashSet<RewriteRule>>();// 以左部为key值的规则集map
	private HashMap<ArrayList<String>, HashSet<RewriteRule>> RHS2Rules = new HashMap<ArrayList<String>, HashSet<RewriteRule>>();// 以规则右部为key值的规则集map
	
	/**
	 * 通过一步一步添加rule来实现规则集，终结符/非终结符的更新
	 */
	public CFG()
	{

	}

	/**
	 * 构造函数,一步创建
	 */
	public CFG(String startSymbol, Set<String> nonTerminalSet, Set<String> terminalSet, HashMap<String, Double> posMap,
			HashMap<String, HashSet<RewriteRule>> ruleMapStartWithlhs,
			HashMap<ArrayList<String>, HashSet<RewriteRule>> ruleMapStartWithrhs)
	{
		this.startSymbol = startSymbol;
		this.nonTerminalSet = nonTerminalSet;
		this.terminalSet = terminalSet;
		this.posProb = posMap;

		this.LHS2Rules = ruleMapStartWithlhs;
		this.RHS2Rules = ruleMapStartWithrhs;

		for (String lhs : ruleMapStartWithlhs.keySet())
		{
			ruleSet.addAll(ruleMapStartWithlhs.get(lhs));
		}
		this.posProb = getPosSet(ruleSet);
	}

	public CFG(String startSymbol, HashMap<String, Double> posMap, Set<String> nonTerminalSet, Set<String> terminalSet,
			Set<RewriteRule> ruleSet, HashMap<String, HashSet<RewriteRule>> lHS2Rules,
			HashMap<ArrayList<String>, HashSet<RewriteRule>> rHS2Rules)
	{
		this.startSymbol = startSymbol;
		this.posProb = posMap;
		this.nonTerminalSet = nonTerminalSet;
		this.terminalSet = terminalSet;
		this.ruleSet = ruleSet;
		LHS2Rules = lHS2Rules;
		RHS2Rules = rHS2Rules;
	}

	/**
	 * 构造时缺少LHS2Rules，RHS2Rules，并通过遍历ruleSet进行添加
	 * 
	 * @param startSymbol
	 * @param posMap
	 * @param nonTerminalSet
	 * @param terminalSet
	 * @param ruleSet
	 */
	public CFG(String startSymbol, HashMap<String, Double> posMap, Set<String> nonTerminalSet, Set<String> terminalSet,
			Set<RewriteRule> ruleSet)
	{
		this.startSymbol = startSymbol;
		this.posProb = posMap;
		this.nonTerminalSet = nonTerminalSet;
		this.terminalSet = terminalSet;
		for (RewriteRule rule : ruleSet)
			add(rule);
	}

	/**
	 * 构造函数，通过起始符，终结符集，非终结符集，规则集一步构造
	 * 
	 * @param startSymbol
	 * @param nonTerminalSet
	 * @param terminalSet
	 * @param rules
	 */
	public CFG(String startSymbol, Set<String> nonTerminalSet, Set<String> terminalSet, HashSet<RewriteRule> rules)
	{
		this.startSymbol = startSymbol;
		this.nonTerminalSet = nonTerminalSet;
		this.terminalSet = terminalSet;
		for (RewriteRule rule : rules)
		{
			add(rule);
		}
	}

	/**
	 * 从文本流中加载CFG文法，此接口可以完成从资源文本流和文件文本流中获得CFG文法
	 * 
	 * @param in 文本流
	 * @param encoding
	 * @throws IOException
	 */
	public CFG(InputStream in, String encoding) throws IOException
	{
		readGrammar(in, encoding);
	}

	/**
	 * 从文本流中加载CFG/PCFG文法，此接口可以完成从资源文本流和文件文本流中获得CFG/PCFG文法
	 * 
	 * @param in
	 * @param encoding
	 * @throws IOException
	 */
	public void readGrammar(InputStream in, String encoding) throws IOException
	{
		BufferedReader buffer = new BufferedReader(new InputStreamReader(in, encoding));
		String str = buffer.readLine().trim();
		if (str.equals("--起始符--"))
		{
			setStartSymbol(buffer.readLine().trim());
		}

		buffer.readLine();
		str = buffer.readLine().trim();
		while (!str.equals("--终结符集--"))
		{
			addNonTerminal(str);
			str = buffer.readLine().trim();
		}

		str = buffer.readLine();
		while (!str.equals("--词性标注映射--"))
		{
			addTerminal(str);
			str = buffer.readLine().trim();
		}

		str = buffer.readLine().trim();
		while (!str.equals("--规则集--"))
		{
			String[] pos = str.split("=");
			posProb.put(pos[0], Double.parseDouble(pos[1]));
			str = buffer.readLine().trim();
		}

		str = buffer.readLine();
		while (str != null)
		{
			str = str.trim();
			add(readRule(str));

			str = buffer.readLine();
		}

		buffer.close();
	}

	protected RewriteRule readRule(String ruleStr)
	{
		return new RewriteRule(ruleStr);
	}

	/**
	 * 判断是否为CNF,将词性标注作为解析规则的最底层
	 */
	public boolean IsCNF()
	{
		Set<String> set = posProb.keySet();
		boolean isCNF = true;
		for (RewriteRule rule : ruleSet)
		{
			ArrayList<String> list = rule.getRHS();
			if (list.size() >= 3)
			{
				isCNF = false;
				break;
			}

			if (list.size() == 2)
			{
				for (String string : list)
				{
					if (!nonTerminalSet.contains(string))
					{
						isCNF = false;
						break;
					}
				}
			}

			if (list.size() == 1)
			{
				String string = list.get(0);
				if (!set.contains(string) && !terminalSet.contains(string))
				{
					isCNF = false;
					break;
				}
			}
		}

		return isCNF;
	}

	/**
	 * 判断文法是宽松CNF文法
	 * 
	 * 宽松CNF文法允许A->B
	 */
	public boolean isLooseCNF()
	{
		boolean isLosseCNF = true;
		for (RewriteRule rule : ruleSet)
		{
			ArrayList<String> list = rule.getRHS();
			if (list.size() >= 3)
			{
				isLosseCNF = false;
				break;
			}

			if (list.size() == 2)
			{
				for (String string : list)
				{
					if (!nonTerminalSet.contains(string))
					{
						isLosseCNF = false;
						break;
					}
				}
			}
		}
		return isLosseCNF;
	}

	public boolean isNoTerminal(String symbol)
	{
		return nonTerminalSet.contains(symbol);
	}

	public boolean isTerminal(String symbol)
	{
		return terminalSet.contains(symbol);
	}

	public String getStartSymbol()
	{
		return startSymbol;
	}

	public void setStartSymbol(String startSymbol)
	{
		this.startSymbol = startSymbol;
	}

	public Set<String> getNonTerminalSet()
	{
		return nonTerminalSet;
	}

	public void setNonTerminalSet(Set<String> nonTerminalSet)
	{
		this.nonTerminalSet = nonTerminalSet;
	}

	public Set<String> getTerminalSet()
	{
		return terminalSet;
	}

	public void setTerminalSet(Set<String> terminalSet)
	{
		this.terminalSet = terminalSet;
	}

	public void setLHS2Rules(HashMap<String, HashSet<RewriteRule>> lHS2Rules)
	{
		LHS2Rules = lHS2Rules;
	}

	public HashMap<ArrayList<String>, HashSet<RewriteRule>> getRHS2Rules()
	{
		return RHS2Rules;
	}

	public void setRHS2Rules(HashMap<ArrayList<String>, HashSet<RewriteRule>> rHS2Rules)
	{
		RHS2Rules = rHS2Rules;
	}

	public void setRuleSet(Set<RewriteRule> ruleSet)
	{
		this.ruleSet = ruleSet;
	}
	
	public boolean containsRule(RewriteRule r)
	{
		return this.ruleSet.contains(r);
	}

	/**
	 * CFG专用，从规则集中得到词性标注集
	 * 
	 * @param ruleSet
	 * @return
	 */
	private HashMap<String, Double> getPosSet(Set<RewriteRule> ruleSet)
	{
		HashMap<String, Double> posMap1 = new HashMap<String, Double>();
		HashSet<String> posSet = new HashSet<String>();
		for (RewriteRule rule : ruleSet)
		{
			if (rule.getRHS().size() == 1 && terminalSet.contains(rule.getRHS().get(0)))
			{
				posSet.add(rule.getLHS());
			}
		}
		for (String pos : posSet)
		{
			posMap1.put(pos, 0.0);
		}
		return posMap1;
	}

	/**
	 * 添加单个规则
	 */
	public void add(RewriteRule rule)
	{
		ruleSet.add(rule);
		if (LHS2Rules.get(rule.getLHS()) != null)
		{
			LHS2Rules.get(rule.getLHS()).add(rule);
		}
		else
		{
			HashSet<RewriteRule> set = new HashSet<RewriteRule>();
			set.add(rule);
			LHS2Rules.put(rule.getLHS(), set);
		}

		if (RHS2Rules.keySet().contains(rule.getRHS()))
		{
			RHS2Rules.get(rule.getRHS()).add(rule);
		}
		else
		{
			HashSet<RewriteRule> set = new HashSet<RewriteRule>();
			set.add(rule);
			RHS2Rules.put(rule.getRHS(), set);
		}
	}

	/**
	 * 得到规则集
	 */
	public Set<RewriteRule> getRuleSet()
	{
		return ruleSet;
	}

	/**
	 * 添加非终结符
	 */
	public void addNonTerminal(String nonTer)
	{
		nonTerminalSet.add(nonTer);
	}

	/**
	 * 添加终结符
	 * 
	 * @param terminal
	 *            终结符
	 */
	public void addTerminal(String terminal)
	{
		terminalSet.add(terminal);
	}

	/**
	 * 根据规则左部得到所有对应规则
	 * 
	 * @param lhs
	 *            左侧字符串
	 */
	public Set<RewriteRule> getRuleByLHS(String lhs)
	{
		if(LHS2Rules.containsKey(lhs))
			return LHS2Rules.get(lhs);
		else
			return new HashSet<RewriteRule>();
	}

	/**
	 * 根据规则右部得到所有对应规则
	 * 
	 * @param rhsList
	 *            右部字符串列表
	 * @return 字符串集合
	 */
	public Set<RewriteRule> getRuleByRHS(ArrayList<String> rhsList)
	{
		if(RHS2Rules.containsKey(rhsList))
			return RHS2Rules.get(rhsList);
		else
			return new HashSet<RewriteRule>();
	}

	/**
	 * 根据规则右部得到所有对应规则
	 * 
	 * @param args
	 *            右部字符串
	 * @return 字符串集合
	 */
	public Set<RewriteRule> getRuleByRHS(String... args)
	{
		ArrayList<String> list = new ArrayList<String>();
		for (String string : args)
		{
			list.add(string);
		}

		return RHS2Rules.get(list);
	}

	/**
	 * 得到词性标注集合
	 * 
	 * @return
	 */
	public HashSet<String> getPosSet()
	{
		return new HashSet<String>(posProb.keySet());
	}

	/**
	 * 设置词性标注的集合及对应的概率
	 */
	public void setPosProb(HashMap<String, Double> posMap)
	{
		this.posProb = posMap;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nonTerminalSet == null) ? 0 : nonTerminalSet.hashCode());
		result = prime * result + ((ruleSet == null) ? 0 : ruleSet.hashCode());
		result = prime * result + ((startSymbol == null) ? 0 : startSymbol.hashCode());
		result = prime * result + ((terminalSet == null) ? 0 : terminalSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		CFG other = (CFG) obj;

		if (nonTerminalSet == null)
		{
			if (other.nonTerminalSet != null)
				return false;
		}
		else if (!nonTerminalSet.equals(other.nonTerminalSet))
			return false;

		if (ruleSet == null)
		{
			if (other.ruleSet != null)
				return false;
		}
		else if (!ruleSet.equals(other.ruleSet))
			return false;

		if (startSymbol == null)
		{
			if (other.startSymbol != null)
				return false;
		}
		else if (!startSymbol.equals(other.startSymbol))
			return false;

		if (terminalSet == null)
		{
			if (other.terminalSet != null)
				return false;
		}
		else if (!terminalSet.equals(other.terminalSet))
			return false;

		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder stb = new StringBuilder();
		Iterator<String> itr1 = nonTerminalSet.iterator();
		stb.append("--起始符--" + '\n');
		stb.append(this.getStartSymbol() + '\n');

		stb.append("--非终结符集--" + '\n');
		while (itr1.hasNext())
		{
			stb.append(itr1.next() + '\n');
		}

		Iterator<String> itr2 = terminalSet.iterator();
		stb.append("--终结符集--" + '\n');
		while (itr2.hasNext())
		{
			stb.append(itr2.next() + '\n');
		}

		stb.append("--词性标注映射--" + '\n');
		for (String string : posProb.keySet())
		{
			stb.append(string + "=" + posProb.get(string) + '\n');
		}

		stb.append("--规则集--" + '\n');
		Set<String> set = LHS2Rules.keySet();
		for (String string : set)
		{
			HashSet<RewriteRule> ruleSet = LHS2Rules.get(string);
			Iterator<RewriteRule> itr3 = ruleSet.iterator();
			while (itr3.hasNext())
			{
				stb.append(itr3.next().toString() + '\n');
			}
		}
		return stb.toString();
	}

	/**
	 * 将CFG文法模型(二进制)的内容写入到out
	 */
	@Override
	public void write(DataOutput out) throws IOException
	{
		/**
		 * 写入起始符
		 */
		out.writeUTF("--起始符--");
		out.writeUTF(startSymbol);

		/**
		 * 写入非终结符
		 */
		out.writeUTF("--非终结符集--");
		for (String nonter : nonTerminalSet)
		{
			out.writeUTF(nonter);
		}

		/**
		 * 写入终结符
		 */
		out.writeUTF("--终结符集--");
		for (String ter : terminalSet)
		{
			out.writeUTF(ter);
		}

		/**
		 * 写入词性标注映射
		 */
		out.writeUTF("--词性标注映射--");
		for (String pos : posProb.keySet())
		{
			out.writeUTF(pos + "=" + posProb.get(pos));
		}

		/**
		 * 写入规则集
		 */
		out.writeUTF("--规则集--");
		for (RewriteRule rule : ruleSet)
		{
			out.writeUTF(rule.toString());
		}

		out.writeUTF("完");
	}

	/**
	 * 从out中读入文法模型内容
	 */
	@Override
	public void read(DataInput in) throws IOException
	{

		String str = in.readUTF();
		if (str.equals("--起始符--"))
		{
			startSymbol = in.readUTF();
		}
		in.readUTF();// 此行为"--非终结符--"，不处理

		str = in.readUTF();
		while (!str.equals("--终结符集--"))
		{
			nonTerminalSet.add(str);
			str = in.readUTF();
		}

		str = in.readUTF();
		while (!str.equals("--词性标注映射--"))
		{
			terminalSet.add(str);
			str = in.readUTF();
		}

		str = in.readUTF();
		while (!str.equals("--规则集--"))
		{
			String[] strs = str.split("=");
			posProb.put(strs[0], Double.parseDouble(strs[1]));
			str = in.readUTF();
		}

		str = in.readUTF();
		while (!str.equals("完") && str != null)
		{
			add(readRule(str));
			str = in.readUTF();
		}
	}
}