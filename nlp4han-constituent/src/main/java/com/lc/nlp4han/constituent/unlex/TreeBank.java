package com.lc.nlp4han.constituent.unlex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.PlainTextByTreeStream;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;

/**
 * 表示树库
 * 
 * @author 王宁
 */
public class TreeBank
{
	public NonterminalTable nonterminalTable;
	private ArrayList<AnnotationTreeNode> treeBank;

	public TreeBank(String treeBankPath, boolean addParentLabel, String encoding) throws IOException
	{
		this.treeBank = new ArrayList<AnnotationTreeNode>();
		this.nonterminalTable = new NonterminalTable();
		
		init(treeBankPath, addParentLabel, encoding);
	}

	public TreeBank()
	{
		this.treeBank = new ArrayList<AnnotationTreeNode>();
		this.nonterminalTable = new NonterminalTable();
	}

	/**
	 * 从括号表达式文件读取句法树
	 * 
	 * @param treeBankPath
	 * @param addParentLabel
	 * @param encoding
	 * @throws IOException
	 */
	public void init(String treeBankPath, boolean addParentLabel, String encoding) throws IOException
	{
		PlainTextByTreeStream stream = new PlainTextByTreeStream(new FileInputStreamFactory(new File(treeBankPath)),
				encoding);
		String expression = stream.read();
		while (expression != "" && expression != null)
		{
			expression = expression.trim();
			if (!expression.equals(""))
				this.addTree(expression, addParentLabel);
			
			expression = stream.read();
		}
		
		stream.close();
	}

	public void addTree(String bracketStr, boolean addParentLabel)
	{
		TreeNode tree = BracketExpUtil.generateTree(bracketStr);
		
		tree = TreeUtil.removeL2LRule(tree);
		
		if (addParentLabel)
			tree = TreeUtil.addParentLabel(tree);
		
		tree = TreeBinarization.binarize(tree);
		
		AnnotationTreeNode annotatedTree = AnnotationTreeNode.getInstance(tree, this.nonterminalTable);
		treeBank.add(annotatedTree);
	}

	/**
	 * 根据树上任一节点内外向概率计算该句子的概率
	 * 
	 * @param node
	 * @return
	 */
	public static double calLogSentenceSocre(AnnotationTreeNode node)
	{
		if (node.getAnnotation().getInnerScores() == null || node.getAnnotation().getOuterScores() == null)
			throw new Error("没有计算树上节点的内外向概率。");
		
		if (node.isLeaf())
			throw new Error("不能利用叶子节点计算内外向概率。");
		
		double sentenceScore = 0.0;
		Double[] innerScore = node.getAnnotation().getInnerScores();
		Double[] outerScores = node.getAnnotation().getOuterScores();
		for (int i = 0; i < innerScore.length; i++)
			sentenceScore += innerScore[i] * outerScores[i];
		
		double logSenScore = Math.log(sentenceScore)
				+ 100 * (node.getAnnotation().getInnerScale() + node.getAnnotation().getOuterScale());
		
		return logSenScore;
	}

	public static double calSentenceSocreIgnoreScale(AnnotationTreeNode node)
	{
		if (node.getAnnotation().getInnerScores() == null || node.getAnnotation().getOuterScores() == null)
			throw new Error("没有计算树上节点的内外向概率。");
		
		if (node.isLeaf())
			throw new Error("不能利用叶子节点计算内外向概率。");
		
		double sentenceScore = 0.0;
		Double[] innerScore = node.getAnnotation().getInnerScores();
		Double[] outerScores = node.getAnnotation().getOuterScores();
		for (int i = 0; i < innerScore.length; i++)
			sentenceScore += innerScore[i] * outerScores[i];
		
		double logSenScore = sentenceScore;
		
		return logSenScore;
	}

	/**
	 * 计算整个树库的log似然值和
	 * 
	 * 使用前要确保计算了内外向概率
	 * 
	 * @return 整个树库的log似然值
	 */
	public double calcLogTreeBankSentenceSocre()
	{
		double totalLSS = 0;
		for (AnnotationTreeNode root : treeBank)
			totalLSS += calLogSentenceSocre(root);
		
		return totalLSS;
	}

	/**
	 * 计算树上所有节点的所有隐藏节点的内向概率及缩放比例
	 * 
	 * @param g
	 *            语法
	 * @param tree
	 *            树根
	 */
	public static void calculateInnerScore(Grammar g, AnnotationTreeNode tree)
	{
		if (tree.isLeaf())
			return;
		
		for (AnnotationTreeNode child : tree.getChildren())
			calculateInnerScore(g, child);

		if (tree.isPreterminal())
		{
			PreterminalRule tempPreRule = new PreterminalRule(tree.getAnnotation().getSymbol(),
					tree.getChildren().get(0).getAnnotation().getWord());
			int length = g.getNumSubSymbol(tree.getAnnotation().getSymbol());
			Double[] innerScores = new Double[length];
			if (g.getLexicon().getPreRules().contains(tempPreRule))
			{
				tempPreRule = g.getRule(tempPreRule);
				for (short i = 0; i < innerScores.length; i++)
					innerScores[i] = tempPreRule.getScore(i);
			}
			else
			{
				/*
				 * 以下for循环在TreeProbTool中调用该方法时可能会时用到，当使用词性标注解析句子但是出现未登录词的时候else会执行，
				 * 此时计算内向概率只是比较所有解析的可能的句子中概率最大的那个，所以将该规则tag_i->unk设置为1。
				 **/
				for (int i = 0; i < length; i++)
				{
					innerScores[i] = 1.0;
				}
				// System.err.println("不包含该preRule");
			}

			// 预终结符号的内向概率不用缩放，最小为e^-30
			tree.getAnnotation().setInnerScores(innerScores);
			tree.getAnnotation().setInnerScale(0);
		}
		else
		{
			Double[] innerScores;
			int length = g.getNumSubSymbol(tree.getAnnotation().getSymbol());
			switch (tree.getChildren().size())
			{
			case 1:
				UnaryRule tempUnaryRule = new UnaryRule(tree.getAnnotation().getSymbol(),
						tree.getChildren().get(0).getAnnotation().getSymbol());
				if (g.getuRules().contains(tempUnaryRule))
				{
					tempUnaryRule = g.getRule(tempUnaryRule);
					innerScores = new Double[length];
					int childInnerScale = tree.getChildren().get(0).getAnnotation().getInnerScale();
					for (short i = 0; i < innerScores.length; i++)
					{
						double innerScores_Ai = 0.0;
						for (short j = 0; j < g
								.getNumSubSymbol(tree.getChildren().get(0).getAnnotation().getSymbol()); j++)
						{ // 规则A_i -> B_j的概率
							double A_i2B_j = tempUnaryRule.getScore(i, j);
							double B_jInnerScore = tree.getChildren().get(0).getAnnotation().getInnerScores()[j];
							innerScores_Ai = innerScores_Ai + (A_i2B_j * B_jInnerScore);
						}
						innerScores[i] = innerScores_Ai;
					}
					int newScale = ScalingUtils.scaleArray(childInnerScale, innerScores);
					tree.getAnnotation().setInnerScores(innerScores);
					tree.getAnnotation().setInnerScale(newScale);
				}
				else
					throw new Error("Attention:grammar don't contains  uRule ");
				break;
			case 2:
				BinaryRule tempBRule = new BinaryRule(tree.getAnnotation().getSymbol(),
						tree.getChildren().get(0).getAnnotation().getSymbol(),
						tree.getChildren().get(1).getAnnotation().getSymbol());
				if (g.getbRules().contains(tempBRule))
				{
					tempBRule = g.getRule(tempBRule);
					innerScores = new Double[length];
					int leftChildInnerScale = tree.getChildren().get(0).getAnnotation().getInnerScale();
					int rightChildInnerScale = tree.getChildren().get(1).getAnnotation().getInnerScale();
					for (short i = 0; i < innerScores.length; i++)
					{
						double innerScores_Ai = 0.0;
						for (short j = 0; j < g
								.getNumSubSymbol(tree.getChildren().get(0).getAnnotation().getSymbol()); j++)
						{
							for (short k = 0; k < g
									.getNumSubSymbol(tree.getChildren().get(1).getAnnotation().getSymbol()); k++)
							{
								// 规则A_i -> B_j C_k的概率
								double A_i2B_jC_k = tempBRule.getScore(i, j, k);
								double B_jInnerScore = tree.getChildren().get(0).getAnnotation().getInnerScores()[j];
								double C_kInnerScore = tree.getChildren().get(1).getAnnotation().getInnerScores()[k];
								innerScores_Ai = innerScores_Ai + (A_i2B_jC_k * B_jInnerScore * C_kInnerScore);
							}
						}
						innerScores[i] = innerScores_Ai;
					}
					int newScale = ScalingUtils.scaleArray(leftChildInnerScale + rightChildInnerScale, innerScores);
					tree.getAnnotation().setInnerScores(innerScores);
					tree.getAnnotation().setInnerScale(newScale);
				}
				else
					throw new Error("Attention :grammar don't contains the bRule ");
				break;
			default:
				throw new Error("Error tree: more than two children.");
			}
		}
	}

	/**
	 * tree的根标记为ROOT
	 * 
	 * @param tree
	 */
	public static void calculateOuterScore(Grammar g, AnnotationTreeNode tree)
	{
		if (tree == null)
			return;
		
		calculateOuterScoreHelper(g, tree, tree);
	}

	private static void calculateOuterScoreHelper(Grammar g, AnnotationTreeNode treeRoot, AnnotationTreeNode treeNode)
	{
		if (treeNode == null)
			return;
		
		if (treeNode.isLeaf())
			return;
		
		// 计算根节点的外向概率
		if (treeNode == treeRoot)
		{
			Double[] array = new Double[g.getNumSubSymbol(treeNode.getAnnotation().getSymbol())];
			Arrays.fill(array, 1.0);
			treeNode.getAnnotation().setOuterScores(array);
			treeNode.getAnnotation().setOuterScale(0);
		}
		else
		{
			AnnotationTreeNode parent = treeNode.getParent();
			switch (parent.getChildren().size())
			{
			case 1:
				UnaryRule tempUnaryRule = new UnaryRule(parent.getAnnotation().getSymbol(),
						treeNode.getAnnotation().getSymbol());
				if (g.getuRules().contains(tempUnaryRule))
				{
					tempUnaryRule = g.getRule(tempUnaryRule);
					Double[] outerScores = new Double[g.getNumSubSymbol(treeNode.getAnnotation().getSymbol())];
					int parentOuterScale = parent.getAnnotation().getOuterScale();
					for (short j = 0; j < outerScores.length; j++)
					{
						double outerScores_Bj = 0.0;
						for (short i = 0; i < g.getNumSubSymbol(parent.getAnnotation().getSymbol()); i++)
						{
							double A_i2B_j = tempUnaryRule.getScore(i, j);
							double A_iOuterScore = parent.getAnnotation().getOuterScores()[i];
							outerScores_Bj = outerScores_Bj + (A_i2B_j * A_iOuterScore);
						}
						outerScores[j] = outerScores_Bj;
					}
					int newScale = ScalingUtils.scaleArray(parentOuterScale, outerScores);
					treeNode.getAnnotation().setOuterScores(outerScores);
					treeNode.getAnnotation().setOuterScale(newScale);
				}
				else
					throw new Error("Error grammar: don't contains  uRule :" + tempUnaryRule.toString());

				break;
			case 2:
				// 获取兄弟节点的内向概率
				Double[] siblingNode_InScore;
				BinaryRule tempBRule;
				int siblingInnerScale;
				int parentOuterScale = parent.getAnnotation().getOuterScale();
				if (parent.getChildren().get(0) == treeNode)
				{
					siblingNode_InScore = parent.getChildren().get(1).getAnnotation().getInnerScores();
					siblingInnerScale = parent.getChildren().get(1).getAnnotation().getInnerScale();
					tempBRule = new BinaryRule(parent.getAnnotation().getSymbol(), treeNode.getAnnotation().getSymbol(),
							parent.getChildren().get(1).getAnnotation().getSymbol());
				}
				else
				{
					siblingNode_InScore = parent.getChildren().get(0).getAnnotation().getInnerScores();
					siblingInnerScale = parent.getChildren().get(0).getAnnotation().getInnerScale();
					tempBRule = new BinaryRule(parent.getAnnotation().getSymbol(),
							parent.getChildren().get(0).getAnnotation().getSymbol(),
							treeNode.getAnnotation().getSymbol());
				}

				if (g.getbRules().contains(tempBRule))
				{
					tempBRule = g.getRule(tempBRule);
					Double[] outerScores = new Double[g.getNumSubSymbol(treeNode.getAnnotation().getSymbol())];
					for (short i = 0; i < outerScores.length; i++)
					{
						double outerScoreB_i = 0.0;
						for (short j = 0; j < g.getNumSubSymbol(parent.getAnnotation().getSymbol()); j++)
						{
							double A_jOuterscore = parent.getAnnotation().getOuterScores()[j];
							for (short k = 0; k < siblingNode_InScore.length; k++)
							{
								double C_kInnerScore = siblingNode_InScore[k];
								if (parent.getChildren().get(0) == treeNode)
								{

									double A_j2B_iC_k = tempBRule.getScore(j, i, k);
									outerScoreB_i = outerScoreB_i + (A_j2B_iC_k * A_jOuterscore * C_kInnerScore);

								}
								else
								{
									double A_j2C_kB_i = tempBRule.getScore(j, k, i);
									outerScoreB_i = outerScoreB_i + (A_j2C_kB_i * A_jOuterscore * C_kInnerScore);
								}

							}
						}
						outerScores[i] = outerScoreB_i;
					}
					int newScale = ScalingUtils.scaleArray(parentOuterScale + siblingInnerScale, outerScores);
					treeNode.getAnnotation().setOuterScores(outerScores);
					treeNode.getAnnotation().setOuterScale(newScale);
				}
				else
					throw new Error("Error grammar: don't contains  bRule :" + tempBRule.toString());

				break;
			default:
				throw new Error("error tree:more than two children.");
			}
		}
		
		for (AnnotationTreeNode childNode : treeNode.getChildren())
			calculateOuterScoreHelper(g, treeRoot, childNode);
	}

	public void calcIOScore(Grammar g)
	{
		for (AnnotationTreeNode tree : treeBank)
		{
			TreeBank.calculateInnerScore(g, tree);
			TreeBank.calculateOuterScore(g, tree);
		}
	}

	// 清空所有树的内外概率
	public void forgetIOScoreAndScale()
	{
		for (AnnotationTreeNode tree : treeBank)
			tree.forgetIOScoreAndScale();
	}

	public NonterminalTable getNonterminalTable()
	{
		return nonterminalTable;
	}

	public void setNonterminalTable(NonterminalTable nonterminalTable)
	{
		this.nonterminalTable = nonterminalTable;
	}

	public ArrayList<AnnotationTreeNode> getTreeBank()
	{
		return treeBank;
	}

	public void setTreeBank(ArrayList<AnnotationTreeNode> treeBank)
	{
		this.treeBank = treeBank;
	}

}
