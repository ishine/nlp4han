package com.lc.nlp4han.constituent;

import java.util.*;

/**
 * 成分树工具方法
 * 
 * @author 杨智超
 * @author 邱宜龙
 *
 */
public class TreeNodeUtil
{
	// 获得树的叶子或词的序列
	public static String[] getetWords(TreeNode tree)
	{
		List<String> words = new ArrayList<String>();
		traverseTree(tree, words, null);
		String[] words1 = new String[words.size()];
		for (int i = 0; i < words.size(); i++)
		{
			words1[i] = words.get(i);
		}
		return words1;
	}

	// 获得树的叶子或词的串
	public static String getetWordString(TreeNode tree)
	{
		List<String> words = new ArrayList<String>();
		traverseTree(tree, words, null);
		
		String str = new String();
		for (int i = 0; i < words.size(); i++)
		{
			str += words.get(i);
		}
		
		return str;
	}

	// 获得树的叶子或词的个数
	public static int getLengthFromTree(TreeNode tree)
	{
		List<String> words = new ArrayList<String>();
		traverseTree(tree, words, null);
		return words.size();
	}

	// 获得树的词和词性序列
	public static void getWordsAndPOSFromTree(ArrayList<String> words, ArrayList<String> poses, TreeNode tree)
	{
		traverseTree(tree, words, poses);
	}

	private static void traverseTree(TreeNode node, List<String> words, ArrayList<String> poses)
	{
		if (node.getChildrenNum() == 0)
		{
			if (poses != null)
			{
				poses.add(node.getParent().getNodeName());
			}

			words.add(node.getNodeName());
		}

		for (TreeNode node1 : node.getChildren())
		{
			traverseTree(node1, words, poses);
		}
	}

	/**
	 * 至结点treeNode向上搜索，返回第一个结点名为treeNodeName的结点；若treeNodeName为null，则返回treeNode的父节点
	 * 
	 * @param treeNode
	 *            起始结点
	 * @param treeNodeName
	 *            匹配的结点名
	 * 
	 * @return 返回指定结点名的结点，若不存在，则返回null
	 */
	public static TreeNode getFirstNodeUpWithSpecifiedName(TreeNode treeNode, String treeNodeName)
	{
		return getFirstNodeUpWithSpecifiedName(treeNode, new String[] { treeNodeName });
	}

	/**
	 * 至结点treeNode向上搜索，返回的第一个结点名在treeNodeNames中的结点；若treeNodeNames为null，则返回treeNode的父节点
	 * 
	 * @param treeNode
	 * @param treeNodeNames
	 * @return
	 */
	public static TreeNode getFirstNodeUpWithSpecifiedName(TreeNode treeNode, String[] treeNodeNames)
	{
		List<TreeNode> tmp = getNodesUpWithSpecifiedName(treeNode, treeNodeNames);
		if (tmp != null && !tmp.isEmpty())
		{
			return tmp.get(0);
		}
		return null;
	}

	/**
	 * 至结点treeNode向上搜索，返回的结点名在treeNodeNames中的所有结点；若treeNodeNames为null，则返回treeNode的父节点
	 * 
	 * @param treeNode
	 * @param treeNodeNames
	 * @return
	 */
	public static List<TreeNode> getNodesUpWithSpecifiedName(TreeNode treeNode, String[] treeNodeNames)
	{
		List<TreeNode> result = new LinkedList<TreeNode>();

		if (treeNode == null)
			return null;
		TreeNode tmp = treeNode.getParent();
		while (tmp != null)
		{
			if (isNodeWithSpecifiedName(tmp, treeNodeNames))
				result.add(tmp);
			tmp = tmp.getParent();
		}

		return result;
	}

	/**
	 * 从结点childNode至其祖先结点ancestorNode之间，获取结点名符合treeNodeNames的所有结点
	 * 
	 * @param childNode
	 *            孙子结点，起始结点
	 * @param ancestorNode
	 *            祖先结点，终止节点
	 * @param treeNodeNames
	 *            结点名
	 * @return 若存在符合条件的TreeNode，则返回；若不存在，则返回null
	 */
	public static List<TreeNode> getNodesWithSpecifiedNameBetween2Nodes(TreeNode childNode, TreeNode ancestorNode,
			String[] treeNodeNames)
	{
		List<TreeNode> candidates = getNodesUpWithSpecifiedName(childNode, treeNodeNames);
		if (candidates != null && !candidates.isEmpty())
		{
			List<TreeNode> result = new LinkedList<TreeNode>();
			for (int i = 0; i < candidates.size(); i++)
			{
				TreeNode tmp = candidates.get(i);
				if (tmp != ancestorNode)
					result.add(tmp);
				if (tmp == ancestorNode)
					return result;
			}
		}
		return candidates;
	}

	/**
	 * 结点treeNode是否为指定结点名的结点。若treeNode的结点名在treeNodeNames中，则返回true；否则，返回false。若treeNodeNames为空，返回true
	 * 
	 * @param treeNode
	 *            被检测的结点
	 * @param treeNodeNames
	 *            结点名集
	 * @return 若treeNode的结点名在treeNodeNames中，则返回true；否则，返回false。若treeNodeNames为空，返回true
	 */
	public static boolean isNodeWithSpecifiedName(TreeNode treeNode, String[] treeNodeNames)
	{
		if (treeNode != null)
		{
			if (treeNodeNames == null)
				return true;
			for (int i = 0; i < treeNodeNames.length; i++)
			{
				if (treeNode.getNodeName().equals(treeNodeNames[i]))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取结点parentNode指点结点名的子结点。
	 * 
	 * @param parentNode
	 * @param treeNodeNames
	 * @return
	 */
	public static List<TreeNode> getChildNodeWithSpecifiedName(TreeNode parentNode, String[] treeNodeNames)
	{
		List<TreeNode> result = new LinkedList<TreeNode>();
		if (parentNode != null && parentNode.getChildrenNum() > 0)
		{
			List<? extends TreeNode> children = parentNode.getChildren();
			for (TreeNode child : children)
			{
				if (isNodeWithSpecifiedName(child, treeNodeNames))
					result.add(child);
			}
		}
		return result;
	}

	/**
	 * 获取根结点
	 * 
	 * @param treeNode
	 * @return
	 */
	public static TreeNode getRootNode(TreeNode treeNode)
	{
		TreeNode result;
		if (treeNode != null)
		{
			result = treeNode;
			while (result.getParent() != null)
			{
				result = result.getParent();
			}
			return result;
		}
		return null;
	}

	public static List<TreeNode> getNodesWithSpecifiedName(TreeNode treeNode, String[] treeNodeNames)
	{
		return getNodesWithSpecifiedNameOnLeftOrRightOfPath(treeNode, null, null, treeNodeNames);
	}

	/**
	 * 在根节点rootNode下，从左至右，广度优先搜索路径path的左侧或右侧的结点，返回结点名符合treeNodeNames的所有结点
	 * 
	 * @param rootNode
	 *            根节点
	 * @param path
	 *            路径
	 * @param leftOrRight
	 *            路径的左侧或右侧，该变量取值为"left"、"right"
	 * @param treeNodeNames
	 *            指定结点名
	 * @return 返回符合要求的结点
	 */
	public static List<TreeNode> getNodesWithSpecifiedNameOnLeftOrRightOfPath(TreeNode rootNode, Path path,
			String leftOrRight, String[] treeNodeNames)
	{
		List<TreeNode> result = new ArrayList<TreeNode>();
		List<TreeNode> buffer = new LinkedList<TreeNode>();

		if (path != null || leftOrRight != null)
		{
			if (path == null)
			{
				throw new RuntimeException("参数错误：无路径，但有路径方向！");
			}
			if (!path.contains(rootNode)) // rootNode必须在path中
			{
				throw new RuntimeException("路径不对");
			}
			if (!leftOrRight.equalsIgnoreCase("left") && !leftOrRight.equalsIgnoreCase("right"))
				throw new RuntimeException("参数leftOrRight不对，只能为\"left\"或\"right\"");
		}
		buffer.add(rootNode);
		collectNodesWithSpecifiedName(buffer, result, path, treeNodeNames, leftOrRight);
		return result;
	}

	private static void processNodeOnPath(TreeNode treeNode, List<TreeNode> buffer, Path path, String leftOrRight)
	{
		if (path == null && leftOrRight == null)
		{// 若路径path和左右标记leftOrRight同时为null时，按无路径条件，正常从左至右广度优先遍历
			for (int i = 0; i < treeNode.getChildrenNum(); i++)
			{
				buffer.add(treeNode.getChild(i));
			}
			return;
		}
		if (leftOrRight.equalsIgnoreCase("left"))
		{// 若遍历路径path左侧
			TreeNode chileNodeOfRootInPath = path.getChileNodeInPath(treeNode);
			if (chileNodeOfRootInPath != null)
			{
				for (int i = 0; i <= treeNode.getChildren().indexOf(chileNodeOfRootInPath); i++)
				{
					buffer.add(treeNode.getChild(i));
				}
			}
		}
		else if (leftOrRight.equalsIgnoreCase("right"))
		{// 若遍历路径path右侧
			TreeNode chileNodeOfRootInPath = path.getChileNodeInPath(treeNode);
			if (chileNodeOfRootInPath != null)
			{
				for (int i = treeNode.getChildren().indexOf(chileNodeOfRootInPath); i < treeNode.getChildrenNum(); i++)
				{
					buffer.add(treeNode.getChild(i));
				}
			}
		}
	}

	private static void collectNodesWithSpecifiedName(List<TreeNode> buffer, List<TreeNode> result, Path path,
			String[] treeNodeNames, String leftOrRight)
	{
		TreeNode searchedNode;
		while (buffer.size() > 0)
		{
			searchedNode = buffer.remove(0);
			if (path == null || !path.contains(searchedNode))
			{
				if (TreeNodeUtil.isNodeWithSpecifiedName(searchedNode, treeNodeNames))
				{
					result.add(searchedNode);
				}
			}

			if (path != null && path.contains(searchedNode))
			{
				processNodeOnPath(searchedNode, buffer, path, leftOrRight);
			}
			else
			{
				for (int i = 0; i < searchedNode.getChildrenNum(); i++)
				{
					TreeNode nodeToBeAdded;
					nodeToBeAdded = searchedNode.getChild(i);
					buffer.add(nodeToBeAdded);
				}
			}
		}
	}

	/**
	 * 获取NP结点的中心词
	 * 
	 * @param node
	 * @return
	 */
	public static TreeNode getHead(TreeNode node)
	{
		if (node.getChildrenNum() == 0)
			return node;
		
		AbstractHeadGenerator headGen = new HeadGeneratorCollins(new HeadRuleSetCTB());
		HeadTreeNode headTree1 = TreeToHeadTree.treeToHeadTree(node, headGen);

		String headWord = headTree1.getHeadWord();

		List<TreeNode> leaves = getAllLeafNodes(node);
		TreeNode headNode = getLastNodeWithSpecifiedName(leaves, new String[] { headWord });

		return headNode;
	}

	private static TreeNode getFirstNodeFromRightToLeft(List<? extends TreeNode> nodes, String[] taggers)
	{
		TreeNode result = null;
		boolean flag = false;
		if (nodes.size() > 0)
		{
			for (String tmp : taggers)
			{
				if (tmp.equals("POS"))
				{
					flag = true;
					break;
				}
			}
			for (int i = nodes.size() - 1; i >= 0; i--)
			{
				for (String tagger : taggers)
				{
					if (flag && i > 0 && nodes.get(i).getNodeName().equals("NP")
							&& nodes.get(i - 1).getNodeName().equals("DNP"))
						return nodes.get(i);
					if (nodes.get(i).getNodeName().equals(tagger))
						return nodes.get(i);
				}
			}
		}
		return result;
	}

	private static TreeNode getFirstNodeFromLeftToRight(List<? extends TreeNode> nodes, String[] taggers)
	{
		TreeNode result = null;
		if (nodes.size() > 0)
		{
			for (int i = 0; i < nodes.size(); i++)
			{
				for (String tagger : taggers)
				{
					if (nodes.get(i).getNodeName().equals(tagger))
						return nodes.get(i);
				}
			}
		}
		return result;
	}

	/**
	 * 是否为并列结构的NP结点
	 * 
	 * @param treeNode
	 * @return
	 */
	public static boolean isCoordinatingNP(TreeNode treeNode)
	{
		if (treeNode == null || !treeNode.getNodeName().equals("NP"))
		{
			throw new RuntimeException("NPNode错误！");
		}
		List<? extends TreeNode> childrenNodes = treeNode.getChildren();

		if (allNodeNames(childrenNodes, new String[] { "NN", "NR", "CC", "PU", "NP" }))
		{
			List<TreeNode> pus = getNodesWithSpecifiedName(treeNode, new String[] { "、" });
			List<TreeNode> ccs = getNodesWithSpecifiedName(treeNode, new String[] { "CC" });
			int num = pus.size() + ccs.size();
			if (num >= childrenNodes.size() * 0.2)
				return true;
		}

		return false;

	}

	/**
	 * treeNodes中所有结点的结点名是否都在nodeNames中
	 * 
	 * @param treeNodes
	 *            所有结点
	 * @param nodeNames
	 *            所有结点名
	 * @return 若treeNodes中所有结点的结点名都在nodeNames中，则返回true；否则，返回false
	 */
	public static boolean allNodeNames(List<? extends TreeNode> treeNodes, String[] nodeNames)
	{
		if (treeNodes == null || nodeNames == null)
		{
			throw new RuntimeException("输入错误！");
		}
		boolean flag = true;
		for (TreeNode treeNode : treeNodes)
		{
			if (!isNodeWithSpecifiedName(treeNode, nodeNames))
			{
				flag = false;
				break;
			}
		}
		return flag;
	}

	/**
	 * treeNodes中是否至少存在一个结点的结点名为nodeName的值
	 * 
	 * @param treeNodes
	 * @param nodeName
	 * @return
	 */
	public static boolean hasNodeName(List<? extends TreeNode> treeNodes, String nodeName)
	{
		if (treeNodes == null || nodeName == null)
		{
			throw new RuntimeException("输入错误！");
		}
		for (TreeNode treeNode : treeNodes)
		{
			if (treeNode.getNodeName().equals(nodeName))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取treeNodes中，结点名在nodesNames中的最后一个结点
	 * 
	 * @param treeNodes
	 * @param nodeNames
	 * @return
	 */
	public static TreeNode getLastNodeWithSpecifiedName(List<TreeNode> treeNodes, String[] nodeNames)
	{
		TreeNode result;
		for (int i = treeNodes.size() - 1; i >= 0; i--)
		{
			result = treeNodes.get(i);
			if (isNodeWithSpecifiedName(result, nodeNames))
			{
				return result;
			}
		}
		return null;
	}

	/**
	 * 获得根节点rootNode下，所有终止符组成的字符串
	 * 
	 * @param rootNode
	 * @return
	 */
	public static String getString(TreeNode rootNode)
	{
		if (rootNode != null)
		{
			StringBuilder result = new StringBuilder();
			List<TreeNode> LeafNodes = getAllLeafNodes(rootNode);
			for (TreeNode node : LeafNodes)
			{
				result.append(node.getNodeName());
			}
			return result.toString();
		}
		return null;

	}

	/**
	 * 获得根结点rootNode下，所有的叶子结点
	 * 
	 * @param rootNode
	 * @return
	 */
	public static List<TreeNode> getAllLeafNodes(TreeNode rootNode)
	{
		if (rootNode == null)
		{
			throw new RuntimeException("输入错误！");
		}
		List<TreeNode> buffer = new LinkedList<TreeNode>();
		List<TreeNode> result = new ArrayList<TreeNode>();
		buffer.add(rootNode);

		while (!buffer.isEmpty())
		{
			TreeNode tmp = buffer.remove(buffer.size() - 1);

			if (tmp.getChildrenNum() == 0)
			{
				result.add(tmp);
			}
			else
			{
				for (int i = tmp.getChildrenNum() - 1; i >= 0; i--)
				{
					buffer.add(tmp.getChild(i));
				}
			}
		}
		return result;
	}

	/**
	 * 获得根节点rootNode下的所有NP结点
	 * 
	 * @param rootNode
	 *            根结点
	 * @return 根节点rootNode下的所有NP结点
	 */
	public static List<TreeNode> getNPNodes(TreeNode rootNode)
	{
		return TreeNodeUtil.getNodesWithSpecifiedName(rootNode, new String[] { "NP" });
	}

	/**
	 * 结点treeNode是否为NP结点
	 * 
	 * @param treeNode
	 *            被验证的结点
	 * @return 若存在，若该节点是NP结点，返回true；否则，返回false
	 */
	public static boolean isNPNode(TreeNode treeNode)
	{
		return TreeNodeUtil.isNodeWithSpecifiedName(treeNode, new String[] { "NP" });
	}

	/**
	 * 结点treeNode是否为IP结点
	 * 
	 * @param treeNode
	 *            被验证的结点
	 * @return 若该节点是IP结点，返回true；否则，返回false
	 */
	public static boolean isIPNode(TreeNode treeNode)
	{
		return TreeNodeUtil.isNodeWithSpecifiedName(treeNode, new String[] { "IP" });
	}

	/**
	 * 根结点rootNode下，路径path左侧，从左至右，广度优先遍历得到的所有NP结点
	 * 
	 * @param rootNode
	 * @param path
	 * @return
	 */
	public static List<TreeNode> getNPNodeOnLeftOfPath(TreeNode rootNode, Path path)
	{
		List<TreeNode> result = TreeNodeUtil.getNodesWithSpecifiedNameOnLeftOrRightOfPath(rootNode, path, "Left",
				new String[] { "NP" });
		return result;
	}

	/**
	 * 从treeNode开始，向上遍历，找到第一个NP结点
	 * 
	 * @param treeNode
	 *            起始结点
	 * @return 若存在，返回符合的NP结点；若不存在，则返回null
	 */
	public static TreeNode getFirstNPNodeUp(TreeNode treeNode)
	{
		return TreeNodeUtil.getFirstNodeUpWithSpecifiedName(treeNode, "NP");
	}

	/**
	 * 从treeNode开始，向上遍历，找到第一个NP或IP结点
	 * 
	 * @param treeNode
	 * @return 若存在，返回符合的NP结点；若不存在，则返回null
	 */
	public static TreeNode getFirstNPOrIPNodeUp(TreeNode treeNode)
	{
		return TreeNodeUtil.getFirstNodeUpWithSpecifiedName(treeNode, new String[] { "NP", "IP" });
	}

	/**
	 * 找到结点在其兄弟节点中的位置
	 * 
	 * @param node
	 * @return
	 */
	public static int getIndex(TreeNode node)
	{
		if (node == null)
			throw new RuntimeException("输入错误");
		if (node.getParent() != null)
		{
			TreeNode father = node.getParent();
			for (int i = 0; i < father.getChildrenNum(); i++)
			{
				if (father.getChild(i) == node)
				{
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * 根据结点名与其位置，找出树中其对应的结点
	 * 
	 * @param nodeName
	 * @param site
	 * @param root
	 * @return
	 */
	public static TreeNode string2Node(String nodeName, int site, TreeNode root)
	{
		if (nodeName == null || site < 0 || root == null)
			throw new RuntimeException("输入错误");
		List<TreeNode> leaves = getAllLeafNodes(root);
		if (leaves.get(site).getNodeName().equals(nodeName))
		{
			return leaves.get(site);
		}
		else
			return null;
	}

	/**
	 * 找出叶子结点的位置
	 * 
	 * @param leaf
	 * @return
	 */
	public static int siteOfLeaves(TreeNode leaf)
	{
		if (leaf == null)
			throw new RuntimeException("输入错误");
		if (leaf.getChildrenNum() > 0)
			throw new RuntimeException("非叶子结点");
		TreeNode root = getRootNode(leaf);
		List<TreeNode> leaves = getAllLeafNodes(root);
		return leaves.indexOf(leaf);

	}
}
