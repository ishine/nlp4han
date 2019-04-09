package org.nlp4han.coref.sieve;

import java.util.List;

public interface Sieve
{
	public boolean coreferent(CorefCluster mentionCluster, CorefCluster potentialAntecedent);
	
	public List<Mention> getOrderedAntecedents(Mention mention, List<List<Mention>> orderedMentionsBySentence, List<CorefCluster> corefClusters);
}
