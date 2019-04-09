package com.lc.nlp4han.constituent.lex;

import com.lc.nlp4han.constituent.TreeNode;

/**
 * 基本名词短语节点: 该节点为NP，并且以该节点为根的结构树中不存在NP
 *
 */
public class NPBUtil
{
	// 应该是用不到，虽然Collins论文中这样指明，但是在回溯恢复整个树结构时显得尤为麻烦
	/**
	 * 移除顿号以外的标点符号，后续将顿号（与CC功能类似）做并列结构处理
	 */
	/**
	 * public void removePunctuation(TreeNode node) { if
	 * (node.getNodeName().equals("PU")) { String pu = node.getFirstChildName(); if
	 * (!(pu.equals("、") )) { //移除 node.setFlag(false);
	 * node.getFirstChild().setFlag(false); node.getParent().setFlag(false); } } }
	 */
	
	/**
	 * 在句法树中添加基本名词短语节点 该节点为NP，并且以该节点为根的结构树中不存在NP
	 * 
	 * @param node
	 */
	public static void labelNPB(TreeNode node)
	{
//		AddNPBNode(node);
		
		if (isNPB(node))
		{
			node.setNewName("NPB");
		}
		
		if (node.getChildrenNum() == 0)
		{
			return;
		}
		else
		{
			for (TreeNode node1 : node.getChildren())
			{
				labelNPB(node1);
			}
		}
	}

//	private static void AddNPBNode(TreeNode node)
//	{
//		if (isNPB(node))
//		{
//			node.setNewName("NPB");
//		}
//	}

	private static boolean isNPB(TreeNode node)
	{
		if (!node.getNodeName().equals("NP"))
		{
			return false;
		}
		else
		{
			// 如果是类似NP->NN->中国，则不标记为NPB
			if (node.getChildrenNum() == 1 && node.getChild(0).getChildrenNum() == 0)
				return false;

			for (TreeNode child : node.getChildren())
			{
				if (!traverseNode(child))
					return false;
			}
			
			return true;
		}
	}

	private static boolean traverseNode(TreeNode node)
	{
		if (node.getNodeName().equals("NP"))
		{
			return false;
		}
		else
		{
			for (TreeNode child : node.getChildren())
			{
				if (!traverseNode(child))
				{
					return false;
				}
			}
		}
		return true;
	}
}
