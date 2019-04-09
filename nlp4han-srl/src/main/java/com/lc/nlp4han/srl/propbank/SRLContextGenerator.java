package com.lc.nlp4han.srl.propbank;

import java.util.ArrayList;
import java.util.List;

import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.ml.util.BeamSearchContextGenerator;

/**
 * 生成上下文特征类
 * @author 王馨苇
 *
 */
public abstract class SRLContextGenerator implements BeamSearchContextGenerator<TreeNodeWrapper<HeadTreeNode>>{
	
	protected List<String> content = new ArrayList<>();
	protected List<String> contentpos = new ArrayList<>();
	
	/**
	 * 为测试语料生成上下文特征
	 * @param i 当前位置
	 * @param argumenttree 以论元为根的树数组
	 * @param predicatetree 以谓词为根的树
	 * @param labelinfo 标记信息
	 * @return
	 */
	public abstract String[] getContext(int i, TreeNodeWrapper<HeadTreeNode>[] argumenttree, String[] labelinfo, Object[] predicatetree);
	
	/**
	 * 获得词语内容和词性标记
	 * @param headtree 
	 */
	protected void getContentAndContentPos(HeadTreeNode headtree){
		if(headtree.getChildrenNum() == 1 && headtree.getFirstChild().getChildrenNum() == 0){
			content.add(headtree.getFirstChildName());
			contentpos.add(headtree.getNodeName());
		}else{
			for (HeadTreeNode node : headtree.getChildren()) {
				getContentAndContentPos(node);
			}
		}
	}
	
	/**
	 * 获取categorization
	 * @param headTree
	 * @return
	 */
	protected String getSubcategorization(HeadTreeNode headTree){
		int index = headTree.getIndex();
		String str = "";
		
		if(headTree.getParent() != null){
			headTree = headTree.getParent();
			str += headTree.getNodeName()+"→";
			for (int i = index; i < headTree.getChildrenNum(); i++) {
				if(i == headTree.getChildrenNum() - 1){
					str += headTree.getChildName(i);
				}else{
					str += headTree.getChildName(i) + " ";
				}				
			}
		}
		
		return str;
	}
	
	/**
	 * 获取论元的第一个词
	 * @param headTree 以当前论元为根节点的树
	 * @return
	 */
	protected String getFirstArgument(HeadTreeNode headTree){
		while(headTree.getChildrenNum() != 0){
			headTree = headTree.getFirstChild();
		}
		return headTree.getNodeName()+"_"+headTree.getParent().getNodeName();
	}
	
	/**
	 * 获取论元的最后一个词
	 * @param headTree 以当前论元为根节点的树
	 * @return
	 */
	protected String getLastArgument(HeadTreeNode headTree){
		while(headTree.getChildrenNum() != 0){
			headTree = headTree.getLastChild();
		}
		return headTree.getNodeName() + "_" + headTree.getParent().getNodeName();
	}
	
	/**
	 * 获得路径  
	 * @param predicatetree 以谓词为根节点的树 
	 * @param argumenttree 以论元为根节点的树
	 * @return
	 */
	protected String getPath(HeadTreeNode predicatetree, HeadTreeNode argumenttree){
		String argumentpath = "";
		String predicatepath = "";
		String path = "";
		HeadTreeNode initargument = argumenttree;
		HeadTreeNode initpredicate = predicatetree;
		//找到共同的根节点
		while(!argumenttree.toString().equals(predicatetree.toString())){
			HeadTreeNode tree = predicatetree;
			while(!argumenttree.toString().equals(predicatetree.toString())){
				if(predicatetree.getParent() != null){
					predicatetree = predicatetree.getParent();
				}else{
					break;
				}
			}
			if(argumenttree.getParent() != null && !argumenttree.toString().equals(predicatetree.toString())){
				argumenttree = argumenttree.getParent();
				predicatetree = tree;
			}else{
				break;
			}
		}
		
		while(!argumenttree.toString().equals(initargument.toString())){
			argumentpath += initargument.getNodeName()+"↑";
			initargument = initargument.getParent();
		}
		
		while(!predicatetree.toString().equals(initpredicate.toString())){
			String temppath = "↓" + initpredicate.getNodeName();
			temppath += predicatepath;	
			predicatepath = temppath;
			initpredicate = initpredicate.getParent();
		}
		
		path += argumentpath + predicatetree.getNodeName() + predicatepath;
		return path;
	}

	/**
	 * 获取路径的长度
	 * @param path 路径
	 * @return
	 */
	protected int getPathLength(String path){
		int count = 1;
		char[] c = path.toCharArray();
		
		for (int i = 0; i < c.length; i++) {
			if(c[i] == '↓' || c[i] == '↑'){
				count++;
			}
		}
		
		return count;
	}
	
	/**
	 * 获取路径的一半，从论元到论元和谓词交汇的地方
	 * @param path 路径
	 * @return
	 */
	protected String getPartialPath(String path){
		String partialpath = "";
		char[] c = path.toCharArray();
		
		for (int i = 0; i < c.length; i++) {
			if(c[i] == '↓'){
				break;
			}else{
				partialpath += c[i];
			}
		}
		
		return partialpath;
	}
}
