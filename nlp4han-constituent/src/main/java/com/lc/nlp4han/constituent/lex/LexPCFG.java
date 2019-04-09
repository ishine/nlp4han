package com.lc.nlp4han.constituent.lex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.lc.nlp4han.constituent.GrammarWritable;

/**
 * 中心词驱动的上下无关文法（Collins Model1）
 * 
 * @author qyl
 *
 */
public class LexPCFG implements GrammarWritable
{
	// 句法树的起始符，该值为预处理句子时设置的起始符ROOT
	private String StartSymbol = null;

	// 词性标注集
	private HashSet<String> posSet = new HashSet<String>();

	// 词性标注和词，以及数目
	private HashMap<WordPOS, Integer> wordMap = new HashMap<WordPOS, Integer>();

	// 词在训练集中的pos集合
	private HashMap<String, HashSet<String>> posesOfWord = new HashMap<String, HashSet<String>>();

	// P(H|P,t,w)）相关的统计数据
	private HashMap<OccurenceCollins, RuleAmountsInfo> headGenMap = new HashMap<OccurenceCollins, RuleAmountsInfo>();

	private HashMap<OccurenceCollins, HashSet<String>> parentList = new HashMap<OccurenceCollins, HashSet<String>>();

	// 用于生成SidesChild(包含其中心word和pos)相关统计数据
	private HashMap<OccurenceCollins, RuleAmountsInfo> sidesGenMap = new HashMap<OccurenceCollins, RuleAmountsInfo>();

	// 用于生成Stop的相关统计数据
	private HashMap<OccurenceCollins, RuleAmountsInfo> stopGenMap = new HashMap<OccurenceCollins, RuleAmountsInfo>();

	// 用于生成并列结构连词（如CC或者逗号和冒号,为简略，我将生成修饰符pos和生成修饰符word都放入此规则
	private HashMap<OccurenceCollins, RuleAmountsInfo> specialGenMap = new HashMap<OccurenceCollins, RuleAmountsInfo>();

	public LexPCFG()
	{
	}

	public LexPCFG(String startSymbol, HashSet<String> posSet, HashMap<WordPOS, Integer> wordMap,
			HashMap<String, HashSet<String>> posesOfWord, HashMap<OccurenceCollins, RuleAmountsInfo> headGenMap,
			HashMap<OccurenceCollins, HashSet<String>> parentList,
			HashMap<OccurenceCollins, RuleAmountsInfo> sidesGenMap,
			HashMap<OccurenceCollins, RuleAmountsInfo> stopGenMap,
			HashMap<OccurenceCollins, RuleAmountsInfo> specialGenMap)
	{
		this.StartSymbol = startSymbol;
		this.posSet = posSet;
		this.wordMap = wordMap;
		this.posesOfWord = posesOfWord;
		this.headGenMap = headGenMap;
		this.parentList = parentList;
		this.sidesGenMap = sidesGenMap;
		this.stopGenMap = stopGenMap;
		this.specialGenMap = specialGenMap;
	}

	/**
	 * 得到词性标注集合
	 * 
	 * @return
	 */
	public HashSet<String> getPosSet()
	{
		return posSet;
	}

	/**
	 * 得到词在训练数据集中出现的pos集合
	 * 
	 * @param word
	 * @return
	 */
	public HashSet<String> getPosSetByword(String word)
	{
		return posesOfWord.get(word);
	}

	/**
	 * 由headChild得到可行的Parent/后者向上延伸的单元规则
	 * 
	 * @return
	 */
	public HashSet<String> getParentSet(OccurenceHeadChild rhcg)
	{
		return parentList.get(rhcg);
	}

	/**
	 * 得到P（H|P,(t,w)）的概率/可以用于计算单元规则的概率
	 * 
	 * @return
	 */
	public double getProbForGenerateHead(OccurenceHeadChild rhcg)
	{
		return getProbOfBackOff(rhcg, headGenMap, "head");
	}

	/**
	 * 得到生成Pl/Pr(Stop|P,H,(t,w),distance)的概率
	 * 
	 * @param rsg
	 * @return
	 */
	public double getProbForGenerateStop(OccurenceStop rsg)
	{
		return getProbOfBackOff(rsg, stopGenMap, "stop");
	}

	/**
	 * 得到生成两侧即Pl/Pr(L(lpos,lword)c,p|P,H,(hpos,hword),distance)的概率c和p分别指CC、标点符号（此处专指顿号）
	 * 
	 * @param sidesRule
	 * @return
	 */
	public double getProbForGenerateSides(OccurenceSides sr)
	{
		// 需要进行平滑运算将其分为两部分
		String parentLabel = sr.getParentLabel();// 父节点的非终结符标记
		String headPOS = sr.getHeadPOS();// 中心词词性标记,在NPB中为上一个修饰符的pos
		String headWord = sr.getHeadWord();// 中心词,在NPB中为上一个修饰符的word
		String headLabel = sr.getHeadLabel();// 中心节点标记
		int direction = sr.getDirection();// 头结点为0,左侧为1，右侧为2
		String sideLabel = sr.getSideLabel();// 所求孩子节点的标记
		String sideHeadPOS = sr.getSideHeadPOS();// 所求孩子节点的中心词词标记
		String sideHeadWord = sr.getSideHeadWord();// 所求的孩子节点的中心词
		boolean coor = sr.isCoor();// 并列结构
		boolean pu = sr.isPu();// 标点符号，由于只保留了顿号所以我们可以把它当做并列结构
		Distance distance = sr.getDistance();// 距离度量

		OccurenceSides rule1 = new OccurenceSides(headLabel, parentLabel, headPOS, headWord, direction, sideLabel,
				sideHeadPOS, null, coor, pu, distance);
		OccurenceSides rule2 = new OccurenceSides(headLabel, parentLabel, headPOS, headWord, direction, sideLabel,
				sideHeadPOS, sideHeadWord, coor, pu, distance);
		return getProbOfBackOff(rule1, sidesGenMap, "1side") * getProbOfBackOff(rule2, sidesGenMap, "2side");
	}

	/**
	 * 得到并列结构（CC）或者含有顿号结构的概率
	 * 即P(CC,word|P,leftLabel,rightLabel,leftWord,righrWord)的概率
	 * 
	 * @param specialRule
	 * @return
	 */
	public double getProbForSpecialCase(OccurenceSpecialCase specialRule)
	{
		return getProbOfBackOff(specialRule, specialGenMap, "special");
	}

	/**
	 * 通过回退模型得到概率
	 * 
	 * @param occur
	 * @param map
	 * @return
	 */
	private double getProbOfBackOff(OccurenceCollins occur, HashMap<OccurenceCollins, RuleAmountsInfo> map, String type)
	{
		double e1, e2, e3, w1, w2;
		e3 = 0;

		double[] pw1 = getProbAndWeight(occur, map, type);
		e1 = pw1[1];
		w1 = pw1[0];

		occur.setHeadWord(null);
		double[] pw2 = getProbAndWeight(occur, map, type);
		e2 = pw2[1];
		w2 = pw2[0];

		occur.setHeadPOS(null);
		if (type.equals("2side"))
		{
			OccurenceSides rs = (OccurenceSides) occur;
			double i = 0.02;// 未登录词的平滑值
			e3 = getProbForWord(rs, i);
		}
		else
		{
			double[] pw3 = getProbAndWeight(occur, map, type);
			e3 = pw3[1];
		}

		// 因为在收集文法时，规则相同，则计数累加，故需要乘以0.5
		if (type.equals("lside"))
		{
			e1 *= 0.5;
			e2 *= 0.5;
		}

		return w1 * e1 + (1 - w1) * (w2 * e2 + (1 - w2) * e3);
	}

	/**
	 * 
	 * @return
	 */
	private double getProbForWord(OccurenceSides rs, double count)
	{
		double e3 = 0;
		WordPOS wop = new WordPOS(rs.getSideHeadWord(), rs.getSideHeadPOS());
		if (wordMap.keySet().contains(wop))
			count = wordMap.get(wop);
		if (wordMap.containsKey(new WordPOS(null, rs.getSideHeadPOS())))
			e3 = count / wordMap.get(new WordPOS(null, rs.getSideHeadPOS()));

		return e3;
	}

	/**
	 * 得到某个回退模型的概率和权重
	 * 
	 * @return
	 */
	private double[] getProbAndWeight(OccurenceCollins occur, HashMap<OccurenceCollins, RuleAmountsInfo> map, String type)
	{
		int x, u, y;
		double[] pw = new double[2];
		y = 0;
		if (map.get(occur) == null)
		{
			pw[1] = 0;
		}
		else
		{
			x = map.get(occur).getAmount();
			switch (type)
			{
			case "head":
				OccurenceHeadChild rhcg1 = (OccurenceHeadChild) occur;
				rhcg1 = new OccurenceHeadChild(null, rhcg1.getParentLabel(), rhcg1.getHeadPOS(), rhcg1.getHeadWord());
				y = map.get(rhcg1).getAmount();
				u = map.get(rhcg1).getSubtypeAmount();
				break;
				
			case "1side":
				OccurenceSides rsg1 = (OccurenceSides) occur;
				rsg1 = new OccurenceSides(rsg1.getHeadLabel(), rsg1.getParentLabel(), rsg1.getHeadPOS(),
						rsg1.getHeadWord(), rsg1.getDirection(), null, null, rsg1.getSideHeadWord(), rsg1.isCoor(),
						rsg1.isPu(), rsg1.getDistance());
				y = map.get(rsg1).getAmount();
				u = map.get(rsg1).getSubtypeAmount();
				break;
				
			case "2side":
				OccurenceSides rsg2 = (OccurenceSides) occur;
				rsg2 = new OccurenceSides(rsg2.getHeadLabel(), rsg2.getParentLabel(), rsg2.getHeadPOS(),
						rsg2.getHeadWord(), rsg2.getDirection(), rsg2.getSideLabel(), rsg2.getSideHeadPOS(), null,
						rsg2.isCoor(), rsg2.isPu(), rsg2.getDistance());

				// 因为此处统计的次数为1side的分子和2side的分母，故统计了两次，所以此处需要乘以0.5
				y = map.get(rsg2).getAmount() * 1 / 2;
				u = map.get(rsg2).getSubtypeAmount();
				break;
				
			case "stop":
				y = 0;
				OccurenceStop rsg3 = (OccurenceStop) occur;
				rsg3 = new OccurenceStop(rsg3.getHeadLabel(), rsg3.getParentLabel(), rsg3.getHeadPOS(),
						rsg3.getHeadWord(), rsg3.getDirection(), true, rsg3.getDistance());
				if (map.containsKey(rsg3))
					y += map.get(rsg3).getAmount();

				rsg3.setStop(false);
				// 加第二次是因为stop只有两个子类，true和false
				if (map.containsKey(rsg3))
					y += map.get(rsg3).getAmount();

				u = 2;
				break;
				
			case "special":
				OccurenceSpecialCase rsg4 = (OccurenceSpecialCase) occur;
				rsg4 = new OccurenceSpecialCase(rsg4.getParentLabel(), null, null, rsg4.getLeftLabel(),
						rsg4.getRightLabel(), rsg4.getLheadWord(), rsg4.getRheadWord(), rsg4.getLheadPOS(),
						rsg4.getRheadPOS());
				y = map.get(rsg4).getAmount();
				u = map.get(rsg4).getSubtypeAmount();
				break;
				
			default:
				y = 0;
				u = 0;
			}
			
			// 若果分母为零，则回退模型的权重值位零
			if (y == 0)
				pw[0] = 0;
			else
				pw[0] = 1.0 * y / (y + 5 * u);

			pw[1] = 1.0 * x / y;
		}
		return pw;
	}

	public String getStartSymbol()
	{
		return StartSymbol;
	}

	public void setStartSymbol(String startSymbol)
	{
		StartSymbol = startSymbol;
	}

	public int getPOS2WordAmount(WordPOS pw)
	{
		return wordMap.get(pw);
	}

	public void setWordMap(HashMap<WordPOS, Integer> wordMap)
	{
		this.wordMap = wordMap;
	}

	public HashSet<String> getPosSetOfWord(String string)
	{
		return posesOfWord.get(string);
	}

	public void setPosesOfWord(HashMap<String, HashSet<String>> posesOfWord)
	{
		this.posesOfWord = posesOfWord;
	}

	public RuleAmountsInfo getHeadGenRuleAmountsInfo(OccurenceCollins oc)
	{
		return headGenMap.get(oc);
	}

	public void setHeadGenMap(HashMap<OccurenceCollins, RuleAmountsInfo> headGenMap)
	{
		this.headGenMap = headGenMap;
	}

	public HashSet<String> getParentList(OccurenceCollins oc)
	{
		return parentList.get(oc);
	}

	public void setParentList(HashMap<OccurenceCollins, HashSet<String>> parentList)
	{
		this.parentList = parentList;
	}

	public RuleAmountsInfo getSidesGenRuleAmountsInfo(OccurenceCollins oc)
	{
		return sidesGenMap.get(oc);
	}

	public void setSidesGeneratorMap(HashMap<OccurenceCollins, RuleAmountsInfo> sidesGenMap)
	{
		this.sidesGenMap = sidesGenMap;
	}

	public RuleAmountsInfo getStopGenRuleAmountsInfo(OccurenceCollins oc)
	{
		return stopGenMap.get(oc);
	}

	public void setStopGenMap(HashMap<OccurenceCollins, RuleAmountsInfo> stopGenMap)
	{
		this.stopGenMap = stopGenMap;
	}

	public RuleAmountsInfo getSpecialGenRuleAmountsInfo(OccurenceCollins oc)
	{
		return specialGenMap.get(oc);
	}

	public void setSpecialGenMap(HashMap<OccurenceCollins, RuleAmountsInfo> specialGenMap)
	{
		this.specialGenMap = specialGenMap;
	}

	public void setPosSet(HashSet<String> posSet)
	{
		this.posSet = posSet;
	}

	@Override
	public String toString()
	{
		StringBuilder stb = new StringBuilder();
		stb.append("--起始符--" + '\n');
		stb.append(this.getStartSymbol() + '\n');

		Iterator<String> itr1 = posSet.iterator();
		stb.append("--词性标注集--" + '\n');
		while (itr1.hasNext())
		{
			stb.append(itr1.next() + '\n');
		}

		Set<WordPOS> wap = wordMap.keySet();
		stb.append("--POS-Word集--" + '\n');
		for (WordPOS wap1 : wap)
		{
			stb.append(wap1.toString() + " " + wordMap.get(wap1) + '\n');
		}

		stb.append("--生成头结点的规则集--" + '\n');
		Set<OccurenceCollins> set = headGenMap.keySet();
		for (OccurenceCollins rule : set)
		{
			OccurenceHeadChild rule1 = (OccurenceHeadChild) rule;
			stb.append(rule1.toString() + " " + headGenMap.get(rule1).toString() + '\n');
		}

		stb.append("--头结点向上延伸的标记集--" + '\n');
		Set<OccurenceCollins> set1 = parentList.keySet();
		for (OccurenceCollins rule : set1)
		{
			OccurenceHeadChild rule1 = (OccurenceHeadChild) rule;
			stb.append(rule1.toString() + " ");
			for (String str : parentList.get(rule1))
			{
				stb.append(str.toString() + " ");
			}
			stb.append('\n');
		}

		stb.append("--生成两侧孩子的规则集--" + '\n');
		Set<OccurenceCollins> set2 = sidesGenMap.keySet();
		for (OccurenceCollins rule : set2)
		{
			OccurenceHeadChild rule1 = (OccurenceHeadChild) rule;
			stb.append(rule1.toString() + " " + sidesGenMap.get(rule1).toString() + '\n');
		}

		stb.append("--生成两侧Stop的规则集--" + '\n');
		Set<OccurenceCollins> set3 = stopGenMap.keySet();
		for (OccurenceCollins rule : set3)
		{
			OccurenceHeadChild rule1 = (OccurenceHeadChild) rule;
			stb.append(rule1.toString() + " " + stopGenMap.get(rule1).toString() + '\n');
		}

		stb.append("--特殊规则集--" + '\n');
		Set<OccurenceCollins> set4 = specialGenMap.keySet();
		for (OccurenceCollins rule : set4)
		{
			OccurenceHeadChild rule1 = (OccurenceHeadChild) rule;
			stb.append(rule1.toString() + " " + specialGenMap.get(rule1).toString() + '\n');
		}
		stb.append("完");
		return stb.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((StartSymbol == null) ? 0 : StartSymbol.hashCode());
		result = prime * result + ((headGenMap == null) ? 0 : headGenMap.hashCode());
		result = prime * result + ((parentList == null) ? 0 : parentList.hashCode());
		result = prime * result + ((posSet == null) ? 0 : posSet.hashCode());
		result = prime * result + ((posesOfWord == null) ? 0 : posesOfWord.hashCode());
		result = prime * result + ((sidesGenMap == null) ? 0 : sidesGenMap.hashCode());
		result = prime * result + ((specialGenMap == null) ? 0 : specialGenMap.hashCode());
		result = prime * result + ((stopGenMap == null) ? 0 : stopGenMap.hashCode());
		result = prime * result + ((wordMap == null) ? 0 : wordMap.hashCode());
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
		LexPCFG other = (LexPCFG) obj;
		if (StartSymbol == null)
		{
			if (other.StartSymbol != null)
				return false;
		}
		else if (!StartSymbol.equals(other.StartSymbol))
			return false;
		if (headGenMap == null)
		{
			if (other.headGenMap != null)
				return false;
		}
		else if (!headGenMap.equals(other.headGenMap))
			return false;
		if (parentList == null)
		{
			if (other.parentList != null)
				return false;
		}
		else if (!parentList.equals(other.parentList))
			return false;
		if (posSet == null)
		{
			if (other.posSet != null)
				return false;
		}
		else if (!posSet.equals(other.posSet))
			return false;
		if (posesOfWord == null)
		{
			if (other.posesOfWord != null)
				return false;
		}
		else if (!posesOfWord.equals(other.posesOfWord))
			return false;
		if (sidesGenMap == null)
		{
			if (other.sidesGenMap != null)
				return false;
		}
		else if (!sidesGenMap.equals(other.sidesGenMap))
			return false;
		if (specialGenMap == null)
		{
			if (other.specialGenMap != null)
				return false;
		}
		else if (!specialGenMap.equals(other.specialGenMap))
			return false;
		if (stopGenMap == null)
		{
			if (other.stopGenMap != null)
				return false;
		}
		else if (!stopGenMap.equals(other.stopGenMap))
			return false;
		if (wordMap == null)
		{
			if (other.wordMap != null)
				return false;
		}
		else if (!wordMap.equals(other.wordMap))
			return false;
		return true;
	}

	/**
	 * 将模型写出
	 */
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF("--起始符--");
		out.writeUTF(this.getStartSymbol());

		Iterator<String> itr1 = posSet.iterator();
		out.writeUTF("--词性标注集--");
		while (itr1.hasNext())
		{
			out.writeUTF(itr1.next());
		}

		Set<WordPOS> wap = wordMap.keySet();
		out.writeUTF("--POS-Word集--");
		for (WordPOS wap1 : wap)
		{
			out.writeUTF(wap1.toString() + " " + wordMap.get(wap1));
		}

		out.writeUTF("--生成头结点的规则集--");
		Set<OccurenceCollins> set = headGenMap.keySet();
		for (OccurenceCollins rule : set)
		{
			OccurenceHeadChild rule1 = (OccurenceHeadChild) rule;
			out.writeUTF(rule1.toString() + " " + headGenMap.get(rule1).toString());
		}

		out.writeUTF("--头结点向上延伸的标记集--");
		Set<OccurenceCollins> set1 = parentList.keySet();
		for (OccurenceCollins rule : set1)
		{
			OccurenceHeadChild rule1 = (OccurenceHeadChild) rule;
			StringBuffer stb = new StringBuffer();
			stb.append(rule1.toString() + " ");
			for (String str : parentList.get(rule1))
			{
				stb.append(str.toString() + " ");
			}
			out.writeUTF(stb.toString());
		}

		out.writeUTF("--生成两侧孩子的规则集--");
		Set<OccurenceCollins> set2 = sidesGenMap.keySet();
		for (OccurenceCollins rule : set2)
		{
			OccurenceHeadChild rule1 = (OccurenceHeadChild) rule;
			out.writeUTF(rule1.toString() + " " + sidesGenMap.get(rule1).toString());
		}

		out.writeUTF("--生成两侧Stop的规则集--");
		Set<OccurenceCollins> set3 = stopGenMap.keySet();
		for (OccurenceCollins rule : set3)
		{
			OccurenceHeadChild rule1 = (OccurenceHeadChild) rule;
			out.writeUTF(rule1.toString() + " " + stopGenMap.get(rule1).toString());
		}

		out.writeUTF("--特殊规则集--");
		Set<OccurenceCollins> set4 = specialGenMap.keySet();
		for (OccurenceCollins rule : set4)
		{
			OccurenceHeadChild rule1 = (OccurenceHeadChild) rule;
			out.writeUTF(rule1.toString() + " " + specialGenMap.get(rule1).toString());
		}
		out.writeUTF("完");
	}

	/**
	 * 从流中读取模型
	 */
	@Override
	public void read(DataInput in) throws IOException
	{
		String str = in.readUTF().trim();
		if (str.equals("--起始符--"))
		{
			setStartSymbol(in.readUTF().trim());
		}
		in.readUTF();// 此处为"--词性标注集--"

		str = in.readUTF().trim();
		while (!str.equals("--POS-Word集--"))
		{// 添加词性标注集
			posSet.add(str);

			str = in.readUTF().trim();
		}

		str = in.readUTF().trim();
		while (!str.equals("--生成头结点的规则集--"))
		{// POS-Word集
			String[] strs = str.split(" ");
			wordMap.put(new WordPOS(strs[0], strs[1]), Integer.parseInt(strs[2]));

			str = in.readUTF().trim();
		}

		str = in.readUTF().trim();
		while (!str.equals("--头结点向上延伸的标记集--"))
		{// 生成头结点的规则集
			String[] strs = str.split(" ");
			int amount = Integer.parseInt(strs[strs.length - 2]);
			int sort = Integer.parseInt(strs[strs.length - 1]);
			headGenMap.put(new OccurenceHeadChild(strs), new RuleAmountsInfo(amount, sort));

			str = in.readUTF().trim();
		}

		str = in.readUTF().trim();
		while (!str.equals("--生成两侧孩子的规则集--"))
		{// 头结点向上延伸的标记集
			String[] strs = str.split(" ");
			HashSet<String> set = new HashSet<String>();
			for (int i = 4; i < strs.length; i++)
			{
				set.add(strs[i]);
			}
			parentList.put(new OccurenceHeadChild(strs), set);

			str = in.readUTF().trim();
		}

		str = in.readUTF().trim();
		while (!str.equals("--生成两侧Stop的规则集--"))
		{// 生成两侧孩子的规则集
			String[] strs = str.split(" ");
			int amount = Integer.parseInt(strs[strs.length - 2]);
			int subtypesAmount = Integer.parseInt(strs[strs.length - 1]);
			sidesGenMap.put(new OccurenceSides(strs), new RuleAmountsInfo(amount, subtypesAmount));

			str = in.readUTF().trim();
		}

		str = in.readUTF().trim();
		while (!str.equals("--特殊规则集--"))
		{// 生成两侧Stop的规则集
			String[] strs = str.split(" ");
			int amount = 1;
			amount = Integer.parseInt(strs[strs.length - 2]);
			int sort = Integer.parseInt(strs[strs.length - 1]);
			stopGenMap.put(new OccurenceStop(strs), new RuleAmountsInfo(amount, sort));

			str = in.readUTF().trim();
		}

		str = in.readUTF().trim();
		while (!str.equals("完") && str != null)
		{// 特殊规则集
			String[] strs = str.split(" ");
			int amount = Integer.parseInt(strs[strs.length - 2]);
			int sort = Integer.parseInt(strs[strs.length - 1]);
			specialGenMap.put(new OccurenceSpecialCase(strs), new RuleAmountsInfo(amount, sort));

			str = in.readUTF().trim();
		}
	}
}
