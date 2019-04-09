package com.lc.nlp4han.constituent.lex;

import java.util.HashSet;

import com.lc.nlp4han.constituent.AbstractHeadGenerator;
import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.constituent.TreeNode;

/**
 * 将树改写为适合柯林斯模型1的头结点树
 * 
 * @author qyl
 *
 */
public class TreeToHeadTreeForCollins
{
	public static HeadTreeNodeForCollins treeToHeadTree(TreeNode treeNode, AbstractHeadGenerator headGen)
	{
		// 动词集合，用于检查是否包含动词
		String[] verbArray = { "VA", "VC", "VE", "VV", "BA", "LB" };
		HashSet<String> verbs = new HashSet<String>();
		for (String verb : verbArray)
			verbs.add(verb);

		// 作为临时的根节点
		HeadTreeNodeForCollins rootNode = new HeadTreeNodeForCollins("tempRoot");

		// 转换过程
		traverseConvert(rootNode, treeNode, 0, headGen, verbs);

		// 添加基本名词短语
		NPBUtil.labelNPB(rootNode.getChild(0));

		return (HeadTreeNodeForCollins) rootNode.getChild(0);
	}

	/**
	 * 利用递归将普通树转换为head树，类似后序遍历
	 * 
	 * @param parentNode
	 * @param treeNode
	 * @param i
	 * @param headGen
	 */
	private static void traverseConvert(HeadTreeNode parentNode, TreeNode treeNode, int i,
			AbstractHeadGenerator headGen, HashSet<String> verbs)
	{
		HeadTreeNodeForCollins node = new HeadTreeNodeForCollins(treeNode.getNodeName());
		parentNode.addChild(node);
		node.setIndex(i);
		node.setParent(parentNode);

		// 先遍历孩子
		for (int j = 0; j < treeNode.getChildrenNum(); j++)
			traverseConvert(node, treeNode.getChild(j), j, headGen, verbs);

		if (treeNode.getChildrenNum() == 0)
		{// 若为终结符节点
			node.setWordIndex(treeNode.getWordIndex());
		}
		else if (treeNode.getChildrenNum() == 1 && treeNode.getChild(0).getChildrenNum() == 0)
		{// 若为词性标注节点
			node.setHeadPos(treeNode.getNodeName());
			node.setHeadWord(treeNode.getChildName(0));
			node.setHeadChildIndex(0);

			// 动词检测
			if (verbs.contains(node.getNodeName()))
				node.setVerb(true);
		}
		else
		{
			node.setHeadWord(headGen.extractHeadWord(node));
			node.setHeadPos(headGen.extractHeadPos(node));
			node.setHeadChildIndex(headGen.extractHeadIndex(node));

			// 动词检测
			for (HeadTreeNode child : node.getChildren())
			{
				HeadTreeNodeForCollins child1 = (HeadTreeNodeForCollins) child;
				if (child1.isVerb() == true)
					node.setVerb(true);
			}
		}
	}
}
