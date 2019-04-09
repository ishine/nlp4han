package com.lc.nlp4han.srl.propbank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.lc.nlp4han.constituent.AbstractHeadGenerator;
import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreeToHeadTree;

/**
 * 一步完成识别和分类的解析策略类
 * @author 王馨苇
 *
 */
public abstract class AbstractParseStrategy<T extends TreeNode> {

	protected boolean containPredicateFlag;
	protected String predicate;
	
	/**
	 * 是否有剪枝操作
	 * @return
	 */
	protected abstract boolean hasPrePruning();
	
	/**
	 * 是否有头结点
	 * @return
	 */
	protected abstract boolean hasHeadWord();
	
	/**
	 * 根据规则进行剪枝操作[剪掉标点符号和动词及动词父节点是并列结构的节点]
	 * @param tree 要剪枝的树[由谓词为根表示的树]
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected T prePruning(T tree){
		int count = 0;
		List<Integer> list = new ArrayList<>();
		
		while(tree.getParent() != null){
			count++;
			list.add(tree.getIndex());
			tree = (T) tree.getParent();
			tree.setFlag(false);
			if(IsCordinateStructureUtil.isCordinate(tree)){
				for (int i = 0; i < tree.getChildrenNum(); i++) {
					tree.getChild(i).setFlag(false);
				}
			}
		}
		
		for (int i = 0; i < count; i++) {
			tree = (T) tree.getChild(list.get(list.size()-1-i));
		}
		
		return tree;
	}
	
	/**
	 * 得到以谓词为根节点的树
	 * @param tree 正常的一棵树
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<T> getPredicateTree(T tree, HashMap<Integer,SemanticRoleStructure> map, int verbindex){
		List<T> list = new ArrayList<>();
		if(tree.getChildrenNum() == 0){
			if(map.containsKey(tree.getWordIndex())){
				if(map.get(tree.getWordIndex()).getRole().equals("rel")){
					int begin = tree.getWordIndex();
					int up = map.get(begin).getUp();
					for (int i = 0; i <= up; i++) {
						tree = (T) tree.getParent();
					}
					if(verbindex == begin){
						list.add(tree);
					}
					for (int i = 0; i <= up; i++) {
						tree = (T) tree.getFirstChild();
					}
				}			
			}
		}else{
			for (TreeNode treenode : tree.getChildren()) {
				list.addAll(getPredicateTree((T)treenode, map, verbindex));
			}
		}
		return list;
	}
	
	/**
	 * 得到谓词或者谓词短语
	 * @param tree 正常的一棵树
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void getPredicate(T tree, HashMap<Integer,SemanticRoleStructure> map, int verbindex){
		if(tree.getChildrenNum() == 0){
			if(map.containsKey(tree.getWordIndex())){
				if(map.get(tree.getWordIndex()).getRole().equals("rel")){
					predicate += tree.getNodeName();
				}			
			}
		}else{
			for (TreeNode treenode : tree.getChildren()) {
				getPredicate((T)treenode, map, verbindex);
			}
		}
	}
	
	/**
	 * 去除标点符号，将其flag设置为false
	 * @param tree 要处理的树
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private T removePunctuation(T tree){
		if(tree.getChildrenNum() == 0){
			if(IsPunctuationUtil.isPunctuation(tree.getParent().getNodeName())){
				tree.getParent().setFlag(false);
				tree.setFlag(false);
			}
		}else{
			for (TreeNode treenode : tree.getChildren()) {			
				removePunctuation((T) treenode);
			}
		}
		return tree;
	}
	
	/**
	 * 将谓词的子节点设置为false
	 * @param tree 树
	 * @param map 语义角色的map
	 * @param verbindex 动词的标记
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private T removePredicateSon(T tree, HashMap<Integer,SemanticRoleStructure> map, int verbindex){
		if(tree.getChildrenNum() == 0){
			if(map.containsKey(tree.getWordIndex())){
				if(map.get(tree.getWordIndex()).getRole().equals("rel")){
					int begin = tree.getWordIndex();
					int up = map.get(tree.getWordIndex()).getUp();
					for (int i = 0; i <= up; i++) {
						tree = (T) tree.getParent();
						tree.setFlag(false);
					}
					if(verbindex == begin){
						tree.setFlag(true);
					}
					for (int i = 0; i <= up; i++) {
						tree = (T) tree.getFirstChild();
					}
				}			
			}
		}else{
			for (TreeNode treenode : tree.getChildren()) {
				removePredicateSon((T)treenode, map, verbindex);
			}
		}
		return tree;
	}
	
	/**
	 * 将树转成训练要用的样本样式
	 * @param tree 树
	 * @param semanticRole 语义信息
	 * @return
	 */
	protected abstract SRLSample<T> toSample(T tree, String semanticRole);
	
	/**
	 * 根据是否要进行剪枝、是否加入头结点，解析样本
	 * @param tree 树
	 * @param semanticRole 语义信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SRLSample<T> parse(TreeNode tree, String semanticRole, AbstractHeadGenerator ahg){
		
		SRLSample<T> sample;
		String[] roles = semanticRole.split(" ");
		int verbindex = Integer.parseInt(roles[2]);
		predicate = "";//清空
		getPredicate((T) tree, getRoleMap(semanticRole), verbindex);
		if(hasHeadWord()){
			HeadTreeNode headtree = TreeToHeadTree.treeToHeadTree(tree, ahg);
			if(hasPrePruning()){
				sample = toSample(prePruning(getPredicateTree(removePredicateSon(removePunctuation((T) headtree), getRoleMap(semanticRole), verbindex), getRoleMap(semanticRole), verbindex).get(0)), semanticRole);
				sample.setPruning(true);
				return sample;
			}else{
				sample = toSample(removePredicateSon(removePunctuation((T) headtree), getRoleMap(semanticRole), verbindex), semanticRole);
				sample.setPruning(false);
				return sample;
			}
		}else{
			if(hasPrePruning()){
				sample = toSample(prePruning(getPredicateTree(removePredicateSon(removePunctuation((T) tree), getRoleMap(semanticRole), verbindex), getRoleMap(semanticRole), verbindex).get(0)), semanticRole);
				sample.setPruning(true);
				return sample;
			}else{
				sample = toSample(removePredicateSon(removePunctuation((T) tree), getRoleMap(semanticRole), verbindex), semanticRole);
				sample.setPruning(false);
				return sample;
			}
		}				
	}
	
	/**
	 * 得到语义信息的hash表，键是终结点的标记，值为上溯的步数以及标记
	 * @param semanticRole 语义角色标注信息
	 * @return
	 */
	protected HashMap<Integer,SemanticRoleStructure> getRoleMap(String semanticRole){
		HashMap<Integer,SemanticRoleStructure> map = new HashMap<>();
		String[] roles = semanticRole.split(" ");
		//对谓词的处理
		for (int i = 6; i < roles.length; i++) {
			//拆开为argument下标和语义标记部分
			String[] digitandrole = roles[i].split("-");
			//处理语义角色部分
			String role = getRole(digitandrole);
			//加入以论元作为根节点的树
			if(role.equals("rel")){
				String[] digits = digitandrole[0].split("\\*");
				//处理,隔开的部分
				String[] comma = digits[0].split(",");
				for (int j = 0; j < comma.length; j++) {
					String[] digit = comma[j].split(":");
					int begin = Integer.parseInt(digit[0]);
					int up = Integer.parseInt(digit[1]);					
					map.put(begin, new SemanticRoleStructure(-1, up, role));				
				}
				for (int j = 1; j < digits.length; j++) {
					String[] digit = digits[j].split(":");
					int begin = Integer.parseInt(digit[0]);
					int up = Integer.parseInt(digit[1]);
					map.put(begin, new SemanticRoleStructure(-1, up, role));
				}
			}
		}
		//处理论元的部分
		for (int i = 6; i < roles.length; i++) {
			//拆开为argument下标和语义标记部分
			String[] digitandrole = roles[i].split("-");
			//处理语义角色部分
			String role = getRole(digitandrole);
			//加入以论元作为根节点的树
			if(!role.equals("rel")){
				String[] digits = digitandrole[0].split("\\*");
				//处理,隔开的部分
				String[] comma = digits[0].split(",");
				for (int j = 0; j < comma.length; j++) {
					String[] digit = comma[j].split(":");
					int begin = Integer.parseInt(digit[0]);
					int up = Integer.parseInt(digit[1]);
					map.put(begin, new SemanticRoleStructure(-1, up, role));
				}
				for (int j = 1; j < digits.length; j++) {
					String[] digit = digits[j].split(":");
					int begin = Integer.parseInt(digit[0]);
					int up = Integer.parseInt(digit[1]);
					map.put(begin, new SemanticRoleStructure(-1, up, role));
				}
			}
		}
		return map;
	}
	
	
	/**
	 * 得到角色标注的标记信息，这里的标记包括功能标记
	 * @param digitandrole 位置和标记数组
	 * @return
	 */
	private String getRole(String[] digitandrole){
		String role = digitandrole[1];		
		for (int j = 2; j < digitandrole.length; j++) {
			if(IsFunctionLabelUtil.isFunction(digitandrole[j])){
				role += "-"+digitandrole[j];
			}				
		}
		return role;
	}
	

	/**
	 * 获取当前树最左端终结点的下标和记录得到下标走过的步数
	 * @param tree 树
	 * @return
	 */
	protected int[] getLeftIndexAndDownSteps(HeadTreeNode tree){
		int step = -1;
		int[] leftanddown = new int[2];
		while(tree.getChildrenNum() != 0){
			tree = tree.getFirstChild();
			step++;
		}
		leftanddown[0] = tree.getWordIndex();
		leftanddown[1] = step;
		return leftanddown;
	}
	
	/**
	 * 判断一棵树的子节点中是否包含谓词
	 * @param tree 要判断的树
	 * @param verbindex 动词的下标
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected boolean containsPredicate(T tree, int verbindex){
		if(tree.getWordIndex() == verbindex){			
			containPredicateFlag = true;
		}else{
			for (TreeNode treenode : tree.getChildren()) {			
				containsPredicate((T)treenode, verbindex);
			}
		}	
		return containPredicateFlag;
	}
}
