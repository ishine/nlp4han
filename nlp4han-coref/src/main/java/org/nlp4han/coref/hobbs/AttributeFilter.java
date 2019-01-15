package org.nlp4han.coref.hobbs;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.nlp4han.coref.hobbs.Attribute.Animacy;
import org.nlp4han.coref.hobbs.Attribute.Gender;
import org.nlp4han.coref.hobbs.Attribute.Number;

import com.lc.nlp4han.constituent.TreeNode;

/**
 * 属性过滤，用于适配单复数、性别、动物性、人称等属性
 * 
 * @author 杨智超
 *
 */
public class AttributeFilter extends FilterWrapper
{
	private TreeNode referenceNode;
	private Attribute referenceNodeAttribute;
	private AttributeGenerator attributeGenerator;

	public AttributeFilter(CandidateFilter filter)
	{
		this.filter = filter;
	}

	public AttributeFilter(FilterWrapper filter, AttributeGenerator attributeGenerator)
	{
		this(filter);
		this.attributeGenerator = attributeGenerator;
	}

	/**
	 * 设置参考结点和属性生成器
	 * 
	 * @param referenceNode
	 *            参考结点
	 * @param attributeGenerator
	 *            属性生成器
	 */
	public void setUp(TreeNode referenceNode, AttributeGenerator attributeGenerator)
	{
		this.referenceNode = referenceNode;
		this.attributeGenerator = attributeGenerator;
		if (this.attributeGenerator != null)
			this.referenceNodeAttribute = this.attributeGenerator.extractAttributes(referenceNode);
	}


	public void setAttributeGenerator(AttributeGenerator attributeGenerator)
	{
		this.attributeGenerator = attributeGenerator;
	}

	/**
	 * 比较两个mention的属性是否相匹配
	 * 
	 * @param attribute1
	 *            属性1
	 * @param attribute2
	 *            属性2
	 * @return 若两属性不排斥，则返回true；否则，返回false
	 */
	public boolean isMatched(Attribute attribute1, Attribute attribute2)
	{
		if ((!GenderCompatibility(attribute1, attribute2)))
			return false;
		if (!NumberCompatibility(attribute1, attribute2))
			return false;
		if (!AnimacyCompatibility(attribute1, attribute2))
			return false;
		return true;
	}

	private boolean AnimacyCompatibility(Attribute attribute1, Attribute attribute2)
	{
		Set<Animacy> a1 = attribute1.getAnimacy();
		Set<Animacy> a2 = attribute2.getAnimacy();
		return compare(a1, a2);
	}

	private boolean NumberCompatibility(Attribute attribute1, Attribute attribute2)
	{
		Set<Number> n1 = attribute1.getNumber();
		Set<Number> n2 = attribute2.getNumber();
		return compare(n1, n2);
	}

	private boolean GenderCompatibility(Attribute attribute1, Attribute attribute2)
	{
		Set<Gender> g1 = attribute1.getGender();
		Set<Gender> g2 = attribute2.getGender();
		return compare(g1, g2);
	}

	private <T> boolean compare(Set<T> s1, Set<T> s2)
	{
		if (s1.size() > 0 && s2.size() > 0)
		{
			Iterator<T> it1 = s1.iterator();
			while (it1.hasNext())
			{
				if (s2.contains(it1.next()))
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	public List<TreeNode> filter(List<TreeNode> nodes)
	{
		List<TreeNode> treeNodes = filter.filter(nodes);
		if (this.referenceNode == null)
			throw new RuntimeException("未设置基准结点");
		if (this.attributeGenerator != null)
		{
			if (this.referenceNodeAttribute == null)
			{
				referenceNodeAttribute = this.attributeGenerator.extractAttributes(referenceNode);
			}
			for (int i = 0; i < treeNodes.size(); i++)
			{
				if (!isMatched(this.referenceNodeAttribute,
						this.attributeGenerator.extractAttributes(treeNodes.get(i))))
				{
					treeNodes.remove(i);
					i--;
				}
			}
		}
		return treeNodes;
	}

	@Override
	public void setReferenceConditions(Object obj)
	{
		TreeNode node = (TreeNode) obj;
		this.referenceNode = node;
		if (this.attributeGenerator != null)
			this.referenceNodeAttribute = this.attributeGenerator.extractAttributes(referenceNode);
		
	}

}
