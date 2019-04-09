package com.lc.nlp4han.clustering;

import java.util.List;

public interface Initialization
{
	public List<Group> initialize(List<Text> texts, int k);
}
