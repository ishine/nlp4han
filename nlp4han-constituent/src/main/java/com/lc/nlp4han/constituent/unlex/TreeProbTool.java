package com.lc.nlp4han.constituent.unlex;

import java.io.IOException;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.TreeNode;

/**
 * 给定一个文法计算树的概率
 * 
 * @author 王宁
 */
public class TreeProbTool
{
	// 返回树的log似然值
	public static double computeProb(Grammar latentGrammar, AnnotationTreeNode tree)
	{
		TreeBank.calculateInnerScore(latentGrammar, tree);
		// System.out
		// .println("score:" + tree.getLabel().getInnerScores()[0] + ",scale:" +
		// tree.getLabel().getInnerScale());
		return Math.log(tree.getAnnotation().getInnerScores()[0]) + 100 * tree.getAnnotation().getInnerScale();
	}

	public static void main(String args[])
	{
		try
		{
			Grammar grammar = LatentGrammarExtractorTool.getGrammar(args[0], args[1], 1, 0.5, 50,
					0.01, 10);
			
			TreeNode tree = TreeBinarization.binarize(BracketExpUtil.generateTree(
					"(ROOT(FRAG(NN 新华社)(NR 上海)(NT 二月)(NT 十日)(NN 电)(PU （)(NN 记者)(NR 谢金虎)(PU 、)(NR 张持坚)(PU ）)))"));
			
			AnnotationTreeNode theTree = grammar.toAnnotationTreeNode(tree);
			double score = computeProb(grammar, theTree);
			
			System.out.println("logS:" + score);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}
