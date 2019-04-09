package org.nlp4han.coref.centering;

import java.util.ArrayList;
import java.util.List;

import org.nlp4han.coref.AnaphoraResult;
import org.nlp4han.coref.hobbs.AbstractEvaluation;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.TreeNode;

public class EvaluationBFP extends AbstractEvaluation
{
	private CenteringBFP bfp = new CenteringBFP();
	public static int total = 0; // 所有样本的数量,公有属性，子类可继承使用
	public static int correctNumber = 0; // 符合预期的数量,公有属性，子类可继承使用

	public static void main(String[] args)
	{
		AbstractEvaluation eBFP = new EvaluationBFP();
		eBFP.parse(args);
		System.out.println("成功：" + correctNumber + "/" + total);
	}

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

		List<AnaphoraResult> result = bfp.resolve(constituentTrees);
		
		List<String> resultStr = toStringFormat(result, constituentTrees);

		total++;
		if (compare(resultStr, information2, total))
			correctNumber++;
	}

	/**
	 * 比较指代消解算法的输出与期望结果，若两者相同，返回true；否则，返回false，并在控制台上输出不符的结果
	 * 
	 * @param results
	 *            指代消解结果
	 * @param expection
	 *            期望结果
	 * @param t
	 *            当前序号
	 * @return 若两者相同，返回true；否则，返回false，并在控制台上输出不符的结果
	 */
	public static boolean compare(List<String> results, List<String> expection, int t)
	{
		boolean meetExpectation = true;
		List<String> errors = new ArrayList<String>(); // 错误的指代关系
		List<String> lacks = new ArrayList<String>(); // 缺少的指代关系
		int[] tags = new int[expection.size()];
		for (int i = 0; i < results.size(); i++) // 用于标记resultSet中被找到的结果
		{
			int index;
			if ((index = expection.indexOf(results.get(i))) == -1)
			{
				meetExpectation = false;
				errors.add(results.get(i));

			}
			else
				tags[index] = 1;
		}

		for (int tag : tags)
		{
			if (tag != 1)
			{
				meetExpectation = false;
				lacks.add(expection.get(tag));
			}
		}

		if (meetExpectation)
			return true;
		else
		{// 打印错误信息
			System.out.println("第" + t + "个未通过：");
			if (!errors.isEmpty())
			{
				System.out.println("\t错误的指代：");
				for (int i = 0; i < errors.size(); i++)
				{
					System.out.println("\t\t" + errors.get(i));
				}
			}
			if (!lacks.isEmpty())
			{
				System.out.println("\t缺少的指代：");
				for (int i = 0; i < lacks.size(); i++)
				{
					System.out.println("\t\t" + lacks.get(i));
				}
			}
			return false;
		}
	}
}
