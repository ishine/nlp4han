package com.lc.nlp4han.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 基于PAM算法的更新簇中心
 * @author 杨智超
 *
 */
public class PAMUpateGroupCenter implements UpdateGroupCenter
{
	DistanceRecode dr = null;
	
	public PAMUpateGroupCenter()
	{
	}
	
	public PAMUpateGroupCenter(DistanceRecode dr)
	{
		this.dr = dr;
	}
	
	public void setDistanceRecode(DistanceRecode dr)
	{
		this.dr = dr;
	}
	
	@Override
	public boolean updateCenter(List<Group> grps)
	{
		boolean result = false;
		List<Text> centers = getCenters(grps);
		double oldLossVaule = getTotalLossVaule(centers);
		
		for (int i=0 ; i<centers.size() ; i++)
		{
			List<Text> ts = grps.get(i).getMembers();
			
			Text tmpText = centers.get(i);
			
			for (int j=0 ; j<ts.size() ; j++)
			{
				if (ts.get(j) != tmpText)
				{
					Text t = centers.set(i, ts.get(j));
					double tempLossVaule = getTotalLossVaule(centers);
					
					if (tempLossVaule >= oldLossVaule)  // 恢复
						centers.set(i, t);
					else
						result = true;
						
				}
			}
		}
		
		for (int i=0 ; i<centers.size() ; i++)
		{
			grps.get(i).setCenter(centers.get(i).getSample().clone());
		}
		
		return result;
	}
	
	private double getTotalLossVaule(List<Text> centers)
	{
		double lossVaule = 0.0;
		Set<Text> texts = dr.textSet();
		for (Text t : texts)
		{
			lossVaule += getMinLossValue(t, centers);
		}
		return lossVaule;
	}
	
	private double getMinLossValue(Text text, List<Text> texts)
	{
		double min = Double.POSITIVE_INFINITY;
		
		for (Text t : texts)
		{
			double tmp = dr.getDistance(text, t);
			
			if (tmp < min)
				min = tmp;
		}
		return min;
	}

	private List<Text> getCenters(List<Group> grps)
	{
		List<Text> result = new ArrayList<Text>();
		
		Set<Text> ts = dr.textSet();
		
		for (Group g : grps)
		{
			Sample tempSample = g.getCenter();
			
			for (Text t : ts)
			{
				if (tempSample.equals(t.getSample()))
				{
					result.add(t);
					break;
				}
			}
		}
		return result;
	}

}
