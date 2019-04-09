package com.lc.nlp4han.constituent.pcfg;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.ConstituentParser;
import com.lc.nlp4han.constituent.ConstituentTree;
import com.lc.nlp4han.constituent.TreeNode;

public class ConstituentParserCKYPCNF implements ConstituentParser
{
	private CKYCell[][] table;// 存储在该点的映射表
	private PCFG pcnf;
	private double pruneThreshold;// 剪枝阈值
	private boolean secondPrune;// 是否进行二次解析
	private boolean prior;// 进行剪枝时是否添加先验概率

	public ConstituentParserCKYPCNF(PCFG pcnf, double pruneThreshold, boolean secondPrune, boolean prior) throws UncompatibleGrammar
	{
		if(!pcnf.IsCNF())
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
	public ConstituentTree parse(String[] words, String[] poses)
	{
		return parse(words, poses, 1)[0];
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
		return parse(words, null, 1)[0];
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
		return parse(words, null, k);
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
	public ConstituentTree[] parse(String[] words, String[] poses, int k)
	{		
		ArrayList<String> bracketList = parseCKY(words, poses, k, true);
		
//		if (secondPrune && bracketList.size() == 0 && words.length <= 70)
//		{
//			bracketList = parseCKY(words, poses, k, false);
//		}
		
		int i = 0;
		ConstituentTree[] treeArray = new ConstituentTree[k];
		for (String bracketString : bracketList)
		{
			//System.out.println(bracketString.toString());
			TreeNode rootNode = TreeRestorer.restoreTree(BracketExpUtil.generateTree(bracketString));
			
			treeArray[i++] = new ConstituentTree(rootNode);
		}
		
		return treeArray;
	}

	/**
	 * CKY算法的具体函数
	 * 
	 * 目前支持最好解析结果，最好K结果暂不支持
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
	private ArrayList<String> parseCKY(String[] words, String[] pos, Integer numOfResulets, boolean prune)
	{
		int n = words.length;

		// 初始化
		initializeChart(words, pos);

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
				{
					prunEdge(i, j);
				}
			}
		}
		
		// 回溯并生成括号表达式列表
		ArrayList<String> resultList = creatBracketStringList(n, numOfResulets);

		return resultList;
	}

	/**
	 * 剪枝
	 * 
	 * @param i
	 * @param j
	 */
	private void prunEdge(int i, int j)
	{
		HashMap<String, CKYPRule> map = table[i][j].getPruleMap();
		ArrayList<String> deleteList = new ArrayList<String>();
		HashMap<String, Double> map2 = new HashMap<String, Double>();

		double bestPro = -1.0;
		for (String str : map.keySet())
		{
			double pro = 1;
			// 添加先验概率
			if (prior)
			{
				//HashMap<String, Double> map1 = pcnf.getPosMap();
				HashSet<String> posSet=new HashSet<String>();
				if (str.contains("@"))
				{
					String strs[] = str.split("@");
					for (String str0 : strs)
					{
						if (!posSet.contains(str0))
						{
							break;
						}
						pro *= pcnf.getPosPro(str0);
					}
				}
				else if (str.contains("&"))
				{
					String strs[] = str.split("&");
					for (String str0 : strs)
					{
						if (!posSet.contains(str0))
						{
							break;
						}
						pro *= pcnf.getPosPro(str0);
					}
				}
			}
			map2.put(str, pro);

			if (map.get(str).getProb() * pro > bestPro)
			{
				bestPro = map.get(str).getProb();
			}
		}
		
		for (String str : map.keySet())
		{
			if (map.get(str).getProb() * map2.get(str) < bestPro * pruneThreshold)
			{
				deleteList.add(str);
			}
		}
		
		for (String str : deleteList)
		{
			map.remove(str);
		}
	}

	/**
	 * 初始化
	 * 
	 * @param words
	 * @param poses
	 */
	private void initializeChart(String[] words, String[] poses)
	{
		int n = words.length;
		table = new CKYCell[n + 1][n + 1];
		for (int i = 0; i < n; i++)
		{
			for (int j = 1; j <= n; j++)
			{
				if (j >= i + 1)
				{// 只有矩阵的上三角存储数据
					table[i][j] = new CKYCell(new HashMap<String, CKYPRule>(), false);
				}
				
				if (j == i + 1)
				{
					table[i][j].setFlag(true);
					HashMap<String, CKYPRule> ruleMap = table[i][j].getPruleMap();
					if (poses == null)
					{// 由分词结果反推得到规则，并进行table表对角线的初始化
						for (RewriteRule rule0 : pcnf.getRuleByRHS(words[i]))
						{
							PRule rule = (PRule) rule0;
							String lhs = rule.getLHS().split("@")[0];
							CKYPRule ckyrule = new CKYPRule(rule.getProb(), rule.getLHS(), rule.getRHS(), 0, 0, 0);
							ruleMap.put(lhs, ckyrule);
							updateCellRules(rule.getProb(), ruleMap, rule.getLHS(), words[j - 1], null, 0);
						}
					}
					else
					{// 根据分词和词性标注的结果进行table表对角线的j初始化
						CKYPRule ckyrule = new CKYPRule(1.0, poses[i], words[i], 0, 0, 0);
						ruleMap.put(poses[i], ckyrule);
						updateCellRules(1.0, ruleMap, poses[i], words[i], null, 0);
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
	 * @return count 进行更新操作的次数
	 */
	private void updateTable(int i, int k, int j, int n, int numOfResulets)
	{
		HashMap<String, CKYPRule> ikRuleMap = table[i][k].getPruleMap();
		HashMap<String, CKYPRule> kjRuleMap = table[k][j].getPruleMap();
		
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
					double pro = ikRuleMap.get(ikStr).getProb() * kjRuleMap.get(kjStr).getProb();
					
					updateCellRules(pro, table[i][j].getPruleMap(), null, ikStr, kjStr, k);
				}
			}
		}
	}

	/**
	 * 更新table表中的RuleMap
	 * 
	 * @param pro
	 *            起始概率
	 * @param ruleMap
	 *            table[i][j]的规则映射表
	 * @param lhs0
	 *            规则左侧，用于更新对角线的节点，若为用于更新非对角线节点则为null
	 * @param rhs1
	 *            规则右侧第一个，用于更新对角线的节点（中文词）,用于更新非对角线节点（rhs的第一个）
	 * @param rhs2
	 *            规则右侧第二个，用于更新非对角线节点（rhs的第一个）,若为用于更新对角线节点则为null
	 * @param k
	 *            分裂的位置
	 */
	private void updateCellRules(double pro, HashMap<String, CKYPRule> ruleMap, String lhs0, String rhs1, String rhs2,
			int k)
	{
		Set<RewriteRule> ruleSet = null;
		ArrayList<String> rhs = new ArrayList<String>();
		if (lhs0 == null)
		{
			rhs.add(rhs1);
			rhs.add(rhs2);
			ruleSet = pcnf.getRuleByRHS(rhs);
		}
		else
		{
			rhs.add(rhs1);
			ruleSet = pcnf.getRuleByRHS(lhs0);
		}

		if (ruleSet != null)
		{
			for (RewriteRule rule : ruleSet)
			{
				PRule prule = (PRule) rule;

				String lhsOfckyrule1 = prule.getLHS();
				if (lhs0 != null)
				{
					lhsOfckyrule1 += "@" + lhs0;
				}
				CKYPRule ckyrule1 = new CKYPRule(prule.getProb() * pro, lhsOfckyrule1, rhs, k, 0, 0);
				String lhs = prule.getLHS().split("@")[0];// 取左侧第一个为ruleMap的key值，如NP@NN中的NP

				if (!ruleMap.keySet().contains(lhs))
				{// 该非终结符对应的规则不存在，直接添加
					ruleMap.put(lhs, ckyrule1);
				}
				else if (ruleMap.get(lhs).getProb() < ckyrule1.getProb()) // 只取最好的结果
				{
					ruleMap.put(lhs, ckyrule1);
				}
			}
		}
	}

	/**
	 * 生成结果括号表达式的列表
	 * 
	 * @param n
	 *            句子长度
	 * @param numOfResulets
	 *            需要获得的句子分析结果个数
	 */
	private ArrayList<String> creatBracketStringList(int n, int numOfResulets)
	{
		ArrayList<String> resultList = new ArrayList<String>();

		// 查找概率最大的n个结果
		CKYPRule resultRule = table[0][n].getPruleMap().get(pcnf.getStartSymbol());
		if (resultRule == null)
		{// 如果没有Parse结果则直接返回
			return resultList;
		}

		StringBuilder strBuilder = new StringBuilder();
		createBracket(0, n, resultRule, strBuilder);// 从最后一个节点[0,n]开始回溯

		resultList.add(strBuilder.toString());

		return resultList;
	}
 
	/**
	 * 递归table和back生成二叉树形式的括号表达式
	 * @param i
	 * @param j
	 * @param prule
	 * @param strBuilder
	 */
	private void createBracket(int i, int j, CKYPRule prule, StringBuilder strBuilder) {
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
		CKYPRule lPrule = table[i][prule.getK()].getPruleMap().get(prule.getRHS().get(0));
		createBracket(i, prule.getK(), lPrule, strBuilder);
		
		// 第二个孩子
		CKYPRule rPrule = table[prule.getK()][j].getPruleMap().get(prule.getRHS().get(1));
		createBracket(prule.getK(), j, rPrule, strBuilder);

		strBuilder.append(")");
	}
	
	/**
	 * 内部类,table存储类,记录在table[i][j]点中的映射规则表，以及用于判断是否为对角线上点的flag
	 */
	class CKYCell
	{
		private HashMap<String, CKYPRule> pruleMap;
		// flag用来判断是否为对角线上的点
		private boolean flag;

		public CKYCell(HashMap<String, CKYPRule> pruleMap, boolean flag)
		{
			this.pruleMap = pruleMap;
			this.flag = flag;
		}

		public HashMap<String, CKYPRule> getPruleMap()
		{
			return pruleMap;
		}

		public void setPruleMap(HashMap<String, CKYPRule> pruleMap)
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
		PCFG pcnf = new PCFG();
		pcnf.read(in);
		
		double pruneThreshold = 0.0001;
		boolean secondPrune = false;
		boolean prior = false;

		ConstituentParserCKYPCNF parser = new ConstituentParserCKYPCNF(pcnf, pruneThreshold, secondPrune, prior);

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
				System.out.println(tree.toPrettyString());
			}
		}

		input.close();
	}
}
