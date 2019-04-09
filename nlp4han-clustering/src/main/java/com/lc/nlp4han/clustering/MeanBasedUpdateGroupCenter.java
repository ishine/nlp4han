package com.lc.nlp4han.clustering;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 基于均值的更新簇中心
 * @author 杨智超
 *
 */
public class MeanBasedUpdateGroupCenter implements UpdateGroupCenter
{

	@Override
	public boolean updateCenter(List<Group> grps)
	{
		boolean result = false;
		
		for (int i=0 ; i<grps.size() ; i++)
		{
			Sample newSample = new Sample();
			
			Group tempGroup = grps.get(i);
			List<Text> members = tempGroup.getMembers();
			
			int n = members.size();
			
			for (int j=0 ; j<n ; j++)
			{
				Text tempText = members.get(j);
				Set<Feature> tempFeatures = tempText.getSample().getFeatures();
				
				Iterator<Feature> tempIter = tempFeatures.iterator();
				while (tempIter.hasNext())
				{
					Feature tempFeature = tempIter.next();
					
					String key = tempFeature.getKey();
					
					if (newSample.containsKey(key))
					{
						Feature f = newSample.getFeature(key);
						double newValue = f.getValue() + tempFeature.getValue();
						
						f.setValue(newValue);
					}
					else
					{
						Feature f = new Feature(key, tempFeature.getValue());
						newSample.add(f);
					}
				}
			}
			
			Set<Feature> tempFeatures = newSample.getFeatures();
			
			Iterator<Feature> tempIter = tempFeatures.iterator();
			while (tempIter.hasNext())
			{
				Feature tempFeature = tempIter.next();
				
				tempFeature.setValue(tempFeature.getValue() / n);
			}
			
			if (!newSample.equals(tempGroup.getCenter()))
			{
				tempGroup.setCenter(newSample);
				result = true;
			}
		}
		return result;
	}

}
