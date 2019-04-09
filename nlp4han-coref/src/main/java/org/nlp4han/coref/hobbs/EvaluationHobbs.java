package org.nlp4han.coref.hobbs;

import java.util.ArrayList;
import java.util.List;

import org.nlp4han.coref.AnaphoraResult;
import org.nlp4han.coref.centering.CenteringBFP;
import org.nlp4han.coref.centering.EvaluationBFP;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.TreeNode;

/**
 * 评价类
 * 
 * @author 杨智超
 *
 */
public class EvaluationHobbs extends AbstractEvaluation
{
	public static int total = 0; // 所有样本的数量,公有属性，子类可继承使用
	public static int correctNumber = 0; // 符合预期的数量,公有属性，子类可继承使用
	private Hobbs hobbs = new Hobbs();
	
	public static void main(String[] args)
	{
		EvaluationHobbs e = new EvaluationHobbs();
		e.parse(args);
		System.out.println("成功：" + correctNumber + "/" + total);
	}

	@Override
	public void processAnEvaluationSample(List<String> information1, List<String> information2,
			List<String> information3)
	{
		List<TreeNode> constituentTrees = new ArrayList<TreeNode>();

		for (int i = 0; i < information1.size(); i++)
		{
			String str = information1.get(i);
			TreeNode ui = BracketExpUtil.generateTreeNoTopBracket("(" + str + ")");
			constituentTrees.add(ui);
		}
		

		List<AnaphoraResult> results = hobbs.resolve(constituentTrees);
		List<String> resultStr = toStringFormat(results, constituentTrees);

		total++;
		if (EvaluationBFP.compare(resultStr, information2, total))
			correctNumber++;

	}

	@Override
	public boolean extractAnEvaluationSample(String oneLine, List<String> information1, List<String> information2,
			List<String> information3)
	{
		if (!oneLine.startsWith("#"))
		{
			if (oneLine.startsWith("("))
			{// 括号表达式
				information1.add(oneLine);
			}
			else if (oneLine.matches(".+" + CenteringBFP.SEPARATOR + ".+"))
			{// 指代结果
				information2.add(oneLine);
			}
			return false;
		}
		else
			return true;
	}
	
}
