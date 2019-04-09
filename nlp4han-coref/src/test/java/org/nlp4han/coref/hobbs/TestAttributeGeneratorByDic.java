package org.nlp4han.coref.hobbs;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.nlp4han.coref.hobbs.Attribute.Animacy;
import org.nlp4han.coref.hobbs.Attribute.Gender;

import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.TreeNode;
import org.nlp4han.coref.hobbs.Attribute.Number;

public class TestAttributeGeneratorByDic
{
	AttributeGeneratorByDic agbd;
	
	@Before
	public void setUp()
	{
		agbd = new AttributeGeneratorByDic();
	}
	
	@Test
	public void testGetGender()
	{
		TreeNode treeNode = BracketExpUtil.generateTreeNoTopBracket("[(NP(DNP (NP (NN 小明))(DEG 的))(NP (NN 妈妈)))]");
		Set<Gender> result = agbd.getGender(treeNode);
		Set<Gender> goal = new HashSet<Gender>();
		goal.add(Gender.FEMALE);
		assertEquals(goal, result);
		
		TreeNode treeNode1 = BracketExpUtil.generateTreeNoTopBracket("[(NP (DNP (NP (NR 李明)) (DEG 的)) (NP (NN 弟弟)))]");
		Set<Gender> result1 = agbd.getGender(treeNode1);
		Set<Gender> goal1 = new HashSet<Gender>();
		goal1.add(Gender.MALE);
		assertEquals(goal1, result1);
		
		TreeNode treeNode2 = BracketExpUtil.generateTreeNoTopBracket("[(NP(QP (CD 三)(CLP (M 个)))(NP (NN 文件)))]");
		Set<Gender> result2 = agbd.getGender(treeNode2);
		Set<Gender> goal2 = new HashSet<Gender>();
		System.out.println(result2);
		goal2.add(Gender.NONE);
		assertEquals(goal2, result2);
		
	}
	
	
	@Test
	public void testGetNumber()
	{
		TreeNode treeNode = BracketExpUtil.generateTreeNoTopBracket("[(NP(DNP (NP (NN 小明))(DEG 的))(NP (NN 妈妈)))]");
		Set<Number> result = agbd.getNumber(treeNode);
		Set<Number> goal = new HashSet<Number>();
		assertEquals(goal, result);
		
		TreeNode treeNode2 = BracketExpUtil.generateTreeNoTopBracket("[(NP(QP (CD 三)(CLP (M 个)))(NP (NN 文件)))]");
		Set<Number> result2 = agbd.getNumber(treeNode2);
		Set<Number> goal2 = new HashSet<Number>();
		goal2.add(Number.PLURAL);
		assertEquals(goal2, result2);
		
		TreeNode treeNode3 = BracketExpUtil.generateTreeNoTopBracket("[(NP(NP(DNP(NP(NR 张三))(DEG 的))(NP(NN 妈妈)))(PU 、)(NP(DNP(NP(ADJP(JJ 小))(NP(NN 明)))(DEG 的))(NP(NN 哥哥)))(CC 和)(NP(PN 我)))]");
		Set<Number> result3 = agbd.getNumber(treeNode3);
		Set<Number> goal3 = new HashSet<Number>();
		goal3.add(Number.PLURAL);
		assertEquals(goal3, result3);
		
		TreeNode treeNode4 = BracketExpUtil.generateTreeNoTopBracket("[(NP(QP (CD 一) (CLP (M 条)))(NP (NN 狗)))]");
		Set<Number> result4 = agbd.getNumber(treeNode4);
		Set<Number> goal4 = new HashSet<Number>();
		goal4.add(Number.SINGULAR);
		assertEquals(goal4, result4);
		
		TreeNode treeNode5 = BracketExpUtil.generateTreeNoTopBracket("[(NP(DP (DT 这个)) (NP (NN 人)))]");
		Set<Number> result5 = agbd.getNumber(treeNode5);
		Set<Number> goal5 = new HashSet<Number>();
		goal5.add(Number.SINGULAR);
		assertEquals(goal5, result5);
		
		TreeNode treeNode6 = BracketExpUtil.generateTreeNoTopBracket("[(NP(DP (DT 那些)) (NP (NN 东西)))]");
		Set<Number> result6 = agbd.getNumber(treeNode6);
		Set<Number> goal6 = new HashSet<Number>();
		goal6.add(Number.PLURAL);
		assertEquals(goal6, result6);
	}
	
	@Test
	public void testGetAnimacy()
	{
		TreeNode treeNode = BracketExpUtil.generateTreeNoTopBracket("[(NP (QP (CD 一) (CLP (M 只))) (ADJP (JJ 大)) (NP (NN 狼狗)))]");
		Set<Animacy> result = agbd.getAnimacy(treeNode);
		Set<Animacy> goal = new HashSet<Animacy>();
		goal.add(Animacy.ANI_ANIMAL);
		assertEquals(goal, result);
		
		TreeNode treeNode2 = BracketExpUtil.generateTreeNoTopBracket("[(NP (DNP (NP (NN 黑色))(DEG 的)) (NP (NN 椅子)))]");
		Set<Animacy> result2 = agbd.getAnimacy(treeNode2);
		Set<Animacy> goal2 = new HashSet<Animacy>();
		goal2.add(Animacy.INANIMACY);
		assertEquals(goal2, result2);
		
		TreeNode treeNode3 = BracketExpUtil.generateTreeNoTopBracket("[(NP(DNP (NP (NN 小明))(DEG 的))(NP (NN 妈妈)))]");
		Set<Animacy> result3 = agbd.getAnimacy(treeNode3);
		Set<Animacy> goal3 = new HashSet<Animacy>();
		goal3.add(Animacy.ANI_HUMAN);
		assertEquals(goal3, result3);
		
		TreeNode treeNode4 = BracketExpUtil.generateTreeNoTopBracket("[(PN 它)]");
		Set<Animacy> result4 = agbd.getAnimacy(treeNode4);
		Set<Animacy> goal4 = new HashSet<Animacy>();
		goal4.add(Animacy.ANI_ANIMAL);
		goal4.add(Animacy.INANIMACY);
		assertEquals(goal4, result4);
	}
}
