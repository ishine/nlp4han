package org.nlp4han.coref.centering;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nlp4han.coref.sieve.Document;
import org.nlp4han.coref.sieve.GrammaticalRoleBasedMentionGenerator;
import org.nlp4han.coref.sieve.Mention;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.TreeNode;

public class TestGrammaticalRoleBasedMentionGenerator
{
	@Test
	public void testEntities_1()
	{
		String str;
		// 句一：小明的妈妈是一名教师。
		str = "((IP(NP(DNP(NP(NN 小明))(DEG 的))(NP(NN 妈妈)))(VP(VC 是)(NP(QP(CD 一)(CLP(M 名)))(NP(NN 教师))))(PU 。)))";
		TreeNode s1 = BracketExpUtil.generateTreeNoTopBracket(str);
		
		List<TreeNode> trees = new ArrayList<TreeNode>();
		trees.add(s1);
		
		Document doc  = new Document();
		doc.setTrees(trees);

		GrammaticalRoleBasedMentionGenerator mg = new GrammaticalRoleBasedMentionGenerator();
		List<List<Mention>> mentions = mg.generate(doc).getMentionsBySentences();
		
		assertEquals(3, mentions.get(0).size());
		
		Mention e0 = mentions.get(0).get(0);
		assertEquals("小明", e0.getHead());
		assertEquals(0, e0.getHeadIndex());
		assertEquals("SBJ", e0.getGrammaticalRole());
		
		Mention e1 = mentions.get(0).get(1);
		assertEquals("妈妈", e1.getHead());
		assertEquals(2, e1.getHeadIndex());
		assertEquals("SBJ", e1.getGrammaticalRole());

		Mention e2 = mentions.get(0).get(2);
		assertEquals("教师", e2.getHead());
		assertEquals(6, e2.getHeadIndex());
		assertEquals("OBJ", e2.getGrammaticalRole());

	}
	
	@Test
	public void testEntities_2()
	{
		String str;
		str = "((IP(IP(NP(NN 父亲))(VP(ADVP(AD 原本))(ADVP(AD 没有))(VP(VV 做)(AS 过)(NP(NN 大事)))))(PU ，)(IP(NP(PN 他)(NN 一生))(VP(ADVP(AD 都))(PP(P在)(LCP(NP(NN 澡堂))(LC 中)))(VP(VV 替人)(IP(VP(VV 按摩))))))(PU 。)))";
		TreeNode s = BracketExpUtil.generateTreeNoTopBracket(str);
		
		List<TreeNode> trees = new ArrayList<TreeNode>();
		trees.add(s);
		
		Document doc  = new Document();
		doc.setTrees(trees);

		GrammaticalRoleBasedMentionGenerator mg = new GrammaticalRoleBasedMentionGenerator();
		List<List<Mention>> mentions = mg.generate(doc).getMentionsBySentences();
		
//		assertEquals(4, mentions.get(0).size());
//		
//		Mention e0 = mentions.get(0).get(0);
//		assertEquals("父亲", e0.getHead());
//		assertEquals(0, e0.getHeadIndex());
//		assertEquals("SBJ", e0.getGrammaticalRole());
//		
//		Mention e1 = mentions.get(0).get(1);
//		assertEquals("他", e1.getHead());
//		assertEquals(7, e1.getHeadIndex());
//		assertEquals("SBJ", e1.getGrammaticalRole());
//
//		Mention e2 = mentions.get(0).get(2);
//		assertEquals("一生", e2.getHead());
//		assertEquals(8, e2.getHeadIndex());
//		assertEquals("SBJ", e2.getGrammaticalRole());
//		
//		Mention e3 = mentions.get(0).get(3);
//		assertEquals("大事", e3.getHead());
//		assertEquals(5, e3.getHeadIndex());
//		assertEquals("OBJ", e3.getGrammaticalRole());
	}
}
