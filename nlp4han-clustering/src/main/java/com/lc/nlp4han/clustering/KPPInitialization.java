package com.lc.nlp4han.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * k-means++初始化
 * @author 杨智超
 *
 */
public class KPPInitialization implements Initialization
{
	DistanceRecode dr = null;
	
	public KPPInitialization(DistanceRecode dr)
	{
		this.dr = dr;
	}
	
	@Override
	public List<Group> initialize(List<Text> texts, int k)
	{
		List<Group> result = new ArrayList<Group>();
		
		Random random = new Random();
		int r = random.nextInt(texts.size());
		
		List<Text> centers = new ArrayList<Text>();
		centers.add(texts.get(r));
		
		for (int i=1 ; i<k ; i++)
		{
			List<Double> recode = new ArrayList<Double>();
			double sum = 0;
			for (int j=0 ; j<texts.size() ; j++)
			{
				
				double min = getMinLossValue(texts.get(j), centers);
				recode.add(min);
				
				sum += min;
			}
			
			double randomDouble = random.nextDouble();
			randomDouble *= sum;
			
			int index = 0;
			while (true)
			{
				randomDouble -= recode.get(index);
				
				if (randomDouble < 0)
					break;
				
				index++;
			}
			centers.add(texts.get(index));
		}
		
		for (int i=0 ; i<k ; i++)
		{
			Group g = new Group();
			g.setCenter(centers.get(i).getSample().clone());
			result.add(g);
		}
		
		for (Text t : centers)  // TODO: 打印信息，可删除
		{
			System.out.println("init: "+t.getName());
		}
		return result;
	}
	
	private double getMinLossValue(Text text, List<Text> centers)
	{
		double min = Double.POSITIVE_INFINITY;
		
		for (Text t : centers)
		{
			double tmp = dr.getDistance(text, t);
			if (tmp < min)
				min = tmp;
		}
		
		return min;
	}

}
