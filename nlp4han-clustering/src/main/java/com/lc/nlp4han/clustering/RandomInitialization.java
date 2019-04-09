package com.lc.nlp4han.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomInitialization implements Initialization
{

	@Override
	public List<Group> initialize(List<Text> texts, int k)
	{
		List<Group> groups = new ArrayList<Group>();
		Random random = new Random();
		List<Integer> randomValues = new ArrayList<Integer>();
		
		for (int i=0 ; i<k ; i++)
		{
			int r = random.nextInt(texts.size());
			if (randomValues.contains(r) || r<0)
			{
				i--;
				continue;
			}
			randomValues.add(r);
		}
		
		for (int i=0 ; i<k ; i++)
		{
			Group g = new Group();
			g.setCenter(texts.get(randomValues.get(i)).getSample().clone());
			groups.add(g);
		}
		
		return groups;
	}

}
