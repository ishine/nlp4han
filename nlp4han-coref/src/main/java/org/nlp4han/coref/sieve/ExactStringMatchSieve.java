package org.nlp4han.coref.sieve;

import java.util.List;

public class ExactStringMatchSieve implements Sieve
{

	@Override
	public boolean coreferent(CorefCluster mentionCluster, CorefCluster potentialAntecedent)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Mention> getOrderedAntecedents(Mention mention, List<List<Mention>> orderedMentionsBySentence,
			List<CorefCluster> corefClusters)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
