package com.lc.nlp4han.constituent.pcfg;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.ConstituentParser;
import com.lc.nlp4han.constituent.ConstituentTree;
import com.lc.nlp4han.constituent.TreeNode;

/**
 * 基于宽松PCNF的CKY句法解析器
 * 
 * 宽松PCNF允许A->B形式的规则
 *
 */
public class ConstituentParserCKYLoosePCNF implements ConstituentParser
{
	private ChartEntry[][] chart;// 存储在该点的映射表

	private PCFG pcnf;

	private double pruneThreshold;// 剪枝阈值
	private boolean secondPrune;// 是否进行二次解析
	private boolean prior;// 是否在解析中

	public ConstituentParserCKYLoosePCNF(PCFG pcnf, double pruneThreshold, boolean secondPrune, boolean prior)
			throws UncompatibleGrammar
	{
		if (!pcnf.isLooseCNF())
			throw new UncompatibleGrammar();

		this.pruneThreshold = pruneThreshold;
		this.secondPrune = secondPrune;
		this.pcnf = pcnf;
		this.prior = prior;
	}

	/**
	 * 得到概率最高的成分树
	 * 
	 * @param words
	 *            分词序列
	 * @param poses
	 *            词性标记
	 * @return
	 */
	@Override
	public ConstituentTree parse(String[] words, String[] poses)
	{
		// return getParseResult(words, poses, 1)[0];

		ConstituentTree[] trees = getParseResult(words, poses, 1);

		if (trees!=null)
			return trees[0];
		else
			return null;
	}

	/**
	 * 得到概率最高的成分树
	 * 
	 * @param words
	 * 
	 * @return
	 */
	public ConstituentTree parse(String[] words)
	{
		ConstituentTree[] trees = getParseResult(words, null, 1);

		if (trees!=null)
			return trees[0];
		else
			return null;
	}

	/**
	 * 得到概率最高k个的成分树
	 * 
	 * @param words
	 *            分词序列
	 * @param poses
	 *            词性标记
	 * @param k
	 *            结果数目
	 * @return
	 */
	@Override
	public ConstituentTree[] parse(String[] words, String[] poses, int k)
	{
		return getParseResult(words, poses, k);
	}

	/**
	 * 得到概率最高k个的成分树
	 * 
	 * @param words
	 *            分词序列
	 * @param k
	 *            结果数目
	 * @return
	 */
	public ConstituentTree[] parse(String[] words, int k)
	{
		return getParseResult(words, null, k);
	}

	/**
	 * 得到成分树数组的通用方法
	 * 
	 * @param words
	 *            分词序列
	 * @param poses
	 *            词性标记
	 * @param k
	 *            结果数目
	 * @return
	 */
	private ConstituentTree[] getParseResult(String[] words, String[] poses, int k)
	{
		ArrayList<String> bracketList = parseCKY(words, poses, k, true);

		if (secondPrune && bracketList.size() == 0 && words.length <= 50)
		{
			bracketList = parseCKY(words, poses, k, false);
		}
		
		if(bracketList.size() == 0)
			return null;

		// int i = 0;
		// ConstituentTree[] treeArray = new ConstituentTree[k];
		ArrayList<ConstituentTree> treeArray = new ArrayList<ConstituentTree>();
		for (String bracketString : bracketList)
		{
			TreeNode rootNode = TreeRestorer.restoreTree(BracketExpUtil.generateTree(bracketString));

			// treeArray[i++] = new ConstituentTree(rootNode);
			treeArray.add(new ConstituentTree(rootNode));
		}

		return treeArray.toArray(new ConstituentTree[treeArray.size()]);
	}

	/**
	 * CKY算法的具体函数
	 * 
	 * @param words
	 *            分词序列
	 * 
	 * @param pos
	 *            词性标注
	 * 
	 * @param numOfResulets
	 *            需要求的结果数
	 * 
	 * @return 输出k个句子解析结果
	 */
	private ArrayList<String> parseCKY(String[] words, String[] pos, int numOfResulets, boolean prune)
	{
		// 初始化
		initializeChart(words, pos, numOfResulets);

		int n = words.length;
		// 填充chart图中的边
		for (int span = 2; span <= n; span++)
		{
			for (int i = 0; i <= n - span; i++)
			{
				int j = i + span;
				for (int k = i + 1; k <= j - 1; k++)
				{// 遍历table[i][k]和table[k][j]中的映射表，更新table[i][j]和back[i][j]
					updateTable(i, k, j, n, numOfResulets);
				}

				// 剪枝
				if (prune)
					pruneEdge(i, j);

			}
		}

		// 回溯并生成括号表达式列表,此刻的树并未还原为宾州树库的形式
		return getBracketList(n, numOfResulets);
	}

	/**
	 * 剪枝
	 * 
	 * @param i
	 * @param j
	 */
	private void pruneEdge(int i, int j)
	{
		HashMap<String, ArrayList<CKYPRule>> symbol2RulesMap = chart[i][j].getPruleMap();	
		HashMap<String, Double> priorProbMap = new HashMap<String, Double>();

		double bestPro = -1.0;
		for (String str : symbol2RulesMap.keySet())
		{
			double pro = 1;
			// 添加先验概率
			if (prior)
			{
				HashSet<String> posSet = pcnf.getPosSet();
				if (str.contains("@"))
				{
					String strs[] = str.split("@");
					for (String str0 : strs)
					{
						if (!posSet.contains(str0))
							break;

						pro *= pcnf.getPosPro(str0);
					}
				}
				else if (str.contains("&"))
				{
					String strs[] = str.split("&");
					for (String str0 : strs)
					{
						if (!posSet.contains(str0))
							break;

						pro *= pcnf.getPosPro(str0);
					}
				}
			}

			priorProbMap.put(str, pro);

			if (symbol2RulesMap.get(str).get(0).getProb() * pro > bestPro)
				bestPro = symbol2RulesMap.get(str).get(0).getProb();
		}

		ArrayList<String> deleteList = new ArrayList<String>();
		for (String str : symbol2RulesMap.keySet())
		{
			if (symbol2RulesMap.get(str).get(0).getProb() * priorProbMap.get(str) < bestPro * pruneThreshold)
				deleteList.add(str);
		}

		for (String str : deleteList)
			symbol2RulesMap.remove(str);
	}

	/**
	 * 初始化线图
	 * 
	 * @param words
	 * @param poses
	 * @param numOfResulets
	 */
	private void initializeChart(String[] words, String[] poses, int numOfResulets)
	{
		int n = words.length;
		chart = new ChartEntry[n + 1][n + 1];
		for (int i = 0; i < n; i++)
		{
			for (int j = 1; j <= n; j++)
			{
				if (j >= i + 1)
				{// 只有矩阵的上三角存储数据
					chart[i][j] = new ChartEntry(new HashMap<String, ArrayList<CKYPRule>>(), false);
				}

				if (j == i + 1)
				{
					chart[i][j].setFlag(true);

					HashMap<String, ArrayList<CKYPRule>> ruleMap = chart[j - 1][j].getPruleMap();

					if (poses == null)
					{
						ArrayList<String> rhs = new ArrayList<String>();
						rhs.add(words[j - 1]);

						for (RewriteRule rule0 : pcnf.getRuleByRHS(rhs))
						{
							PRule rule = (PRule) rule0;
							ArrayList<CKYPRule> ckyPRulList = new ArrayList<CKYPRule>();
							// 此处延迟概率初始化至updateRuleMapOfTable
							ckyPRulList.add(new CKYPRule(1.0, rule.getLHS(), rule.getRHS(), 0, 0, 0));

							HashMap<String, Double> lhsAndProMap = new HashMap<String, Double>();

							addUnitInduction(rule, ruleMap, rule.getLHS(), ckyPRulList, numOfResulets,
									lhsAndProMap);
						}
					}
					else
					{
						// 根据分词和词性标注的结果进行table表对角线的j初始化
						ArrayList<CKYPRule> ckyPRulList = new ArrayList<CKYPRule>();
						ckyPRulList.add(new CKYPRule(1.0, poses[j - 1], words[j - 1], 0, 0, 0));

						PRule rule = new PRule(1.0, poses[j - 1], words[j - 1]);
						HashMap<String, Double> lhsAndProMap = new HashMap<String, Double>();

						addUnitInduction(rule, ruleMap, rule.getLHS(), ckyPRulList, numOfResulets, lhsAndProMap);
					}
				}
			}
		}
	}

	/**
	 * @param i
	 *            table表横坐标点
	 * @param k
	 *            分裂的值
	 * @param j
	 *            table表纵坐标点
	 * @param n
	 *            words的长度
	 * @param numOfResulets
	 *            所需的最佳结果数
	 */
	private void updateTable(int i, int k, int j, int n, int numOfResulets)
	{
		HashMap<String, ArrayList<CKYPRule>> ikRuleMap = chart[i][k].getPruleMap();
		HashMap<String, ArrayList<CKYPRule>> kjRuleMap = chart[k][j].getPruleMap();

		if (ikRuleMap.size() != 0 && kjRuleMap.size() != 0)
		{// 如果在ik点和kj点的映射表不为空
			Iterator<String> itrIk = ikRuleMap.keySet().iterator();
			while (itrIk.hasNext())
			{
				String ikStr = itrIk.next();
				Iterator<String> itrKj = kjRuleMap.keySet().iterator();
				while (itrKj.hasNext())
				{
					String kjStr = itrKj.next();
					Set<RewriteRule> ruleSet = pcnf.getRuleByRHS(ikStr, kjStr);
					if (ruleSet == null)
					{// 若ruleSet为空，则遍历下一个kjStr
						continue;
					}

					Iterator<RewriteRule> itr = ruleSet.iterator();
					while (itr.hasNext())
					{
						PRule prule = (PRule) itr.next();
						ArrayList<CKYPRule> ckyPRuleList = getKCKYPRuleFromTable(k, numOfResulets, ikRuleMap.get(ikStr),
								kjRuleMap.get(kjStr), prule);
						
						HashMap<String, Double> lhsAndProMap = new HashMap<String, Double>();
						HashMap<String, ArrayList<CKYPRule>> ruleMap = chart[i][j].getPruleMap();
						addUnitInduction(prule, ruleMap, prule.getLHS(), ckyPRuleList, numOfResulets, lhsAndProMap);
					}
				}
			}
		}
	}

	/**
	 * 更新table表中ruleMap,其中每个终结符对应的ArrayList中个最多有numOfResulets个 遇到单位产品时， 比如S->A->B
	 * D新建规则，pro=P(S->A)*P(A->B D),LHS=S+ "@" +A ，RHS=B D。并在Map中以S作为key值
	 * 
	 * @param rule
	 * @param ruleMap
	 * @param symbol
	 * @param ckyPRuleList
	 * @param numOfResulets
	 * @param lhsAndProMap
	 */
	private void addUnitInduction(PRule rule, HashMap<String, ArrayList<CKYPRule>> ruleMap, String symbol,
			ArrayList<CKYPRule> ckyPRuleList, int numOfResulets, HashMap<String, Double> lhsAndProMap)
	{
		// 该非终结符对应的映射表不存在，直接添加
		if (!ruleMap.keySet().contains(symbol))
		{
			ArrayList<CKYPRule> tempList = addCKYPRule(ckyPRuleList, rule);
			ruleMap.put(symbol, tempList);
		} // 若该非终结符对应的映射表已满，而且其中概率最小的比ckyPRuleList中最大的还要大则不处理
		else if (ruleMap.get(symbol).size() == numOfResulets
				&& ruleMap.get(symbol).get(numOfResulets - 1).getProb() >= ckyPRuleList.get(0).getProb() * rule.getProb())
		{

		} // 将ckyPRuleList和ruleMap中该非终结符对应的规则表联合再排序
		else
		{
			ArrayList<CKYPRule> tempList = addCKYPRule(ckyPRuleList, rule);
			tempList.addAll(ruleMap.get(symbol));
			Collections.sort(tempList);
			/*
			 * 若结果集中多余k个，则截取其中的前k个
			 */
			if (tempList.size() > numOfResulets)
			{
				ArrayList<CKYPRule> subList = new ArrayList<CKYPRule>();
				for (int i = 0; i < numOfResulets; i++)
					subList.add(tempList.get(i));

				ruleMap.put(symbol, subList);
			}
			else
				ruleMap.put(symbol, tempList);
		}

		int size = ruleMap.get(symbol).size();
		lhsAndProMap.put(symbol, ruleMap.get(symbol).get(size - 1).getProb());
		
		Set<RewriteRule> ruleSet = pcnf.getRuleByRHS(symbol);
		if (ruleSet != null)
		{
			for (RewriteRule rule0 : ruleSet)
			{
				PRule prule = (PRule) rule0;
				double pro1 = prule.getProb() * rule.getProb() * ckyPRuleList.get(0).getProb(); 
				if (lhsAndProMap.containsKey(prule.getLHS()))
				{
					if (lhsAndProMap.get(prule.getLHS()) >= pro1)
						continue;
				}

				PRule prule1 = new PRule(prule.getProb() * rule.getProb(), prule.getLHS() + "@" + rule.getLHS(),
						rule.getRHS());

				addUnitInduction(prule1, ruleMap, prule.getLHS(), ckyPRuleList, numOfResulets, lhsAndProMap);
			}
		}
	}

	/**
	 * 由规则右侧反推规则左侧，由右侧的非终结符得到对应的列表ikCKYPRuleList，kjCKYPRuleList，然后不同的列表中的规则两两结合，取前numOfResulets个并返回
	 * 
	 * @param k
	 *            分裂位置
	 * @param numOfResultRule
	 *            所需结果数
	 * @param ikCKYPRuleList
	 *            ik点的非终结符ikStr对应的规则列表
	 * @param kjCKYPRuleList
	 *            kj点的非终结符kjStr对应的规则列表
	 * @param prule
	 *            ij点的规则
	 * @return
	 */
	private ArrayList<CKYPRule> getKCKYPRuleFromTable(int k, int numOfResulets, ArrayList<CKYPRule> ikCKYPRuleList,
			ArrayList<CKYPRule> kjCKYPRuleList, PRule prule)
	{
		String lhs = prule.getLHS();
		ArrayList<CKYPRule> tempList = new ArrayList<CKYPRule>();
		for (int i = 0; i < ikCKYPRuleList.size(); i++)
		{
			CKYPRule ikCKYPRule = ikCKYPRuleList.get(i);
			for (int j = 0; j < kjCKYPRuleList.size(); j++)
			{
				CKYPRule kjCKYPRule = kjCKYPRuleList.get(j);
				tempList.add(new CKYPRule(ikCKYPRule.getProb() * kjCKYPRule.getProb(), lhs, prule.getRHS(), k, i, j));
			}
		}

		Collections.sort(tempList);

		/*
		 * 若结果集中多余k个，则截取其中的前k个
		 */
		if (tempList.size() > numOfResulets)
		{
			ArrayList<CKYPRule> subList = new ArrayList<CKYPRule>();
			for (int i = 0; i < numOfResulets; i++)
				subList.add(tempList.get(i));

			return subList;
		}

		return tempList;

	}

	private ArrayList<CKYPRule> addCKYPRule(ArrayList<CKYPRule> ckyPRuleList, PRule rule)
	{
		ArrayList<CKYPRule> tempList = new ArrayList<CKYPRule>();
		for (int i = 0; i < ckyPRuleList.size(); i++)
		{
			CKYPRule ckyprule = ckyPRuleList.get(i);
			tempList.add(new CKYPRule(rule.getProb() * ckyprule.getProb(), rule.getLHS(), ckyprule.getRHS(),
					ckyprule.getK(), ckyprule.getI(), ckyprule.getJ()));
		}

		return tempList;
	}

	/**
	 * 生成括号表达式
	 * 
	 * @param n
	 * @param numOfResulets
	 * @param table
	 * @param pcnf
	 * @return
	 */
	private ArrayList<String> getBracketList(int n, int numOfResulets)
	{
		// 查找概率最大的n个结果
		ArrayList<CKYPRule> resultRuleList = chart[0][n].getPruleMap().get(pcnf.getStartSymbol());
		ArrayList<String> resultList = new ArrayList<String>();

		if (resultRuleList == null)
			return resultList;

		for (CKYPRule prule : resultRuleList)
		{
			StringBuilder strBuilder = new StringBuilder();

			// 从最后一个节点[0,n]开始回溯
			createBracket(0, n, prule, strBuilder);

			resultList.add(strBuilder.toString());
		}

		return resultList;
	}

	/**
	 * 先根遍历
	 * 
	 * @param i
	 * @param j
	 * @param prule
	 * @param strBuilder
	 * @param table
	 */
	private void createBracket(int i, int j, CKYPRule prule, StringBuilder strBuilder)
	{
		strBuilder.append("(");

		strBuilder.append(prule.getLHS());
		if (i == j - 1)
		{// 对角线存储词性规则
			strBuilder.append(" ");
			strBuilder.append(prule.getRHS().get(0));
			strBuilder.append(")");
			return;
		}

		// 第一个孩子
		CKYPRule lPrule = chart[i][prule.getK()].getPruleMap().get(prule.getRHS().get(0)).get(prule.getI());
		createBracket(i, prule.getK(), lPrule, strBuilder);

		// 第二个孩子
		CKYPRule rPrule = chart[prule.getK()][j].getPruleMap().get(prule.getRHS().get(1)).get(prule.getJ());
		createBracket(prule.getK(), j, rPrule, strBuilder);

		strBuilder.append(")");
	}

	/**
	 * 内部类,table存储类,记录在table[i][j]点中的映射规则表，以及用于判断是否为对角线上点的flag
	 */
	class ChartEntry
	{
		// 子树根节点非终结符为键
		private HashMap<String, ArrayList<CKYPRule>> pruleMap;

		// flag用来判断是否为对角线上的点
		private boolean flag;

		public ChartEntry(HashMap<String, ArrayList<CKYPRule>> pruleMap, boolean flag)
		{
			this.pruleMap = pruleMap;
			this.flag = flag;
		}

		public HashMap<String, ArrayList<CKYPRule>> getPruleMap()
		{
			return pruleMap;
		}

		public void setPruleMap(HashMap<String, ArrayList<CKYPRule>> pruleMap)
		{
			this.pruleMap = pruleMap;
		}

		public boolean isFlag()
		{
			return flag;
		}

		public void setFlag(boolean flag)
		{
			this.flag = flag;
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		DataInput in = new DataInputStream(new FileInputStream((args[0])));
		PCFG loosePCNF = new PCFG();
		loosePCNF.read(in);

		double pruneThreshold = 0.0001;// Double.parseDouble(args[2]);
		boolean secondPrune = false;// Boolean.getBoolean(args[3]);
		boolean prior = false;// Boolean.getBoolean(args[4]);

		ConstituentParserCKYLoosePCNF parser = new ConstituentParserCKYLoosePCNF(loosePCNF, pruneThreshold, secondPrune,
				prior);

		Scanner input = new Scanner(System.in);
		String text = "";
		while (true)
		{
			System.out.println("请输入待分析的文本：");
			text = input.nextLine();

			if (text.equals(""))
			{
				System.out.println("内容为空，请重新输入！");
			}
			else if (text.equals("exit"))
			{
				break;
			}
			else
			{
				String[] s = text.split("\\s+");
				ConstituentTree tree = parser.parse(s);
				if (tree != null)
					System.out.println(tree.toPrettyString());
				else
					System.out.println("Can't parse.");
			}
		}

		input.close();
	}
}
