package com.lc.nlp4han.constituent.unlex;

import java.util.ArrayList;
import com.lc.nlp4han.constituent.TreeNode;

/**
 * 处理树
 * 
 * @author 王宁
 */
public class TreeUtil
{

	/**
	 * 一棵树除了根节点以外，其他所有的节点均添加父节点的label
	 * 
	 * @param tree
	 * @return 一棵树除了根节点以外，其他所有的节点均添加了父节点label的树
	 */
	public static TreeNode addParentLabel(TreeNode tree)
	{
		if (tree == null || tree.isLeaf())
			return tree;
		for (TreeNode child : tree.getChildren())
		{
			addParentLabel(child);
			if (!child.isLeaf())
				child.setNewName(child.getNodeName() + "^" + tree.getNodeName());
		}
		return tree;
	}

	/**
	 * 由带父节点label的树的得到不带父节点label的树
	 * 
	 * @param tree
	 * @return 不带父节点label的树
	 */
	public static TreeNode removeParentLabel(TreeNode tree)
	{
		if (tree == null || tree.isLeaf())
		{
			return tree;
		}
		for (TreeNode child : tree.getChildren())
		{
			removeParentLabel(child);
		}
		tree.setNewName(tree.getNodeName().split("\\^")[0]);
		return tree;
	}

	public static TreeNode removeLatentLabel(TreeNode tree)
	{
		if (tree == null || tree.isLeaf())
		{
			return tree;
		}
		for (TreeNode child : tree.getChildren())
		{
			removeLatentLabel(child);
		}
		tree.setNewName(tree.getNodeName().split("_")[0]);
		return tree;
	}

	/**
	 * 移除树中例如A->A的结构
	 * 
	 * @param tree
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public static TreeNode removeL2LRule(TreeNode tree)
	{
		if (tree.getChildren().isEmpty()
				|| (tree.getChildren().size() == 1 && tree.getChildren().get(0).getChildren().isEmpty()))
			return tree;
		
		for (int i = 0; i < tree.getChildren().size(); i++)
			removeL2LRule(tree.getChildren().get(i));
		
		if (tree.getChildren().size() == 1 && !tree.getChildren().get(0).getChildren().isEmpty()
				&& tree.getNodeName().equals(tree.getChildren().get(0).getNodeName()))
		{
			tree.setChildren((ArrayList<TreeNode>) (tree.getChildren().get(0).getChildren()));
			
			for (TreeNode child : tree.getChildren())
				child.setParent(tree);
		}
		
		return tree;
	}

	public static AnnotationTreeNode forgetScore(AnnotationTreeNode tree)
	{
		if (tree.isLeaf() || tree == null)
			return tree;
		tree.getAnnotation().setInnerScores(null);
		tree.getAnnotation().setOuterScores(null);
		for (AnnotationTreeNode child : tree.getChildren())
		{
			forgetScore(child);
		}
		return tree;
	}

}
