package com.lc.nlp4han.constituent.unlex;

import java.util.ArrayList;
import java.util.List;

import com.lc.nlp4han.constituent.TreeNode;

/**
 * 将树二叉化与反二叉化
 * 
 * @author 王宁
 */
public class TreeBinarization
{
	public static TreeNode binarize(TreeNode normalTree)
	{
		List<TreeNode> children = new ArrayList<>(normalTree.getChildren());
		if ((children.size() == 1 && children.get(0).getChildren().isEmpty()) || children.isEmpty())
			return normalTree;
		
		List<TreeNode> newChildren = new ArrayList<TreeNode>(children.size());
		for (TreeNode child : children)
			newChildren.add(binarize(child));

		children = newChildren;
		if (children.size() >= 3)
		{
			// children转化为left、right child
			TreeNode leftChild = new TreeNode("#" + normalTree.getNodeName());
			TreeNode rightChild = children.get(children.size() - 1);
			
			leftChild.setParent(normalTree);
			rightChild.setParent(normalTree);
			
			List<TreeNode> tempChildrenOfLC = new ArrayList<TreeNode>(2);
			tempChildrenOfLC.add(children.get(0));
			tempChildrenOfLC.add(children.get(1));
			
			for (int i = 2; i < children.size() - 1; i++)
			{
				TreeNode tempTree = new TreeNode("#" + normalTree.getNodeName());
				tempTree.addChild(tempChildrenOfLC.toArray(new TreeNode[2]));
				
				tempTree.getChild(0).setParent(tempTree);
				tempTree.getChild(1).setParent(tempTree);
				
				tempChildrenOfLC = new ArrayList<TreeNode>(2);
				tempChildrenOfLC.add(tempTree);
				tempChildrenOfLC.add(children.get(i));
			}
			
			leftChild.addChild(tempChildrenOfLC.toArray(new TreeNode[2]));
			leftChild.getChild(0).setParent(leftChild);
			leftChild.getChild(1).setParent(leftChild);
			
			children = new ArrayList<TreeNode>(2);
			children.add(leftChild);
			children.add(rightChild);
		}

		normalTree.setChildren(children);

		return normalTree;
	}

	private static void recoverBinaryTreeHelper(TreeNode binaryTree, List<TreeNode> realChildren)
	{
		if (binaryTree.getChildren().get(0).getNodeName().startsWith("#"))
		{
			recoverBinaryTreeHelper(binaryTree.getChildren().get(0), realChildren);
			realChildren.add(binaryTree.getChildren().get(1));
		}
		else
		{
			realChildren.add(binaryTree.getChildren().get(0));
			realChildren.add(binaryTree.getChildren().get(1));
		}
	}

	public static TreeNode unbinarize(TreeNode binaryTree)
	{
		if (binaryTree.isLeaf() || (binaryTree.getChildren().size() == 1 && binaryTree.getChildren().get(0).isLeaf()))
			return binaryTree;
		
		if (binaryTree.getChildren().get(0).getNodeName().startsWith("#"))
		{
			List<TreeNode> tempChildren = new ArrayList<TreeNode>();
			recoverBinaryTreeHelper(binaryTree, tempChildren);
			
			List<TreeNode> realChildren = new ArrayList<TreeNode>(tempChildren.size());
			realChildren.addAll(tempChildren);
			binaryTree.setChildren(realChildren);
			for (TreeNode child : realChildren)
				child.setParent(binaryTree);
		}
		
		for (int i = 0; i < binaryTree.getChildren().size(); i++)
			unbinarize(binaryTree.getChildren().get(i));
		
		return binaryTree;
	}
}
