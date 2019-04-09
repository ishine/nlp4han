package com.lc.nlp4han.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Evaluation
{
	private Map<String, Integer> categoryInfo = new HashMap<String, Integer>();  // 类别标记，如文件名以"c1-xxx"，"c2-xxx"等命名，则"c1"，"c2"为类别标记
	private List<Set<Text>> expectedResult = new ArrayList<Set<Text>>();  // 期望的聚类结果。根据不同的类别标记，对所有文本进行分类的结果，作为评价的标准
	private int textNumber;  // 文本数量
	private List<Group> actualResult = null;  // 实际的聚类结果。
	
	private List<int[]> sameClassTextNumberList = new ArrayList<int[]>();  // 同类文本数。记录聚类结果actualResult的每个簇中各类的文档数，簇的顺序与actualResult对应，类别的顺序与categoryInfo对应
	
	private int TP = 0;
	private int FP = 0;
	private int TN = 0;
	private int FN = 0; 
	
	public Evaluation()
	{
		
	}
	
	public Evaluation(List<Text> texts)
	{
		init(texts);
	}
	
	public Evaluation(List<Text> texts, List<Group> actualResult)
	{
		init(texts);
		setActualResult(actualResult);
	}

	/**
	 * 通过所有Text，生成期望的聚类结果和所有类别信息
	 * @param texts 所有文本，text
	 */
	public void init(List<Text> texts)
	{
		for (Text t : texts)
		{
			String s = t.getName();
			String category = s.split("-")[0];
			if (category==null || category.length()<1)
			{
				System.out.println("\""+t.getName()+"\""+"无法归类！");
				continue;
			}
			if (categoryInfo.containsKey(category))
			{
				int index = categoryInfo.get(category);
				expectedResult.get(index).add(t);
			}
			else
			{
				categoryInfo.put(category, expectedResult.size());
				
				Set<Text> tmp = new HashSet<Text>();
				tmp.add(t);
				expectedResult.add(tmp);
			}
		}
		
		textNumber = texts.size();
	}

	public List<Set<Text>> getExpectedResult()
	{
		return this.expectedResult;
	}
	
	/**
	 * 计算TP，FP，TN，FN
	 */
	private void calculateTP_FP_TN_FN()
	{
		int TP_FP=0;  // TP+FP
		
		for (int i=0 ; i<actualResult.size() ; i++)				//算出TP+FP
		{
			if (actualResult.get(i).size()>1)
				TP_FP += combine(actualResult.get(i).size(), 2);
		}
		
		for (int i=0 ; i<sameClassTextNumberList.size() ; i++)			//算出TP
		{
			for (int j=0 ; j<sameClassTextNumberList.get(i).length ; j++)
			{
				int num;
				if ((num = sameClassTextNumberList.get(i)[j]) > 1)
				{
					TP += combine(num, 2);
				}
			}
		}
		
		FP = TP_FP - TP;				//算出FP
		
		/*for (int i=0 ; i<analysisOfResults.size() ; i++)			//算出FP
		{
			for (int j=0 ; j<analysisOfResults.get(i).length ; j++)
			{
				for (int k=j+1 ; k<analysisOfResults.get(i).length ; k++)
				{
					FP += analysisOfResults.get(i)[j] * analysisOfResults.get(i)[k];
				}
			}
		}*/
		
		for (int i=0 ; i<sameClassTextNumberList.size() ; i++)			//算出FN
		{
			for (int j=i+1 ; j<sameClassTextNumberList.size() ; j++)
			{
				for (int k=0 ; k<sameClassTextNumberList.get(i).length ; k++)
				{
					FN += sameClassTextNumberList.get(i)[k] * sameClassTextNumberList.get(j)[k];
				}
			}
		}
		
		TN = textNumber * (textNumber-1) /2 - TP_FP - FN;		//算出TN
	}
	
	/**
	 * 排列
	 */
	private int arrange(int a, int b)	
	{
		int smallerValue;
		int biggerValue;
		int result = 1;
		if (a<0 || b<0)
		{
			return -1;
		}
		if (a<=b)
		{
			smallerValue = a;
			biggerValue = b;
		}
		else
		{
			smallerValue = b;
			biggerValue = a;
		}
		
		for (int i=0 ; i<smallerValue ; i++)
		{
			result *= (biggerValue-i);
		}
		return result;
	}
	
	/**
	 * 组合
	 */
	private int combine(int a, int b)
	{
		int smallerValue;
		int molecular;
		int denominator;
		if (a<0 || b<0)
		{
			return -1;
		}
		if (a<=b)
		{
			smallerValue = a;
		}
		else
		{
			smallerValue = b;
		}
		
		molecular = arrange(a, b);
		denominator = arrange(smallerValue, smallerValue);
		return molecular/denominator;
	}
	
	/**
	 * 生成sameClassTextNumberList中的数值
	 */
	private void initSameClassTextNumberList(List<Group> grps)
	{
		if (grps == null || grps.size()<1)
			throw new RuntimeException("参数错误！");
		
		for (Group g : grps)
		{
			sameClassTextNumberList.add(getSameClassNumber(g));
		}
	}

	/**
	 * 在一个簇中，同一类的文本数量，其类别与categoryInfo对应
	 * @param g 被统计的簇
	 * @return 各类别的文档数
	 */
	private int[] getSameClassNumber(Group g)
	{
		int[] result = new int[categoryInfo.size()];
		
		List<Text> names = g.getMembers();
		
		for (int i=0 ; i<names.size() ; i++)
		{
			for (int j=0 ; j<expectedResult.size() ; j++)
			{
				if (expectedResult.get(j).contains(names.get(i)))
				{
					result[j]++;
				}
			}
		}
		return result;
	}

	/**
	 * 计算纯度
	 * @return 纯度
	 */
	public double purity()
	{
		int sum=0;
		for (int i=0 ; i<sameClassTextNumberList.size() ; i++)
		{
			sum += maxValueInArray(sameClassTextNumberList.get(i));
		}
		return sum*1.0/textNumber;
	}

	/**
	 * 计算兰德指数（Rand Index）
	 * @return 兰德指数
	 */
	public double RI()
	{
		if (TP==0 && TN==0 && FP==0 && FN==0)
			calculateTP_FP_TN_FN();
		return (TP+TN)*1.0/(TP+FP+FN+TN);
	}

	/**
	 * 计算F值
	 * @param beta 
	 * @return
	 */
	public double FMeasure(int beta)
	{
		double P;
		double R;
		double F;
		if (TP==0 && TN==0 && FP==0 && FN==0)
			calculateTP_FP_TN_FN();
		
		P = TP*1.0/(TP+FP);
		R = TP*1.0/(TP+FN);
		F = (beta*beta + 1)*P*R / (beta*beta*P + R);
		return F;
	}
	
	/**
	 * 设置被评价的实际聚类结果
	 * @param groups
	 */
	public void setActualResult(List<Group> groups)
	{
		this.actualResult = groups;
		initSameClassTextNumberList(groups);
	}
	
	private int maxValueInArray(int[] array)
	{
		int max = -1;
		for (int i=0 ; i<array.length ; i++)
		{
			if (max < array[i])
				max = array[i];
		}
		return max;
	}
	
	/**
	 * 计算归一化互信息NMI（Normalized Mutual Information）
	 * @return
	 */
	public double NMI()
	{
		double I=0;  // 互信息
		
		int[] d = new int[categoryInfo.size()];  // 实际聚类结果中，各类别的总文本数
		
		for (int i=0 ; i<sameClassTextNumberList.size() ; i++)
		{
			int[] tmp = sameClassTextNumberList.get(i);
			for (int j=0 ; j<tmp.length ; j++)
			{
				d[j] += tmp[j];
			}
		}
		
		for (int i=0 ; i<sameClassTextNumberList.size() ; i++)
		{
			int[] tmp = sameClassTextNumberList.get(i);
			
			for (int j=0 ; j<tmp.length ; j++)
			{
				if (tmp[j]>0)
					I += 1.0*tmp[j] / textNumber * log(1.0*textNumber*tmp[j]/actualResult.get(i).size()/d[j]);
			}
		}
		
		double H_OMEGA = 0;  // 实际聚类结果的信息熵
		
		for (int i=0 ; i<actualResult.size() ; i++)
		{
			Group tmp = actualResult.get(i);
			H_OMEGA -= 1.0*tmp.size()/ textNumber*log(1.0*tmp.size()/ textNumber);
		}
		
		double H_C = 0; // 期望类别集合的信息熵
		
		for (int i=0 ; i<d.length ; i++)
		{
			H_OMEGA -= 1.0*d[i]/ textNumber*log(1.0*d[i]/ textNumber);
		}
		
		
		return I*2/(H_OMEGA+H_C);
	}
	
	private double log(double d)
	{
		return Math.log(d);
	}
}
