package com.lc.nlp4han.srl.propbank;

import java.util.List;
import java.util.Stack;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.constituent.TreeNode;

/**
 * 将一棵树转换成语义角色标注树
 * @author 王馨苇
 *
 */
public class TreeToSRLTree {
	
	/**
	 * 将一颗树转成语义角色树
	 * @param tree 一颗树
	 * @return
	 */
	private static SRLTreeNode transferToSRLTreeNodeStructure(TreeNode treenode){
		String strtree = "("+treenode.toStringWordIndexNoNone()+")";
		String format = BracketExpUtil.formatNoTopBracket(strtree);
		List<String> parts = BracketExpUtil.stringToList(format);
		Stack<SRLTreeNode> tree = new Stack<SRLTreeNode>();
        for (int i = 0; i < parts.size(); i++) {
			if(!parts.get(i).equals(")") && !parts.get(i).equals(" ")){
				SRLTreeNode tn = new SRLTreeNode(parts.get(i));
				tree.push(tn);				
			}else if(parts.get(i).equals(" ")){
				
			}else if(parts.get(i).equals(")")){
				Stack<SRLTreeNode> temp = new Stack<SRLTreeNode>();
				while(!tree.peek().getNodeName().equals("(")){
					if(!tree.peek().getNodeName().equals(" ")){
						temp.push(tree.pop());						
					}
				}
				tree.pop();
				SRLTreeNode node = temp.pop();
				while(!temp.isEmpty()){		
					temp.peek().setParent(node);					
					if(temp.peek().getChildren().size() == 0){
						SRLTreeNode wordindexnode = temp.peek();
						String[] str = temp.peek().getNodeName().split("\\[");
						wordindexnode.setNewName(str[0]);
						wordindexnode.setWordIndex(Integer.parseInt(str[1].substring(0, str[1].length()-1)));
						node.addChild(wordindexnode);
					}else{
						node.addChild(temp.peek());
					}
					temp.pop();
				}
				tree.push(node);
			}
		}
        
        SRLTreeNode treeStruct = tree.pop();
        return treeStruct;
	}
	
	/**
	 * 为语义角色标注树加上语义信息
	 * @param tree 语义角色信息暂时为空的树
	 * @param argumenttree 论元树
	 * @param labelinfo 论元标记
	 * @return
	 */
	private static SRLTreeNode treeAddSemanticRole(SRLTreeNode tree, TreeNodeWrapper<HeadTreeNode>[] argumenttree, String[] labelinfo){
		for (int i = 0; i < labelinfo.length; i++) {
			if(labelinfo[i].contains("NULL")){
				
			}else{
				treeAddSingleSemanticRole(tree, argumenttree[i].getTree(), labelinfo[i]);
			}
		}
		
		return tree;
	}
	
	/**
	 * 为语义角色树中的一个节点加上语义信息
	 * @param tree 语义角色信息暂时为空的树
	 * @param headtree 其中一颗论元为根节点的子树
	 * @param role 角色标注信息
	 */
	private static void treeAddSingleSemanticRole(SRLTreeNode tree, HeadTreeNode headtree, String role){
		if(tree.toString().equals(headtree.toStringWordIndex())){
			tree.setSemanticRole(role);
		}else{
			for (TreeNode treenode : tree.getChildren()) {
				treeAddSingleSemanticRole((SRLTreeNode)treenode, headtree, role);
			}
		}
	}

	
	/**
	 * 将树转换成语义角色树
	 * @param tree 要转换的树
	 * @param semanticRole 语义角色信息
	 * @return
	 */
	public static SRLTreeNode treeToSRLTree(TreeNode tree, TreeNodeWrapper<HeadTreeNode>[] argumenttree, String[] labelinfo){		
		return treeAddSemanticRole(transferToSRLTreeNodeStructure(tree), argumenttree, labelinfo);
	}
}
