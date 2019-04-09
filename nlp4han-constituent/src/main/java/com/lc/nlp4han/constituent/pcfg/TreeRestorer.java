package com.lc.nlp4han.constituent.pcfg;


import com.lc.nlp4han.constituent.TreeNode;

/**
 * 用pcnf进行剖析句子时，将得到的树恢复为宾州树的工具类
 * 
 * @author qyl
 *
 */
public class TreeRestorer
{

	/**
	 * 将二叉化树还原成正常的树
	 * 
	 * @param node
	 * @return
	 */
	public static TreeNode restoreTree(TreeNode node)
	{
		TreeNode tempRootNode = new TreeNode("tempRoot");
		
		traverseConvert(tempRootNode, node, 0);
		
		return tempRootNode.getChild(0);
	}

	/**
	 * 先序遍历
	 * 
	 * @param parentNode
	 * @param node
	 * @param num
	 */
	private static void traverseConvert(TreeNode parentNode, TreeNode node, int num)
	{
		String lhs = node.getNodeName();

		// 若为两个非终结符合并形成的伪非终结符，跳过
		if (lhs.contains("&"))
		{// 直接处理node的孩子，也就是相当于parentNode的第二个和第三个孩子
			traverseConvert(parentNode, node.getChild(0), 1);
			traverseConvert(parentNode, node.getChild(1), 2);
			
			return;
		}
		
		TreeNode newNode = null;
		// 若为伪非终结符，跳过直接处理孩子
		if (lhs.contains("$"))
		{
			newNode = new TreeNode(node.getChildName(0));
			addChild(newNode, parentNode, num);
			
			return;
		}

		if (!lhs.contains("@"))
		{
			newNode = new TreeNode(lhs);
			addChild(newNode, parentNode, num);
		}
		else
		{// 单元规则
			String[] strs = lhs.split("@");
			for (int i = 0; i < strs.length; i++)
			{
				newNode = new TreeNode(strs[i]);
				addChild(newNode, parentNode, 0);
				parentNode = newNode;
			}
		}

		if (node.getChildrenNum() == 0)
		{
			newNode.setWordIndex(node.getWordIndex());
			return;
		}
		else
		{
			for (int i = 0; i < node.getChildrenNum(); i++)
			{
				traverseConvert(newNode, node.getChild(i), i);
			}
		}

	}

	/**
	 * 添加孩子
	 * 
	 * @param node
	 * @param parentNode
	 * @param num
	 */
	private static void addChild(TreeNode node, TreeNode parentNode, int num)
	{
		node.setIndex(num);
		node.setParent(parentNode);
		parentNode.addChild(node);
	}
}
