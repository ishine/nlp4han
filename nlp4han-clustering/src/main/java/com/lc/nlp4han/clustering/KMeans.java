package com.lc.nlp4han.clustering;

import java.util.List;

// KMeans扁平聚类
public class KMeans
{
	private static final int TIMES = 5000;

	public static List<Group> run(List<Text> texts, int k)
	{
		if (texts == null || k < 1 || texts.size() < k)
			throw new RuntimeException("参数错误！");

		FeatureGenerator fg = new WordBasedZeroOneFeatureGenerator();
		if (!fg.isInitialized())
			fg.init(texts);

		DistanceCalculator distCalc = new DistanceCalculatorJaccard();

		for (int i = 0; i < texts.size(); i++)
		{
			Text t = texts.get(i);
			Sample s = new Sample(fg.getFeatures(t));
			t.setSample(s);
		}

		DistanceRecode dr = new DistanceRecode(texts, distCalc);

		UpdateGroupCenter ugc = new PAMUpateGroupCenter(dr);

		Initialization init = new RandomInitialization();
		List<Group> groups = init.initialize(texts, k);

		int iterationTimes = 0;
		for (; iterationTimes < TIMES; iterationTimes++)
		{
			for (int j = 0; j < k; j++)
			{
				groups.get(j).clear();
			}

			for (int j = 0; j < texts.size(); j++)
			{
				int index = minDistanceGroup(texts.get(j), groups, distCalc);
				groups.get(index).addMember(texts.get(j));
			}

			if (!ugc.updateCenter(groups))
				break;

		}

		if (iterationTimes > TIMES)
			System.out.println("共迭代：" + (iterationTimes - 1) + "次");
		else
			System.out.println("共迭代：" + iterationTimes + "次");
		return groups;
	}

	// 和文本最近的簇
	private static int minDistanceGroup(Text t, List<Group> groups, DistanceCalculator d)
	{
		double min = Double.POSITIVE_INFINITY;
		int index = -1;
		for (int i = 0; i < groups.size(); i++)
		{
			double distance = d.getDistance(t, groups.get(i));
			
			if (distance-min<0.00001 && distance-min>-0.00001)
			{
				if (groups.get(i).size() < groups.get(index).size())
				{
					index = i;
				}
			}
			else if (distance < min)
			{
				min = distance;
				index = i;
			}
		}
		return index;
	}

}