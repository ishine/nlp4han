package com.lc.nlp4han.constituent.unlex;

import org.junit.Test;

import com.lc.nlp4han.constituent.unlex.AnnotationTreeNode;
import com.lc.nlp4han.constituent.unlex.GrammarExtractor;

public class AnnotationTreeNodeTest
{
	@Test
	public void getAnnotationTreeTest()
	{
		String[] sentences = { "(ROOT(IP(NP (PN i))(VP (VV like)(NP (DT the) (NN book)))))",
				"(ROOT(VP (VT give)(NP (PN me))(NP (CD two) (NN books))))",
				"(ROOT(IP(NP (DT the) (NN boy))(VP (VT saw)(NP (DT the) (NN dog)))))" };
		TreeBank treeBank = new TreeBank();
		for (int i = 0; i < sentences.length; i++)
		{
			treeBank.addTree(sentences[i], false);
		}
	}

	@Test
	public void IOScoreTest()
	{

		Boolean addParentLabel = false;
		String[] sentences = { "(ROOT(IP(NP (PN i))(VP (VV like)(NP (DT the) (NN book)))))",
				"(ROOT(VP (VT give)(NP (PN me))(NP (CD two) (NN books))))",
				"(ROOT(IP(NP (DT the) (NN boy))(VP (VT saw)(NP (DT the) (NN dog)))))" };
		TreeBank treeBank = new TreeBank();
		for (int i = 0; i < sentences.length; i++)
		{
			treeBank.addTree(sentences[i], addParentLabel);
		}
		GrammarExtractor gExtractor = new GrammarExtractor();
		Grammar g = gExtractor.extractLatentGrammar(treeBank, Lexicon.DEFAULT_RAREWORD_THRESHOLD, 0, 50, 0.5,
				0.01);
		GrammarSpliter.splitGrammar(g, treeBank);
		for (AnnotationTreeNode tree : treeBank.getTreeBank())
		{
			TreeBank.calculateInnerScore(g, tree);
			TreeBank.calculateOuterScore(g, tree);
		}
	}
}