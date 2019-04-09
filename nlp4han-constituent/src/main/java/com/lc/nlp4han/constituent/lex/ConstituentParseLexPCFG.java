package com.lc.nlp4han.constituent.lex;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.ConstituentParser;
import com.lc.nlp4han.constituent.ConstituentTree;
import com.lc.nlp4han.constituent.TreeNode;

/**
 * 词汇化PCFG句法解析器
 * 
 * 依据Collins1999论文实现
 * 
 *
 */
public class ConstituentParseLexPCFG implements ConstituentParser
{
	private boolean coorAndPc = false;// 是否处理并列结构及标点符号
	private ChartEntry[][] chart = null;
	private LexPCFG lexpcfg = null;
	private double pruneThreshold;
	private boolean secondPrune;
	private boolean prior;

	private double smoothPro = 0.0000001;// 当概率为零时的平滑值

	public ConstituentParseLexPCFG(LexPCFG lexpcfg, double pruneThreshold, boolean secondPrune, boolean prior)
	{
		this.lexpcfg = lexpcfg;
		this.pruneThreshold = Math.log(1.0 / pruneThreshold);
		this.secondPrune = secondPrune;
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
		return getParseResult(words, poses, 1)[0];
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
		return getParseResult(words, null, 1)[0];
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
	 * 获取成分树的通用方法
	 * 
	 * @param words
	 * @param poses
	 * @param k
	 * @return
	 */
	private ConstituentTree[] getParseResult(String[] words, String[] poses, int k)
	{
		ArrayList<String> bracketList = parseLex(words, poses, k, true);

		if (bracketList == null && secondPrune && words.length <= 40)
			bracketList = parseLex(words, poses, k, false);

		int i = 0;
		ConstituentTree[] treeArray = new ConstituentTree[k];
		for (String bracketString : bracketList)
		{
			TreeNode rootNode = BracketExpUtil.generateTree(bracketString);
			treeArray[i++] = new ConstituentTree(rootNode);
		}

		return treeArray;
	}

	/**
	 * 剖析的具体过程
	 * 
	 * @param words
	 * @param poses
	 * @param k
	 * @return
	 */
	private ArrayList<String> parseLex(String[] words, String[] poses, int k, boolean prune)
	{
		// 初始化
		initializeChart(words, poses);

		int n = words.length;
		// 填充chart图中的边
		for (int span = 2; span <= words.length; span++)
		{
			for (int i = 0; i <= n - span; i++)
			{
				int j = i + span;
				fillEdgeOfChart(i, j);

				// 剪枝
				if (prune)
					pruneEdge(i, j, words.length);
			}
		}

		return Chart2Results.getBrackets(chart, words.length, k);
	}

	/**
	 * 全局剪枝，但是因为因为需要词到词性标注的概率，也就是在输入只包含分词的情况下才能进行全局剪枝
	 * 
	 * @param n
	 * @param span
	 */
	@SuppressWarnings("unused")
	private void globalPruneEdge(int n, int span)
	{
		LexPCFGPrior pcp = (LexPCFGPrior) lexpcfg;
		HashMap<String, Double> map1 = pcp.getPriorMap();

		ArrayList<Edge> deleteList = new ArrayList<Edge>();

		double[] f = new double[n + 1];
		double[] b = new double[n + 1];

		double[] f1 = new double[n + 1];
		double[] b1 = new double[n + 1];

		f[0] = 1.0;
		f[1] = 1.0;
		f1[0] = 1.0;
		f1[1] = 1.0;
		for (int start = 0; start <= n - 2; start++)
		{
			for (int j = start + 2; j - start <= span && j <= n; j++)
			{
				HashMap<Edge, Double> map = chart[start][j].getEdgeMap();
				if (map == null)
					break;

				for (Edge edge : map.keySet())
				{
					double left, score;
					if (edge.isStop())
					{
						left = f[start];
						score = left * map.get(edge) * map1.get(edge.getLabel());
						if (score > f[j])
							f[j] = score;
					}
					else
					{
						left = f1[start];
						score = left * map.get(edge) * map1.get(edge.getLabel());
						if (score > f1[j])
							f1[j] = score;
					}

				}
			}
		}

		b[n] = 1;
		b[n - 1] = 1.0;
		b1[n] = 1;
		b1[n - 1] = 1.0;
		for (int start = n - 2; start > 0; start--)
		{
			for (int j = start + 2; j - start <= span && j <= n; j++)
			{
				HashMap<Edge, Double> map = chart[start][j].getEdgeMap();
				if (map == null)
					break;

				for (Edge edge : map.keySet())
				{
					double right, score;
					if (edge.isStop())
					{
						right = b[j];
						score = right * map.get(edge) * map1.get(edge.getLabel());
						if (score > b[start])
							b1[start] = score;
					}
					else
					{
						right = b1[j];
						score = right * map.get(edge) * map1.get(edge.getLabel());
						if (score > b1[j])
							b1[j] = score;
					}
				}
			}
		}

		double bestPro;
		for (int i = 0; i <= n - span; i++)
		{
			for (int j = i + 2; j - i <= span; j++)
			{
				for (Edge edge : chart[i][j].getEdgeMap().keySet())
				{
					double left, right;
					if (edge.isStop())
					{
						left = f[i];
						right = b[j];
						bestPro = f[n];
					}
					else
					{
						left = f1[i];
						right = b1[j];
						bestPro = f1[n];
					}

					double total = left * edge.getProb() * map1.get(edge.getLabel()) * right;
					if (total < bestPro * pruneThreshold)
						deleteList.add(edge);
				}
			}
		}

		for (Edge edge : deleteList)
		{
			int i = edge.getStart();
			int j = edge.getEnd();
			chart[i][j].getEdgeMap().remove(edge);
		}
	}

	/**
	 * 剪枝
	 * 
	 * @param i
	 * @param j
	 * @param n
	 */
	private void pruneEdge(int i, int j, int n)
	{
		double bestPro1 = -1000;
		double bestPro2 = -1000;
		// 动态得到剪枝比例
		double pruneThreshold1 = pruneThreshold;
		// double pruneThreshold1 = getPruneThreshold(n);

		ArrayList<Edge> deleteList = new ArrayList<Edge>();
		HashMap<Edge, Double> map = chart[i][j].getEdgeMap();
		for (Edge edge : map.keySet())
		{
			double pro = 0.0;
			if (prior)
			{
				LexPCFGPrior lpp = (LexPCFGPrior) lexpcfg;
				HashMap<String, Double> map1 = lpp.getPriorMap();
				pro = Math.log(map1.get(edge.getLabel()));
			}

			if (edge.isStop() && map.get(edge) + pro > bestPro1)
				bestPro1 = map.get(edge) + pro;
			else if (!edge.isStop() && map.get(edge) + pro > bestPro2)
				bestPro2 = map.get(edge) + pro;
		}

		for (Edge edge : map.keySet())
		{
			double pro = 0.0;
			if (prior)
			{
				LexPCFGPrior lpp = (LexPCFGPrior) lexpcfg;
				HashMap<String, Double> map1 = lpp.getPriorMap();
				pro = Math.log(map1.get(edge.getLabel()));
			}

			if (edge.isStop() && map.get(edge) + pro < bestPro1 - pruneThreshold1)
				deleteList.add(edge);
			else if (!edge.isStop() && map.get(edge) + pro < bestPro2 - pruneThreshold1)
				deleteList.add(edge);
		}
		
		for (Edge edge : deleteList)
			map.remove(edge);
	}

	/**
	 * 初始化chart，并将对应的内容填充对角线节点中
	 * 
	 * @param words
	 * @param poses
	 */
	private void initializeChart(String[] words, String[] poses)
	{
		int n = words.length;
		chart = new ChartEntry[n + 1][n + 1];
		for (int i = 0; i < n; i++)
		{
			for (int j = 1; j <= n; j++)
			{
				if (j >= i + 1)
					chart[i][j] = new ChartEntry(false, new HashMap<Edge, Double>());

				if (j == i + 1)
				{
					Distance lc = new Distance(true, false);
					Distance rc = new Distance(true, false);
					Edge edge = new Edge(poses[i], null, words[i], poses[i], i, i + 1, lc, rc, true, 0.0, null);

					chart[i][j].setFlag(true);
					chart[i][j].getEdgeMap().put(edge, 0.0);
					addSinglesAndStops(i, j);
				}
			}
		}
	}

	/**
	 * 添加单元规则和Stop符号
	 * 
	 * @param i
	 * @param j
	 */
	private void addSinglesAndStops(int i, int j)
	{
		// 为避免迭代器的冲突，添加一个临时的边的列表
		ArrayList<Edge> tempEdgeList = new ArrayList<Edge>();
		HashMap<Edge, Double> map = chart[i][j].getEdgeMap();
		for (Edge edge : map.keySet())
		{
			if (!edge.isStop())
				addStop(edge, tempEdgeList);
		}
		
		// 将成功添加stop的边添加进映射表中，并清除临时表中的数据
		addNewEdge(map, tempEdgeList);
		
		final int MAXUNARY = 3;

		// 添加单元规则
		for (i = 1; i < MAXUNARY; i++)
		{
			for (Edge edge : map.keySet())
			{
				if (edge.isStop())
					addSingle(edge, tempEdgeList);
			}
			
			addNewEdge(map, tempEdgeList);
		}

	}

	/**
	 * 将新生成的边添加到对应点的映射表中
	 * 
	 * @param map
	 * @param tempEdgeList
	 */
	private void addNewEdge(HashMap<Edge, Double> map, ArrayList<Edge> tempEdgeList)
	{
		for (Edge edge : tempEdgeList)
			addEdge(edge, edge.getStart(), edge.getEnd(), null);

		tempEdgeList.clear();
	}

	/**
	 * 添加单元规则
	 * 
	 * @param edge
	 */
	private void addSingle(Edge edge, ArrayList<Edge> tempEdgeList)
	{
		// 从特殊到一般
		OccurenceHeadChild occur0 = new OccurenceHeadChild(edge.getLabel(), null, edge.getHeadPOS(), edge.getHeadWord());
		OccurenceHeadChild occur1 = new OccurenceHeadChild(edge.getLabel(), null, edge.getHeadPOS(), null);
		OccurenceHeadChild occur2 = new OccurenceHeadChild(edge.getLabel(), null, null, null);
		
		HashSet<String> parentSet = lexpcfg.getParentSet(occur0);
		if (parentSet == null)
		{// 若没有可以向上延伸的规则，进行回退试探
			parentSet = lexpcfg.getParentSet(occur1);
			if (parentSet == null)
				parentSet = lexpcfg.getParentSet(occur2);
			
			if (parentSet == null)
				return;
		}

		for (String parentLabel : parentSet)
		{
			ArrayList<Edge> children = new ArrayList<Edge>();
			children.add(edge);
			
			Distance lc = new Distance(true, false);
			Distance rc = new Distance(true, false);
			
			int start = edge.getStart();
			int end = edge.getEnd();
			
			occur0.setParentLabel(parentLabel);
			double edgePro = lexpcfg.getProbForGenerateHead(occur0);
			if (edgePro == 0.0)
				edgePro = smoothPro;

			double pro = Math.log(edgePro) + edge.getProb();

			Edge e1 = new Edge(parentLabel, edge.getLabel(), edge.getHeadWord(), edge.getHeadPOS(), start, end, lc, rc, false,
					pro, children);

			addEdge(e1, start, end, tempEdgeList);
			addStop(e1, tempEdgeList);
		}
	}

	/**
	 * 添加新的边
	 * 
	 * @param edge
	 * @param start
	 * @param end
	 */
	private void addEdge(Edge edge, int start, int end, ArrayList<Edge> tempEdgeList)
	{
		if (chart[start][end].getEdgeMap().containsKey(edge)
				&& chart[start][end].getEdgeMap().get(edge) > edge.getProb())
		{
			return;
		}
		else
		{
			if (tempEdgeList != null)
			{
				tempEdgeList.add(edge);
			}
			else
			{
				chart[start][end].getEdgeMap().remove(edge);
				chart[start][end].getEdgeMap().put(edge, edge.getProb());
			}
		}
	}

	/**
	 * 在规则两侧添加Stop符号
	 * 
	 * @param edge
	 */
	private void addStop(Edge edge, ArrayList<Edge> tempEdgeList)
	{
		// 若此边包含没有合并的并列结构或者顿号，则不添加stop
		if (edge.isCoor() || edge.isPu())
			return;

		// 分别初始化两侧的stop规则
		OccurenceStop rsg1 = new OccurenceStop(edge.getHeadLabel(), edge.getLabel(), edge.getHeadPOS(),
				edge.getHeadWord(), 1, true, edge.getLc());
		OccurenceStop rsg2 = new OccurenceStop(edge.getHeadLabel(), edge.getLabel(), edge.getHeadPOS(),
				edge.getHeadWord(), 2, true, edge.getRc());

		if (edge.getLabel().equals("NPB"))
		{
			Edge edge1 = edge.getFirstChild();
			Edge edge2 = edge.getLastChild();
			rsg1 = new OccurenceStop(edge1.getLabel(), edge.getLabel(), edge1.getHeadPOS(), edge1.getHeadWord(), 1,
					true, new Distance());
			rsg2 = new OccurenceStop(edge2.getLabel(), edge.getLabel(), edge2.getHeadPOS(), edge.getHeadWord(), 2, true,
					new Distance());
		}
		
		// 将原始概率与两侧规则的概率相乘得到新的概率
		double lstop = lexpcfg.getProbForGenerateStop(rsg1);
		double rstop = lexpcfg.getProbForGenerateStop(rsg2);

		// 如果概率为零则不添加
		if (lstop == 0)
			lstop = smoothPro;
		if (rstop == 0)
			rstop = smoothPro;
		/*
		 * if(lstop==0.0||rstop==0.0) { return; }
		 */
		double pro = edge.getProb() + Math.log(lstop) + Math.log(rstop);

		Edge e1 = new Edge(edge.getLabel(), edge.getHeadLabel(), edge.getHeadWord(), edge.getHeadPOS(), edge.getStart(),
				edge.getEnd(), edge.getLc(), edge.getRc(), true, pro, edge.getChildren());
		addEdge(e1, e1.getStart(), e1.getEnd(), tempEdgeList);
	}

	/**
	 * 填充Chart中的边
	 * 
	 * @param i
	 *            横坐标值
	 * @param j
	 *            纵坐标值
	 */
	private void fillEdgeOfChart(int i, int j)
	{
		for (int split = i + 1; split < j; split++)
		{
			HashMap<Edge, Double> map1 = chart[i][split].getEdgeMap();
			HashMap<Edge, Double> map2 = chart[split][j].getEdgeMap();
			if (map1.size() != 0 && map2.size() != 0)
			{
				ArrayList<Edge> tempEdgeList = new ArrayList<Edge>();
				for (Edge edge1 : map1.keySet())
				{
					for (Edge edge2 : map2.keySet())
					{
						if (!edge1.isStop() && edge2.isStop())
							mergeEdge(edge1, edge2, 2, tempEdgeList);
						else if (edge1.isStop() && !edge2.isStop())
							mergeEdge(edge1, edge2, 1, tempEdgeList);
					}
				}
				
				for (Edge edge : tempEdgeList)
					addEdge(edge, i, j, null);
			}
		}
		
		addSinglesAndStops(i, j);
	}

	/**
	 * 合并规则的两侧和中心child
	 * 
	 * @param e1
	 * @param e2
	 * @param direction
	 *            1为左侧，2为右侧
	 */
	private void mergeEdge(Edge e1, Edge e2, int direction, ArrayList<Edge> tempEdgeList)
	{
		Edge edge;
		Distance lc, rc;
		OccurenceSides rsg;
		ArrayList<Edge> children = new ArrayList<Edge>();

		// 动词集合
		HashSet<String> verbs = getVerbs();

		double pro = e1.getProb() + e2.getProb();

		// 若添加的是head右侧的孩子
		if (direction == 2)
		{
			// 为新的边添加孩子
			children.addAll(e1.getChildren());
			children.add(e2);

			lc = e1.getLc();// 此刻的距离
			// 是否包含动词
			boolean rcVerb = (e1.getRc().isCrossVerb() || e2.getLc().isCrossVerb() || e2.getRc().isCrossVerb()
					|| verbs.contains(e2.getHeadPOS()));
			rc = new Distance(false, rcVerb);

			// 获得概率
			if (e1.getLabel().equals("NPB"))
			{
				rsg = disposeNPB(e1, e2, 2);
			}
			else
			{
				rsg = new OccurenceSides(e1.getHeadLabel(), e1.getLabel(), e1.getHeadPOS(), e1.getHeadWord(), direction,
						e2.getLabel(), e2.getHeadPOS(), e2.getHeadWord(), false, false, e1.getRc());
			}

			double sidesPro = lexpcfg.getProbForGenerateSides(rsg);
			if (sidesPro == 0.0)
				return;

			pro = pro + Math.log(sidesPro);

			// 并列结构处理
			if (coorAndPc)
				pro = disposeCoorAndPC(e1, e2, direction, rsg, pro);

			edge = new Edge(e1.getLabel(), e1.getHeadLabel(), e1.getHeadWord(), e1.getHeadPOS(), e1.getStart(),
					e2.getEnd(), lc, rc, false, pro, children);

			// 并列结构处理
			if (coorAndPc)
				edge = disposeCoorAndPCEdge(e1, e2, edge, children);
		}
		else
		{
			// 为新的边添加孩子
			children.addAll(e2.getChildren());
			children.add(e1);
			rc = e2.getRc();
			boolean lcVerb = (e1.getLc().isCrossVerb() || e1.getRc().isCrossVerb() || e2.getLc().isCrossVerb()
					|| verbs.contains(e1.getHeadPOS()));
			lc = new Distance(false, lcVerb);

			if (e2.getLabel().equals("NPB"))
			{
				rsg = disposeNPB(e1, e2, 1);
			}
			else
			{
				rsg = new OccurenceSides(e2.getHeadLabel(), e2.getLabel(), e2.getHeadPOS(), e2.getHeadWord(), direction,
						e1.getLabel(), e1.getHeadPOS(), e1.getHeadWord(), false, false, e2.getLc());
			}
			
			double sidesPro = lexpcfg.getProbForGenerateSides(rsg);
			if (sidesPro == 0.0)
				return;

			pro = pro + Math.log(sidesPro);

			edge = new Edge(e2.getLabel(), e2.getHeadLabel(), e2.getHeadWord(), e2.getHeadPOS(), e1.getStart(),
					e2.getEnd(), lc, rc, false, pro, children);
		}
		
		addEdge(edge, edge.getStart(), edge.getEnd(), tempEdgeList);
	}

	/**
	 * 得到动词列表
	 * 
	 * @return
	 */
	private HashSet<String> getVerbs()
	{
		String[] verbArray = { "VA", "VC", "VE", "VV", "BA", "LB" };
		HashSet<String> verbs = new HashSet<String>();
		for (String verb : verbArray)
			verbs.add(verb);

		return verbs;
	}

	/**
	 * 合并时，若父节点为NPB,则其概率计算方式不变，但是生成规则中的headChild变为将要添加的孩子的前一个孩子
	 * 
	 * @param e1
	 * @param e2
	 * @param direction
	 * @param rsg
	 * @return
	 */
	private OccurenceSides disposeNPB(Edge e1, Edge e2, int direction)
	{
		OccurenceSides rsg;
		if (direction == 2)
		{
			Edge lastChild = e1.getLastChild();
			rsg = new OccurenceSides(lastChild.getLabel(), e1.getLabel(), lastChild.getHeadPOS(),
					lastChild.getHeadWord(), direction, e2.getLabel(), e2.getHeadPOS(), e2.getHeadWord(), false, false,
					new Distance());
		}
		else
		{
			Edge firstChild = e2.getFirstChild();
			rsg = new OccurenceSides(firstChild.getLabel(), e2.getLabel(), firstChild.getHeadPOS(),
					firstChild.getHeadWord(), direction, e1.getLabel(), e1.getHeadPOS(), e1.getHeadWord(), false, false,
					new Distance());
		}
		return rsg;
	}

	/**
	 * 处理并列结构及标点符号（暂时只处理并列结构）
	 * 
	 * @param e1
	 * @param e2
	 * @param direction
	 * @param rsg
	 * @param pro
	 * @return
	 */
	private double disposeCoorAndPC(Edge e1, Edge e2, int direction, OccurenceSides rsg, double pro)
	{
		if (e1.getChildNum() >= 2 && !e1.getLabel().equals("NPB") && e1.getLastChild().getLabel().equals("CC")
				&& e1.getLabel().equals(e1.getChildLabel(e1.getChildNum() - 2)) && e1.getLabel().equals(e2.getLabel()))
		{
			Edge lastChild = e1.getLastChild();
			rsg = new OccurenceSides(lastChild.getLabel(), e1.getLabel(), lastChild.getHeadPOS(),
					lastChild.getHeadWord(), direction, e2.getLabel(), e2.getHeadPOS(), e2.getHeadWord(), true, false,
					e1.getRc());

			OccurenceSpecialCase sg = new OccurenceSpecialCase(e1.getLabel(), lastChild.getHeadPOS(),
					lastChild.getHeadWord(), e1.getChildLabel(e1.getChildNum() - 2), e2.getLabel(),
					e1.getChild(e1.getChildNum() - 2).getHeadWord(), e2.getHeadWord(),
					e1.getChild(e1.getChildNum() - 2).getHeadPOS(), e2.getHeadPOS());

			pro = pro * lexpcfg.getProbForGenerateSides(rsg) * lexpcfg.getProbForSpecialCase(sg);
		}
		return pro;
	}

	/**
	 * 若此刻添加的是CC或者标点符号、，则概率不变，只是将edge中的coor值改为1
	 * 
	 * @param e1
	 * @param e2
	 * @param edge
	 * @param children
	 * @return
	 */
	private Edge disposeCoorAndPCEdge(Edge e1, Edge e2, Edge edge, ArrayList<Edge> children)
	{
		if (e2.getLabel().equals("CC") && !e1.getLabel().equals("NPB")
				&& e1.getLabel().equals(e1.getLastChild().getLabel()))
		{
			edge = new Edge(e1.getLabel(), e1.getHeadLabel(), e1.getHeadWord(), e1.getHeadPOS(), e1.getStart(),
					e2.getEnd(), e1.getLc(), new Distance(false, e1.getRc().isCrossVerb()), true, false, false,
					e1.getProb(), children);
		}
		return edge;
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		DataInput in = new DataInputStream(new FileInputStream((args[0])));
		LexPCFG lexPCFG = new LexPCFG();
		lexPCFG.read(in);

		double pruneThreshold = 0.0001;// Double.parseDouble(args[2]);
		boolean secondPrune = false;// Boolean.getBoolean(args[3]);
		boolean prior = false;// Boolean.getBoolean(args[4]);

		ConstituentParseLexPCFG parser = new ConstituentParseLexPCFG(lexPCFG, pruneThreshold, secondPrune, prior);

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