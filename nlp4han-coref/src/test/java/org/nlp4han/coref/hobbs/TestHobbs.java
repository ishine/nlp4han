package org.nlp4han.coref.hobbs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.nlp4han.coref.AnaphoraResult;
import org.nlp4han.coref.centering.EvaluationBFP;
import org.nlp4han.coref.hobbs.Hobbs;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreeNodeUtil;

public class TestHobbs
{
	private Hobbs hobbs = new Hobbs();

	@Test
	public void testHobbs_1()
	{
		String str;
		// 句一：小明的妈妈是一名教师。
		str = "((IP(NP(DNP(NP(NN 小明))(DEG 的))(NP(NN 妈妈)))(VP(VC 是)(NP(QP(CD 一)(CLP(M 名)))(NP(NN 教师))))(PU 。)))";
		TreeNode s1 = BracketExpUtil.generateTreeNoTopBracket(str);
		// 句二：家长们都很喜欢她。
		str = "((IP(IP(NP(NN 家长们))(VP(ADVP(AD 都))(ADVP(AD 很))(VP(VV 喜欢)(NP(PN 她)))))(PU 。)))";
		TreeNode s2 = BracketExpUtil.generateTreeNoTopBracket(str);
		
		List<TreeNode> constituentTrees = new ArrayList<TreeNode>();
		constituentTrees.add(s1);
		constituentTrees.add(s2);
		
		List<AnaphoraResult> result = hobbs.resolve(constituentTrees);
		
		List<String> resultStr = EvaluationBFP.toStringFormat(result, constituentTrees);
		
		List<String> goal = new ArrayList<String>();
		goal.add("她(2-5)->妈妈(1-3)");
		
		assertEquals(goal, resultStr);
	}

	@Test
	public void testHobbs_2()
	{
		String str;
		// 句一：小明的妈妈是一名教师。
		str = "((IP(NP(DNP(NP(NN 小明))(DEG 的))(NP(NN 妈妈)))(VP(VC 是)(NP(QP(CD 一)(CLP(M 名)))(NP(NN 教师))))(PU 。)))";
		TreeNode s1 = BracketExpUtil.generateTreeNoTopBracket(str);
		// 句二：张三的妈妈都很喜欢她。
		str = "((IP(IP(NP(DNP(NP(NR 张三))(DEG 的))(NP(NN 妈妈)))(VP(ADVP(AD 很))(VP(VV 喜欢)(NP(PN 她)))))(PU 。)))";
		TreeNode s2 = BracketExpUtil.generateTreeNoTopBracket(str);

		List<TreeNode> constituentTrees = new ArrayList<TreeNode>();
		constituentTrees.add(s1);
		constituentTrees.add(s2);

		List<AnaphoraResult> result = hobbs.resolve(constituentTrees);
		List<String> resultStr = EvaluationBFP.toStringFormat(result, constituentTrees);
		
		List<String> goal = new ArrayList<String>();
		goal.add("她(2-5)->妈妈(1-3)");
		
		assertNotEquals(goal, resultStr);
	}

	//
	@Test
	public void testHobbs_3()
	{	//注释：此句“这一辈子”需通过查字典赋Animacy属性排除。

		String str;
		// 句一： 黄秋雅是个老姑娘，她这一辈子，大概连恋爱都没谈过。
		str = "((IP(IP(NP(NR 黄秋雅))(VP(VC 是)(NP(QP(CLP(M 个)))(ADJP(JJ 老))(NP(NN 姑娘)))))(PU ，)(IP(NP(PN 她))(NP(DP(DT 这)(QP(CD 一)))(NP(NN 辈子)))(PU ，)(VP(VP(ADVP(AD 大概))(ADVP(AD 连))(VP(VV 恋爱)))(VP(ADVP(AD 都))(ADVP(AD 没))(VP(VV 谈过)))))(PU 。)))";
		TreeNode s1 = BracketExpUtil.generateTreeNoTopBracket(str);

		List<TreeNode> constituentTrees = new ArrayList<TreeNode>();
		constituentTrees.add(s1);

		List<AnaphoraResult> result = hobbs.resolve(constituentTrees);
		List<String> resultStr = EvaluationBFP.toStringFormat(result, constituentTrees);
		
		List<String> goal = new ArrayList<String>();
		goal.add("她(1-7)->黄秋雅(1-1)");
		
		assertEquals(goal, resultStr);
	}
	
	@Test
	public void testHobbs_4()
	{
		String str;
		str = "(IP (NP (NR 李明)) (VP (VV 喜欢) (NP(NP (PN 他))(NP (NN 爷爷))))(PU 。))";
		TreeNode s1 = BracketExpUtil.generateTree(str);

		List<TreeNode> constituentTrees = new ArrayList<TreeNode>();
		constituentTrees.add(s1);

		List<AnaphoraResult> result = hobbs.resolve(constituentTrees);
		
		assertEquals(1, result.size());
		
		TreeNode pron = result.get(0).getPronNode();
		assertEquals("他", TreeNodeUtil.getetWordString(pron));
		
		TreeNode antecedent = result.get(0).getAntecedentNode();
		assertEquals("李明", TreeNodeUtil.getetWordString(antecedent));
	}
	
}
