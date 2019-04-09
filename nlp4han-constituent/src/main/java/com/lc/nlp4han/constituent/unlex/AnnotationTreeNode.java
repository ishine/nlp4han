package com.lc.nlp4han.constituent.unlex;

import java.util.ArrayList;
import java.util.List;

import com.lc.nlp4han.constituent.TreeNode;

/**
 * 带标记的成分树结点
 * 
 * @author 王宁 
 */
public class AnnotationTreeNode extends TreeNode
{
	private Annotation annotation;

	private AnnotationTreeNode()
	{
	}

	private AnnotationTreeNode(Annotation label)
	{
		this.annotation = label;
	}

	public static AnnotationTreeNode getInstance()
	{
		return new AnnotationTreeNode();
	}

	public static AnnotationTreeNode getInstance(TreeNode tree, NonterminalTable nonterminalTable)
	{
		if (tree == null)
		{
			return null;
		}

		if (tree instanceof AnnotationTreeNode)
		{
			return (AnnotationTreeNode) tree;
		}
			
		if (tree.getNodeName() == null)
		{
			return null;
		}

		if (tree.isLeaf())
		{
			return new AnnotationTreeNode(new Annotation(tree.getNodeName()));
		}
		
		short intValueOfLabel;
		if (nonterminalTable.hasSymbol(tree.getNodeName()))
		{
			intValueOfLabel = nonterminalTable.intValue(tree.getNodeName());
		}
		else
		{
			intValueOfLabel = nonterminalTable.putSymbol(tree.getNodeName());
		}

		if (tree.getChild(0).isLeaf() && tree.getChildren().size() == 1
				&& !nonterminalTable.getIntValueOfPreterminalArr().contains(intValueOfLabel))
		{
			nonterminalTable.addToPreterminalArr(intValueOfLabel);
		}

		AnnotationTreeNode annotationTree = new AnnotationTreeNode(new Annotation(intValueOfLabel, (short) 1));
		annotationTree.setNewName(null);
		
		ArrayList<AnnotationTreeNode> tempChildren = new ArrayList<AnnotationTreeNode>(tree.getChildren().size());
		for (TreeNode child : tree.getChildren())
		{
			AnnotationTreeNode newChild = getInstance(child, nonterminalTable);
			
			newChild.setParent(annotationTree);
			tempChildren.add(newChild);
		}
		
		for (int i = 0; i < tempChildren.size(); i++)
			annotationTree.addChild(tempChildren.get(i));
		
		return annotationTree.setSpan();
	}

	public boolean isPreterminal()
	{
		if (children.size() == 1 && children.get(0).isLeaf())
			return true;
		else
			return false;
	}

	// 清空树的内外概率
	public AnnotationTreeNode forgetIOScoreAndScale()
	{
		if (this.isLeaf() || this == null)
			return this;
		
		annotation.setInnerScores(null);
		annotation.setOuterScores(null);
		annotation.setInnerScale(0);
		annotation.setOuterScale(0);
		
		for (AnnotationTreeNode child : this.getChildren())
		{
			child.forgetIOScoreAndScale();
		}
		
		return this;
	}

	private AnnotationTreeNode setSpan()
	{
		setSpanFTHelper(0);
		
		return this;
	}

	private int setSpanFTHelper(int count)
	{
		if (this.isPreterminal())
		{

			this.annotation.setSpanFrom((short) count);
			this.annotation.setSpanTo((short) (count + 1));
			return count + 1;
		}

		for (AnnotationTreeNode child : getChildren())
		{
			count = child.setSpanFTHelper(count);
		}
		
		this.annotation.setSpanFrom(this.getFirstChild().getAnnotation().getSpanFrom());
		this.annotation.setSpanTo(this.getLastChild().getAnnotation().getSpanTo());
		
		return count;
	}

	@Override
	public AnnotationTreeNode getFirstChild()
	{
		return (AnnotationTreeNode) super.getFirstChild();
	}

	@Override
	public AnnotationTreeNode getLastChild()
	{
		return (AnnotationTreeNode) super.getLastChild();
	}

	@Override
	public AnnotationTreeNode getChild(int i)
	{
		return (AnnotationTreeNode) super.getChild(i);
	}

	@Override
	public String getChildName(int i)
	{
		return ((AnnotationTreeNode) children.get(i)).getNodeName();
	}

	@Override
	public AnnotationTreeNode getParent()
	{
		return (AnnotationTreeNode) super.getParent();
	}

	public String getNodeName(NonterminalTable nonterminalTable)
	{
		if (isLeaf())
			return annotation.getWord();
		else
			return nonterminalTable.stringValue(annotation.getSymbol());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AnnotationTreeNode> getChildren()
	{
		return (List<AnnotationTreeNode>) super.getChildren();
	}

	public Annotation getAnnotation()
	{
		return annotation;
	}

	public void setAnnotation(Annotation label)
	{
		this.annotation = label;
	}
}
