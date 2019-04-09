package org.nlp4han.coref;

import java.util.List;

import org.nlp4han.coref.sieve.Document;

import com.lc.nlp4han.constituent.TreeNode;

/**
 * 代词消解接口
 * 
 * @author 杨智超
 *
 */
public interface AnaphoraResolution
{
	/**
	 * 代词消解
	 * @param sentences 需要进行消解的句子
	 * @return 返回代词消解的结果，从代词到先行词
	 */
	public List<AnaphoraResult> resolve(List<TreeNode> sentences);
	
	/**
	 * 代词消解
	 * @param sentences 需要进行消解的句子
	 * @param pronoun 需要消解的代词结点
	 * @return 先行词结点
	 */
	public AnaphoraResult resolve(List<TreeNode> sentences, TreeNode pronoun);
	
	public List<AnaphoraResult> resolve(Document doc);
}
