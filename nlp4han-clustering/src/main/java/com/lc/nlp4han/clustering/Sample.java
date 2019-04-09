package com.lc.nlp4han.clustering;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Sample
{
	private Map<String, Feature> features = new HashMap<String, Feature>();

	public Sample()
	{
		
	}
	
	public Sample(Collection<Feature> fs)
	{
		setFeatures(fs);
	}

	public Feature getFeature(String key)
	{
		Feature f = features.get(key);
		return f;
	}
	
	/**
	 * 获取所有特征
	 * @return 所有特征
	 */
	public Set<Feature> getFeatures()
	{
		Set<Feature> result = new HashSet<Feature>();
		
		Set<String> keys = features.keySet();
		for (String key : keys)
		{
			Feature f = features.get(key);
			result.add(f);
		}
		
		return result;
	}

	/**
	 * 设置特征
	 * @param fs 被指定的特征列表
	 */
	public void setFeatures(Collection<Feature> fs)
	{
		
		clear();
		add(fs);
	}
	
	/**
	 * 添加特征
	 * @param fs
	 */
	public void add(Collection<Feature> fs)
	{
		if (features == null)
			features = new HashMap<String, Feature>();
		for (Feature f : fs)
		{
			features.put(f.getKey(), f);
		}
	}
	
	/**
	 * 添加特征
	 * @param f
	 */
	public void add(Feature f)
	{
		if (features == null)
			features = new HashMap<String, Feature>();
		
		features.put(f.getKey(), f);
		
	}
	
	public void clear()
	{
		if (features != null)
			this.features.clear();
	}
	
	public double getValue(String key)
	{
		return features.get(key).getValue();
	}

	public Set<String> getKeySet()
	{
		return features.keySet();
	}
	
	public boolean containsKey(String key) 
	{
		return features.containsKey(key);
	}

	@Override
	public Sample clone()
	{
		Sample result = new Sample();
		
		result.features = new HashMap<String, Feature>();
		Set<String> keys = features.keySet();
		
		for (String key : keys)
		{
			Feature f = features.get(key);
			result.features.put(key, f.clone());
		}
		
		return result;
	}
	

	@Override
	public String toString()
	{
		return getFeatures() + "";
	}

	public int size()
	{
		if (features != null)
			return features.size();
		else
			return 0;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((features == null) ? 0 : features.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sample other = (Sample) obj;
		if (features == null)
		{
			if (other.features != null)
				return false;
		}
		else if (features.size() != other.features.size())
			return false;
		else 
		{
			Set<String> keys = features.keySet();
			
			for (String key : keys)
			{
				if (!other.containsKey(key))
					return false;
				else if (!features.get(key).equals(other.features.get(key)))
					return false;
			}
		}
		return true;
	}
}
