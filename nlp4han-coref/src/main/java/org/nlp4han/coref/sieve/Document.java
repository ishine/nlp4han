package org.nlp4han.coref.sieve;

import java.util.List;

import com.lc.nlp4han.constituent.TreeNode;

public class Document
{
	private String content;
	
	private List<TreeNode> trees;
	
	private List<CorefCluster> corefClusters;
	private List<List<Mention>> mentionsBySentences;
	private List<Mention> mentions;
	private List<List<Entity>> entitiesBySentences;
	
	public Document()
	{
		
	}
	
	public Document(List<TreeNode> trees)
	{
		this.trees = trees;
	}
	
	public Document(String content)
	{
		// TODO Auto-generated constructor stub
	}

	public List<List<Mention>> getMentionsBySentences()
	{
		return mentionsBySentences;
	}
	
	public List<Mention> getMentions()
	{
		return mentions;
	}
	
	public void setMentions(List<List<Mention>> mentions)
	{
		this.mentionsBySentences = mentions;
	}

	public List<List<Entity>> getEntities()
	{
		return entitiesBySentences;
	}

	public void setEntities(List<List<Entity>> entities)
	{
		this.entitiesBySentences = entities;
	}

	public List<TreeNode> getTrees()
	{
		return trees;
	}

	public void setTrees(List<TreeNode> trees)
	{
		this.trees = trees;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public List<CorefCluster> getCorefClusters()
	{
		return corefClusters;
	}

	public void setCorefClusters(List<CorefCluster> corefClusters)
	{
		this.corefClusters = corefClusters;
	}

}
