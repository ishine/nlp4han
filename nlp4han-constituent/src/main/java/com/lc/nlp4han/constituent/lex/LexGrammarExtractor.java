package com.lc.nlp4han.constituent.lex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.lc.nlp4han.constituent.AbstractHeadGenerator;
import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.HeadGeneratorCollins;
import com.lc.nlp4han.constituent.HeadRuleSetCTB;
import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.constituent.PlainTextByTreeStream;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;

public class LexGrammarExtractor
{
	// 是否收集并列结构和标点符号信息
	boolean processCoorPU = false;

	// 起始符
	private String startSymbol = null;
	// 词性标注集
	private HashSet<String> posSet = new HashSet<String>();
	// 词性标注和词，以及数目
	private HashMap<WordPOS, Integer> wordMap = new HashMap<WordPOS, Integer>();

	// P(H|P,t,w)）相关的统计数据
	private HashMap<OccurenceCollins, RuleAmountsInfo> headGenMap = new HashMap<OccurenceCollins, RuleAmountsInfo>();
	private HashMap<OccurenceCollins, HashSet<String>> parentList = new HashMap<OccurenceCollins, HashSet<String>>();

	// 用于生成SidesChild(包含其中心word和pos)相关统计数据
	private HashMap<OccurenceCollins, RuleAmountsInfo> sidesGenMap = new HashMap<OccurenceCollins, RuleAmountsInfo>();

	// 用于生成Stop的相关统计数据
	private HashMap<OccurenceCollins, RuleAmountsInfo> stopGenMap = new HashMap<OccurenceCollins, RuleAmountsInfo>();

	// 用于生成并列结构连词（如CC或者逗号和冒号,为简略，我将生成修饰符pos和生成修饰符word都放入此规则
	private HashMap<OccurenceCollins, RuleAmountsInfo> specialGenMap = new HashMap<OccurenceCollins, RuleAmountsInfo>();

	public LexPCFG extractGrammar(String fileName, String enCoding) throws IOException
	{
		// 括号表达式树拼接成括号表达式String数组
		PlainTextByTreeStream ptbt = new PlainTextByTreeStream(new FileInputStreamFactory(new File(fileName)),
				enCoding);
		String bracketStr = ptbt.read();
		ArrayList<String> bracketList = new ArrayList<String>();
		while (bracketStr != null)
		{
			bracketList.add(bracketStr);
			
			bracketStr = ptbt.read();
		}
		ptbt.close();
		
		return brackets2Grammar(bracketList);
	}

	/**
	 * 由括号表达式列表直接得到LexPCFG
	 */
	public static LexPCFG getLexPCFG(ArrayList<String> bracketStrList) throws IOException
	{
		LexPCFG grammar = new LexGrammarExtractor().brackets2Grammar(bracketStrList);

		return grammar;
	}
	
	/**
	 * 括号表达式生成文法
	 * 
	 * @param bracketStrList
	 * @return
	 */
	private LexPCFG brackets2Grammar(ArrayList<String> bracketStrList)
	{
		AbstractHeadGenerator headGen = new HeadGeneratorCollins(new HeadRuleSetCTB());

		for (String bracketStr : bracketStrList)
		{
			TreeNode rootNode = BracketExpUtil.generateTree(bracketStr);

			HeadTreeNodeForCollins headNode = TreeToHeadTreeForCollins.treeToHeadTree(rootNode, headGen);

			if (startSymbol == null)
				startSymbol = headNode.getNodeName();

			traverseTree(headNode);
		}

		return new LexPCFGPrior(startSymbol, posSet, wordMap, null, headGenMap, parentList, sidesGenMap, stopGenMap,
				specialGenMap);
	}

	/**
	 * 递归遍历句法树并提取规则
	 * 
	 * @param node
	 */
	private void traverseTree(HeadTreeNodeForCollins node)
	{
		if (node.getChildrenNum() == 1 && node.getChild(0).getChildrenNum() == 0) // 预终结符
		{
			posSet.add(node.getNodeName());

			WordPOS wap = new WordPOS(node.getFirstChildName(), node.getNodeName());
			WordPOS pos = new WordPOS(null, node.getNodeName());
			if (!wordMap.containsKey(pos))
			{
				wordMap.put(pos, 1);
				wordMap.put(wap, 1);
			}
			else
			{
				if (!wordMap.containsKey(wap))
					wordMap.put(wap, 1);
				else
					wordMap.put(wap, wordMap.get(wap) + 1);
				
				wordMap.put(pos, wordMap.get(pos) + 1);
			}
			
			// 处理pos节点后，其孩子节点不用处理
			return;
		}
		else
		{
			extractRule(node);
		}

		for (HeadTreeNode node1 : node.getChildren())
		{
			traverseTree((HeadTreeNodeForCollins) node1);
		}
	}

	/**
	 * 提取规则
	 * 
	 * @param node
	 */
	private void extractRule(HeadTreeNodeForCollins node)
	{
		if (node.getChildrenNum() == 0)
		{// 出现错误标注节点
			return;
		}
		
		// 统计生成中心child的数据
		countHeadGR(node);
		
		// 统计生成中心Child两侧的数据,以及统计两侧不生成Stop的数据
		countSidesGR(node);
		
		// 统计生成中心Stop的数据,此处是指统计两侧为Stop的数据
		countStopGR(node);
	}

	/**
	 * 收集生成中心child的统计数据
	 * 
	 * @param node
	 */
	private void countHeadGR(HeadTreeNodeForCollins node)
	{
		String headLabel = node.getChild(node.getHeadChildIndex()).getNodeName();
		String parentLabel = node.getNodeName();
		String headword = node.getHeadWord();
		String headpos = node.getHeadPos();
		
		// 回退模型1
		OccurenceHeadChild hcgr0 = new OccurenceHeadChild(headLabel, parentLabel, headpos, headword);
		OccurenceHeadChild hcgr1 = new OccurenceHeadChild(null, parentLabel, headpos, headword);
		addGenerateRule(hcgr0, hcgr1, headGenMap);

		// 回退模型二
		OccurenceHeadChild hcgr2 = new OccurenceHeadChild(headLabel, parentLabel, headpos, null);
		OccurenceHeadChild hcgr3 = new OccurenceHeadChild(null, parentLabel, headpos, null);
		addGenerateRule(hcgr2, hcgr3, headGenMap);

		// 回退模型三
		OccurenceHeadChild hcgr4 = new OccurenceHeadChild(headLabel, parentLabel, null, null);
		OccurenceHeadChild hcgr5 = new OccurenceHeadChild(null, parentLabel, null, null);
		addGenerateRule(hcgr4, hcgr5, headGenMap);

		// 在解析中需要用此得到候选的父节点，以免遍历不需要的非终结符
		OccurenceHeadChild rhcg0 = new OccurenceHeadChild(headLabel, null, headpos, headword);
		addParents(rhcg0, parentLabel);
		
		OccurenceHeadChild rhcg = new OccurenceHeadChild(headLabel, null, headpos, null);
		addParents(rhcg, parentLabel);
		
		OccurenceHeadChild rhcg1 = new OccurenceHeadChild(headLabel, null, null, null);
		addParents(rhcg1, parentLabel);
	}

	/**
	 * 添加向上延伸的规则
	 * 
	 * @param rhcg
	 */
	private void addParents(OccurenceHeadChild rhcg, String parentLabel)
	{
		if (!parentList.containsKey(rhcg))
		{
			HashSet<String> labelSet = new HashSet<String>();
			labelSet.add(parentLabel);
			parentList.put(rhcg, labelSet);
		}
		else
		{
			parentList.get(rhcg).add(parentLabel);
		}
	}

	/**
	 * 统计在已有中心Child的基础上生成两侧孩子的数据
	 * 
	 * @param node
	 */
	private void countSidesGR(HeadTreeNodeForCollins node)
	{
		int headIndex = node.getHeadChildIndex();// 中心节点标记
		String parentLabel = node.getNodeName();// 父节点的非终结符标记
		String headPOS = node.getHeadPos();// 中心词词性标记
		String headWord = node.getHeadWord();// 中心词
		String headLabel = node.getChildName(headIndex);// 中心孩子的标记

		// 单独取左侧生成规则
		for (int i = headIndex - 1; i >= 0; i--)
		{
			addOneSideGR(1, i, parentLabel, headLabel, headPOS, headWord, headIndex, node);
		}
		
		// 单独取右侧的生成规则
		for (int i = headIndex + 1; i < node.getChildrenNum(); i++)
		{
			addOneSideGR(2, i, parentLabel, headLabel, headPOS, headWord, headIndex, node);
		}

	}

	/**
	 * 收集生成Stop的统计数据 若存在<direction,Stop,P,H,distance>则意味着可以在该处添加Stop
	 * 
	 * @param node
	 */
	private void countStopGR(HeadTreeNodeForCollins node)
	{
		int headIndex = node.getHeadChildIndex();
		String headLabel = node.getChild(headIndex).getNodeName();
		String parentLabel = node.getNodeName();
		String headWord = node.getHeadWord();
		String headPOS = node.getHeadPos();
		
		Distance leftDistance = getDistance(node, 1, headIndex, -1);// 左侧距离
		Distance rightDistance = getDistance(node, 2, headIndex, node.getChildrenNum());// 右侧距离

		if (parentLabel.equals("NPB"))
		{
			headWord = node.getFirstChildHeadWord();
			headPOS = node.getFirstChildHeadWordPos();
			headLabel = node.getFirstChildName();
			addOneSideStopGRule(true, parentLabel, headLabel, headPOS, headWord, new Distance(), 1);// 左侧 Stop符号
			headWord = node.getLastChildHeadWord();
			headPOS = node.getLastChildHeadPos();
			headLabel = node.getLastChildName();
			addOneSideStopGRule(true, parentLabel, headLabel, headPOS, headWord, new Distance(), 2);// 右侧Stop符号
			return;
		}

		addOneSideStopGRule(true, parentLabel, headLabel, headPOS, headWord, leftDistance, 1);// 左侧 Stop符号
		addOneSideStopGRule(true, parentLabel, headLabel, headPOS, headWord, rightDistance, 2);// 右侧Stop符号
	}

	/**
	 * 取中心节点两侧的某个孩子的数据 我将用rsg4判断此次生成两侧孩子是否可行
	 * 
	 * @param direction
	 * @param i
	 *            孩子的索引值
	 * @param parentLabel
	 * @param headLabel
	 * @param headPOS
	 * @param headWord
	 * @param headIndex
	 */
	private void addOneSideGR(int direction, int i, String parentLabel, String headLabel, String headPOS,
			String headWord, int headIndex, HeadTreeNodeForCollins node)
	{
		String sideLabel = node.getChildName(i);// 所求孩子节点的标记
		String sideHeadPOS = node.getChildHeadPos(i);// 所求孩子节点的中心词词标记
		String sideHeadWord = node.getChildHeadWord(i);// 所求的孩子节点的中心词
		
		boolean coor = false;// 并列结构,0为不设值，1和2为有或者没有
		boolean pu = false;// 标点符号，由于只保留了顿号所以我们可以把它当做并列结构，并列结构,0为不设值，1和2为有或者没有
		Distance distance = getDistance(node, direction, headIndex, i);

		// 基本名词短语单独处理
		if (node.getNodeName().equals("NPB"))
		{
			// NPB不需要距离度量，故将其设置为固定值（此处为false）
			distance = new Distance();

			if (i > headIndex)
			{ // 修饰符在headChild右侧
				headLabel = node.getChildName(i - 1);
				headPOS = node.getChildHeadPos(i - 1);
				headWord = node.getChildHeadWord(i - 1);
			}
			else if (i < headIndex)
			{ // 修饰符在headChild左侧
				headLabel = node.getChildName(i + 1);
				headPOS = node.getChildHeadPos(i + 1);
				headWord = node.getChildHeadWord(i + 1);
			}
		}
		// 在此处虽然不生成Stop但仍要统计其数据，在计算概率时使用
		addOneSideStopGRule(false, parentLabel, headLabel, headPOS, headWord, distance, direction);

		// 并列结构处理
		if (processCoorPU)
		{
			if ((i >= 1 && node.getChildName(i).equals("CC")) && node.getNodeName().equals(node.getChildName(i - 1))
					&& node.getNodeName().equals(node.getChildName(i + 1)))
			{
				addOneSideGROfCoor(direction, i, parentLabel, headLabel, headPOS, headWord, headIndex, node);
				return;
			}
			else if (i >= 2 && node.getChildName(i - 1).equals("CC"))
			{
				coor = true;
			}
		}
		
		// 生成两侧Label和pos的回退模型
		// 回退模型1
		OccurenceSides rsg0 = new OccurenceSides(headLabel, parentLabel, headPOS, headWord, direction, sideLabel,
				sideHeadPOS, null, coor, pu, distance);
		OccurenceSides rsg1 = new OccurenceSides(headLabel, parentLabel, headPOS, headWord, direction, null, null, null,
				coor, pu, distance);
		addGenerateRule(rsg0, rsg1, sidesGenMap);

		// 回退模型二
		OccurenceSides rsg2 = new OccurenceSides(headLabel, parentLabel, headPOS, null, direction, sideLabel,
				sideHeadPOS, null, coor, pu, distance);
		OccurenceSides rsg3 = new OccurenceSides(headLabel, parentLabel, headPOS, null, direction, null, null, null,
				coor, pu, distance);
		addGenerateRule(rsg2, rsg3, sidesGenMap);

		// 回退模型三
		OccurenceSides rsg4 = new OccurenceSides(headLabel, parentLabel, null, null, direction, sideLabel, sideHeadPOS,
				null, coor, pu, distance);
		OccurenceSides rsg5 = new OccurenceSides(headLabel, parentLabel, null, null, direction, null, null, null, coor,
				pu, distance);
		addGenerateRule(rsg4, rsg5, sidesGenMap);

		// 生成两侧word的回退模型
		// 回退模型1
		OccurenceSides rsgword0 = new OccurenceSides(headLabel, parentLabel, headPOS, headWord, direction, sideLabel,
				sideHeadPOS, sideHeadWord, coor, pu, distance);
		OccurenceSides rsgword1 = new OccurenceSides(headLabel, parentLabel, headPOS, headWord, direction, sideLabel,
				sideHeadPOS, null, coor, pu, distance);
		addGenerateRule(rsgword0, rsgword1, sidesGenMap);

		// 回退模型2
		OccurenceSides rsgword2 = new OccurenceSides(headLabel, parentLabel, headPOS, null, direction, sideLabel,
				sideHeadPOS, sideHeadWord, coor, pu, distance);
		OccurenceSides rsgword3 = new OccurenceSides(headLabel, parentLabel, headPOS, null, direction, sideLabel,
				sideHeadPOS, null, coor, pu, distance);
		addGenerateRule(rsgword2, rsgword3, sidesGenMap);
		// 回退模型3
		// 此刻的概率计算我使用词汇和pos信息直接计算
	}

	/**
	 * 并列结构规则的提取
	 * 
	 * @param direction
	 * @param i
	 * @param parentLabel
	 * @param headLabel
	 * @param headPOS
	 * @param headWord
	 * @param headIndex
	 * @param node
	 */
	private void addOneSideGROfCoor(int direction, int i, String parentLabel, String headLabel, String headPOS,
			String headWord, int headIndex, HeadTreeNodeForCollins node)
	{
		// 若为并列结构，也单独处理
		String cPOS = node.getChildName(i);
		String cWord = node.getChild(i).getChildName(0);
		HeadTreeNodeForCollins lnode = (HeadTreeNodeForCollins) node.getChild(i - 1);
		HeadTreeNodeForCollins rnode = (HeadTreeNodeForCollins) node.getChild(i + 1);

		// 回退模型1
		OccurenceSpecialCase rsg0 = new OccurenceSpecialCase(node.getNodeName(), cPOS, cWord, lnode.getNodeName(),
				rnode.getNodeName(), lnode.getHeadWord(), rnode.getHeadWord(), lnode.getHeadPos(), rnode.getHeadPos());
		OccurenceSpecialCase rsg1 = new OccurenceSpecialCase(node.getNodeName(), null, null, lnode.getNodeName(),
				rnode.getNodeName(), lnode.getHeadWord(), rnode.getHeadWord(), lnode.getHeadPos(), rnode.getHeadPos());
		addGenerateRule(rsg0, rsg1, specialGenMap);

		// 回退模型2
		OccurenceSpecialCase rsg2 = new OccurenceSpecialCase(node.getNodeName(), cPOS, cWord, lnode.getNodeName(),
				rnode.getNodeName(), null, null, lnode.getHeadPos(), rnode.getHeadPos());
		OccurenceSpecialCase rsg3 = new OccurenceSpecialCase(node.getNodeName(), null, null, lnode.getNodeName(),
				rnode.getNodeName(), null, null, lnode.getHeadPos(), rnode.getHeadPos());
		addGenerateRule(rsg2, rsg3, specialGenMap);

		// 回退模型3
		OccurenceSpecialCase rsg4 = new OccurenceSpecialCase(node.getNodeName(), cPOS, cWord, lnode.getNodeName(),
				rnode.getNodeName(), null, null, null, null);
		OccurenceSpecialCase rsg5 = new OccurenceSpecialCase(node.getNodeName(), null, null, lnode.getNodeName(),
				rnode.getNodeName(), null, null, null, null);
		addGenerateRule(rsg4, rsg5, specialGenMap);
	}

	/**
	 * 生成规则两侧stop的信息收集
	 * 
	 * @param stop
	 * @param parentLabel
	 * @param headLabel
	 * @param headPOS
	 * @param headWord
	 * @param distance
	 * @param direction
	 */
	private void addOneSideStopGRule(boolean stop, String parentLabel, String headLabel, String headPOS,
			String headWord, Distance distance, int direction)
	{
		// 生成两侧word的回退模型,输出只有两种，所以不需要单独添加计算概率和权重时的分母`
		// 回退模型1
		OccurenceStop sg0 = new OccurenceStop(headLabel, parentLabel, headPOS, headWord, direction, stop, distance);
		addGenerateRule(sg0, null, stopGenMap);
		
		// 回退模型2
		OccurenceStop sg2 = new OccurenceStop(headLabel, parentLabel, headPOS, null, direction, stop, distance);
		addGenerateRule(sg2, null, stopGenMap);
		
		// 回退模型3
		OccurenceStop sg4 = new OccurenceStop(headLabel, parentLabel, null, null, direction, stop, distance);
		addGenerateRule(sg4, null, stopGenMap);
	}

	/**
	 * 得到特定孩子到中心孩子的距离（默认verb和邻接为false）
	 * 
	 * @param node
	 * @param direction
	 * @param headIndex
	 * @param i
	 *            左右两侧的距离，因为存在stop所以i的范围为[-1，n],n为孩子的数目
	 * @return
	 */
	private Distance getDistance(HeadTreeNodeForCollins node, int direction, int headIndex, int i)
	{
		Distance distance = new Distance();
		// 邻接
		if (Math.abs(node.getHeadChildIndex() - i) == 1)
		{
			distance.setAdjacency(true);
			return distance;
		}
		
		// 非邻接
		int start, end;
		if (direction == 1)
		{
			start = i + 1;
			end = headIndex - 1;
		}
		else
		{
			start = headIndex + 1;
			end = i - 1;
		}
		
		for (int j = start; j <= end; j++)
		{
			HeadTreeNodeForCollins collinsNode = (HeadTreeNodeForCollins) node.getChild(j);
			if (collinsNode.isVerb())
			{
				distance.setCrossVerb(true);
				return distance;
			}
		}
		
		return distance;
	}

	/**
	 * 添加头结点的生成规则 在计算概率时，rule1指定子节点具体信息的规则，而rule2对应于做了平滑处理的规则（即回退，也就是子节点某处信息为null）
	 * rule1不需要计算子类型数目
	 * 
	 * @param specificOccur
	 * @param generalOccur
	 */
	private void addGenerateRule(OccurenceCollins specificOccur, OccurenceCollins generalOccur,
			HashMap<OccurenceCollins, RuleAmountsInfo> map)
	{
		if (generalOccur == null)
		{// 生成两侧stop时，不需要计算回退规则，故generalOccur为空
			if ((!map.containsKey(specificOccur)))
			{
				map.put(specificOccur, new RuleAmountsInfo(1, 0));
			}
			else
			{
				map.get(specificOccur).addAmount(1);
			}
			return;
		}

		if (!map.containsKey(generalOccur))
		{// 回退规则不存在
			map.put(specificOccur, new RuleAmountsInfo(1, 0));
			map.put(generalOccur, new RuleAmountsInfo(1, 1));
		}
		else
		{
			if (!map.containsKey(specificOccur))
			{
				map.put(specificOccur, new RuleAmountsInfo(1, 0));
				map.get(generalOccur).addSubtypeAmount(1);// specificOccur不存在，则意味着回退模型generalOccur的子类型数目需要+1
			}
			else
			{
				map.get(specificOccur).addAmount(1);
			}
			
			// 不管specificOccur是否已经存在，回退规则generalOccur的数目都要+1
			map.get(generalOccur).addAmount(1);
		}
	}
}
