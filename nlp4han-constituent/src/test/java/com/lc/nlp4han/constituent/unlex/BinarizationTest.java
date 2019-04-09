package com.lc.nlp4han.constituent.unlex;

import org.junit.Test;
import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.unlex.TreeBinarization;

public class BinarizationTest
{
	@Test
	public void binaryTest()
	{
		String expression = "(ROOT(FRAG(NN 新华社)(NR 上海)(NT 二月)(NT 十日)(NN 电)))";
		String binaryExp = "(ROOT(FRAG(#FRAG(#FRAG(#FRAG(NN 新华社)(NR 上海))(NT 二月))(NT 十日))(NN 电)))";
		TreeNode tree = BracketExpUtil.generateTree(expression);
		TreeNode binaryTreeExchanged = TreeBinarization.binarize(BracketExpUtil.generateTree(expression));
		TreeNode realBinaryTree = BracketExpUtil.generateTree(binaryExp);
		Identical.identical(binaryTreeExchanged, realBinaryTree);
		TreeNode treeRecovered = TreeBinarization.unbinarize(binaryTreeExchanged);
		Identical.identical(tree, treeRecovered);
	}

}
