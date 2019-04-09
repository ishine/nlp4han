package com.lc.nlp4han.constituent.unlex;

import java.util.ArrayList;

import com.lc.nlp4han.constituent.ConstituentParser;
import com.lc.nlp4han.constituent.ConstituentTree;
import com.lc.nlp4han.constituent.pcfg.ConstituentParserCKYLoosePCNF;
import com.lc.nlp4han.constituent.pcfg.PCFG;
import com.lc.nlp4han.constituent.pcfg.UncompatibleGrammar;

/**
 * 用带父节点标记的语法解析
 */
public class ConstituentParserParent implements ConstituentParser
{
	private ConstituentParserCKYLoosePCNF pcfgParser;

	public ConstituentParserParent(Grammar gPLAdded) throws UncompatibleGrammar
	{
		this(gPLAdded, 0.0001, false, false);
	}

	public ConstituentParserParent(Grammar gPLAdded, double pruneThreshold, boolean secondPrune,
			boolean prior) throws UncompatibleGrammar
	{
		PCFG pcfg = gPLAdded.getPCFG();
		pcfgParser = new ConstituentParserCKYLoosePCNF(pcfg, pruneThreshold, secondPrune, prior);
	}

	@Override
	public ConstituentTree parse(String[] words, String[] poses)
	{
		ConstituentTree[] allTree = parse(words, poses, 1);
		
		if (allTree != null && allTree[0] != null)
			return allTree[0];
		
		return null;
	}

	@Override
	public ConstituentTree[] parse(String[] words, String[] poses, int k)
	{
		ArrayList<ConstituentTree> trees = new ArrayList<>();
		ConstituentTree[] result = pcfgParser.parse(words, poses, k);
		for (ConstituentTree tree : result)
		{
			if (tree != null)
			{
				TreeBinarization.unbinarize(tree.getRoot());
				TreeUtil.removeParentLabel(tree.getRoot());
				trees.add(tree);
			}
		}
		
		if (trees.size() != 0)
			return trees.toArray(new ConstituentTree[trees.size()]);
		else
			return null;
	}

}
