package com.lc.nlp4han.constituent.lex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LexPCFGTest
{
	private LexPCFG lexpcfg;
	private OccurenceHeadChild rhcg0;

	@Before
	public void beforeTest() throws IOException
	{
		ArrayList<String> CTBList = new ArrayList<String>();
		String[] sentences = new String[3];
		sentences[0] = "(ROOT(NP(NN a)(NR b)(NN c)))";
		sentences[1] = "(ROOT(NP(NP(NR h))(VV d)(NP(NR e))))";
		sentences[2] = "(ROOT(IP(NP(NN b))(ADJP1(JJ f))(ADJP2(JJ g))))";
		for (String str : sentences)
		{
			CTBList.add(str);
		}
		lexpcfg = LexGrammarExtractor.getLexPCFG(CTBList);
	}

	@Test
	// 测试提取的文法内容
	public void testLexPCFGGrammar()
	{
		// 测试得到起始符
		String startSymbol = lexpcfg.getStartSymbol();
		Assert.assertEquals(startSymbol, "ROOT");

		// 测试得到词性标注的总集合
		String[] poses = { "NN", "VV", "NR", "JJ" };
		HashSet<String> posSet = new HashSet<String>();
		for (String pos : poses)
		{
			posSet.add(pos);
		}
		HashSet<String> set = lexpcfg.getPosSet();
		Assert.assertEquals(set, posSet);

		// 测试由headChild得到可行的Parent/或者向上延伸的单元规则
		// 在测试中发现当我使用以lael(pos,word)进行解析时数据过少，而以label(null,null)为条件进行搜索parent时，候选的父节点又过于多
		// 故，我以label(pos,null)为条件进行搜索，对效果并无影响
		rhcg0 = new OccurenceHeadChild("JJ", null, "JJ", null);
		HashSet<String> posSetOfParent = new HashSet<String>();
		posSetOfParent.add("ADJP1");
		posSetOfParent.add("ADJP2");
		HashSet<String> parentSet = lexpcfg.getParentSet(rhcg0);
		Assert.assertEquals(parentSet, posSetOfParent);

		// 生成头规则数据,以(NP (NN a) (NN b) (NN c))为例
		String headLabel = "NN";
		String headPos = "NN";
		String headWord = "c";
		String parentLabel = "NPB";
		OccurenceHeadChild rhcgs[] = new OccurenceHeadChild[6];
		rhcgs[0] = new OccurenceHeadChild(headLabel, parentLabel, headPos, headWord);
		rhcgs[3] = new OccurenceHeadChild(null, parentLabel, headPos, headWord);
		Assert.assertTrue(lexpcfg.getHeadGenRuleAmountsInfo(rhcgs[0]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getHeadGenRuleAmountsInfo(rhcgs[3]).equals(new RuleAmountsInfo(1,1)));
		
		// 回退模型2使用
		rhcgs[1] = new OccurenceHeadChild(headLabel, parentLabel, headPos, null);
		Assert.assertTrue(lexpcfg.getHeadGenRuleAmountsInfo(rhcgs[1]).equals(new RuleAmountsInfo(2,0)));
		rhcgs[4] = new OccurenceHeadChild(null, parentLabel, headPos, null);
		Assert.assertTrue(lexpcfg.getHeadGenRuleAmountsInfo(rhcgs[4]).equals(new RuleAmountsInfo(2,1)));
		
		// 回退模型3使用
		rhcgs[2] = new OccurenceHeadChild(headLabel, parentLabel, null, null);
		Assert.assertTrue(lexpcfg.getHeadGenRuleAmountsInfo(rhcgs[2]).equals(new RuleAmountsInfo(2,0)));
		rhcgs[5] = new OccurenceHeadChild(null, parentLabel, null, null);
		Assert.assertTrue(lexpcfg.getHeadGenRuleAmountsInfo(rhcgs[5]).equals(new RuleAmountsInfo(4,2)));

		// 生成stop规则数据,以(NP (NN a) (NR b) (NN c))为例,当父节点为NPB时，将中心孩子换为上一个孩子（以中心词下标为起点）
		Distance distance2 = new Distance(false, false);// 此时的距离变量将忽略
		OccurenceStop[] rsgs = new OccurenceStop[12];
		// 回退模型1
		rsgs[0] = new OccurenceStop(headLabel, parentLabel, headPos, headWord, 1, false, distance2);// 左侧第一个
		rsgs[1] = new OccurenceStop("NR", parentLabel, "NR", "b", 1, false, distance2);// 左侧第二个
		rsgs[2] = new OccurenceStop(headLabel, parentLabel, headPos, "a", 1, true, distance2);// 左侧第三个
		rsgs[3] = new OccurenceStop(headLabel, parentLabel, headPos, headWord, 2, true, distance2);// 右侧第一个
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[0]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[1]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[2]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[3]).equals(new RuleAmountsInfo(1,0)));
	
		// 回退模型2
		rsgs[4] = new OccurenceStop(headLabel, parentLabel, headPos, null, 1, false, distance2);// 左侧第一个
		rsgs[5] = new OccurenceStop("NR", parentLabel, "NR", null, 1, false, distance2);// 左侧第二个
		rsgs[6] = new OccurenceStop(headLabel, parentLabel, headPos, null, 1, true, distance2);// 左侧第三个
		rsgs[7] = new OccurenceStop(headLabel, parentLabel, headPos, null, 2, true, distance2);// 右侧第一个
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[4]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[5]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[6]).equals(new RuleAmountsInfo(2,0)));
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[7]).equals(new RuleAmountsInfo(2,0)));
		
		// 回退模型3
		rsgs[8] = new OccurenceStop(headLabel, parentLabel, null, null, 1, false, distance2);// 左侧第一个
		rsgs[9] = new OccurenceStop(headLabel, parentLabel, null, null, 1, false, distance2);// 左侧第二个
		rsgs[10] = new OccurenceStop(headLabel, parentLabel, null, null, 1, true, distance2);// 左侧第三个
		rsgs[11] = new OccurenceStop(headLabel, parentLabel, null, null, 2, true, distance2);// 右侧第一个
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[8]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[9]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[10]).equals(new RuleAmountsInfo(2,0)));
		Assert.assertTrue(lexpcfg.getStopGenRuleAmountsInfo(rsgs[11]).equals(new RuleAmountsInfo(2,0)));

		// 生成两侧规则数据，以(NP(NP(NR h))(VV d)(NP(NR e)))，也就是NP(,d)->NP(NR,h) VV(VV d) NP(NR
		// e)为例
		headLabel = "NPB";
		parentLabel = "NP";
		headPos = "NR";
		headWord = "e";
		String lsideLabel1 = "VV";
		String lsideLabel2 = "NPB";
		String lsideHeadPos1 = "VV";
		String lsideHeadPos2 = "NR";
		String lsideHeadWord1 = "d";
		String lsideHeadWord2 = "h";
		int direction = 1;
		boolean coor = false;
		boolean pu = false;
		Distance ldistance1 = new Distance(true, false);
		Distance ldistance2 = new Distance(false, true);
		// 左侧第一个符号
		OccurenceSides[] rsg = new OccurenceSides[21];
		// 生成两侧Label和pos的回退模型
		// 回退模型1
		rsg[1] = new OccurenceSides(headLabel, parentLabel, headPos, headWord, direction, lsideLabel1, lsideHeadPos1,
				null, coor, pu, ldistance1);
		rsg[2] = new OccurenceSides(headLabel, parentLabel, headPos, headWord, direction, null, null, null, coor, pu,
				ldistance1);
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[1]).equals(new RuleAmountsInfo(2,1)));
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[2]).equals(new RuleAmountsInfo(1,1)));
		
		// 回退模型二
		rsg[3] = new OccurenceSides(headLabel, parentLabel, headPos, null, direction, lsideLabel1, lsideHeadPos1,
				null, coor, pu, ldistance1);
		rsg[4] = new OccurenceSides(headLabel, parentLabel, headPos, null, direction, null, null, null, coor, pu,
				ldistance1);
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[3]).equals(new RuleAmountsInfo(2,1)));
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[4]).equals(new RuleAmountsInfo(1,1)));
		
		// 回退模型三
		rsg[5] = new OccurenceSides(headLabel, parentLabel, null, null, direction, lsideLabel1, lsideHeadPos1, null,
				coor, pu, ldistance1);
		rsg[6] = new OccurenceSides(headLabel, parentLabel, null, null, direction, null, null, null, coor, pu,
				ldistance1);
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[5]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[6]).equals(new RuleAmountsInfo(1,1)));

		// 生成两侧word的回退模型
		// 回退模型1
		rsg[7] = new OccurenceSides(headLabel, parentLabel, headPos, headWord, direction, lsideLabel1, lsideHeadPos1,
				lsideHeadWord1, coor, pu, ldistance1);
		rsg[8] = new OccurenceSides(headLabel, parentLabel, headPos, headWord, direction, lsideLabel1, lsideHeadPos1,
				null, coor, pu, ldistance1);
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[7]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[8]).equals(new RuleAmountsInfo(2,1)));
		
		// 回退模型2
		rsg[9] = new OccurenceSides(headLabel, parentLabel, headPos, null, direction, lsideLabel1, lsideHeadPos1,
				lsideHeadWord1, coor, pu, ldistance1);
		rsg[10] = new OccurenceSides(headLabel, parentLabel, headPos, null, direction, lsideLabel1, lsideHeadPos1,
				null, coor, pu, ldistance1);
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[9]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[10]).equals(new RuleAmountsInfo(2,1)));

		// 左侧第二个符号
		// 生成两侧Label和pos的回退模型
		// 回退模型1
		rsg[11] = new OccurenceSides(headLabel, parentLabel, headPos, headWord, direction, lsideLabel2,
				lsideHeadPos2, null, coor, pu, ldistance2);
		rsg[12] = new OccurenceSides(headLabel, parentLabel, headPos, headWord, direction, null, null, null, coor,
				pu, ldistance2);
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[11]).equals(new RuleAmountsInfo(2,1)));
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[12]).equals(new RuleAmountsInfo(1,1)));
		
		// 回退模型二
		rsg[13] = new OccurenceSides(headLabel, parentLabel, headPos, null, direction, lsideLabel2, lsideHeadPos2,
				null, coor, pu, ldistance2);
		rsg[14] = new OccurenceSides(headLabel, parentLabel, headPos, null, direction, null, null, null, coor, pu,
				ldistance2);
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[13]).equals(new RuleAmountsInfo(2,1)));
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[14]).equals(new RuleAmountsInfo(1,1)));
		
		// 回退模型三
		rsg[15] = new OccurenceSides(headLabel, parentLabel, null, null, direction, lsideLabel2, lsideHeadPos2, null,
				coor, pu, ldistance2);
		rsg[16] = new OccurenceSides(headLabel, parentLabel, null, null, direction, null, null, null, coor, pu,
				ldistance2);
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[15]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[16]).equals(new RuleAmountsInfo(1,1)));

		// 生成两侧word的回退模型
		// 回退模型1
		rsg[17] = new OccurenceSides(headLabel, parentLabel, headPos, headWord, direction, lsideLabel2,
				lsideHeadPos2, lsideHeadWord2, coor, pu, ldistance2);
		rsg[18] = new OccurenceSides(headLabel, parentLabel, headPos, headWord, direction, lsideLabel2,
				lsideHeadPos2, null, coor, pu, ldistance2);
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[17]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[18]).equals(new RuleAmountsInfo(2,1)));
		// 回退模型2
		rsg[19] = new OccurenceSides(headLabel, parentLabel, headPos, null, direction, lsideLabel2, lsideHeadPos2,
				lsideHeadWord2, coor, pu, ldistance2);
		rsg[20] = new OccurenceSides(headLabel, parentLabel, headPos, null, direction, lsideLabel2, lsideHeadPos2,
				null, coor, pu, ldistance2);
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[19]).equals(new RuleAmountsInfo(1,0)));
		Assert.assertTrue(lexpcfg.getSidesGenRuleAmountsInfo(rsg[20]).equals(new RuleAmountsInfo(2,1)));
	}

	@Test
	// 测试求概率的方法（没有写出NPB和特殊（并列或者标点符号）规则情况的测试）
	public void testGetPro()
	{
		// 生成父节点,以(NP (NN a) (NN b) (NN c))为例,求生成父节点的概率
		String headLabel = "NN";
		String headPos = "NN";
		String headWord = "c";
		String parentLabel = "NPB";
		OccurenceHeadChild rhcg = new OccurenceHeadChild(headLabel, parentLabel, headPos, headWord);
		Double pro1 = lexpcfg.getProbForGenerateHead(rhcg);
		double e1 = 1.0;
		double w1 = 1.0 / (1 + 5 * 1);
		double e2 = 1.0;
		double w2 = 2.0 / (2 + 5 * 1);
		double e3 = 0.5;
		double pro = e1 * w1 + (1 - w1) * (e2 * w2 + (1 - w2) * e3);
		Assert.assertTrue(pro == pro1);

		// 生成STOP,以(NP(NP(NR h))(VV d)(NP(NR e)))为例,求生成stop的概率
		headLabel = "NPB";
		parentLabel = "NP";
		headPos = "NR";
		headWord = "e";
		Distance ldistance = new Distance(false, true);
		Distance rdistance = new Distance(true, false);
		OccurenceStop lrsg = new OccurenceStop(headLabel, parentLabel, headPos, headWord, 1, true, ldistance);
		OccurenceStop rrsg = new OccurenceStop(headLabel, parentLabel, headPos, headWord, 2, true, rdistance);
		pro1 = lexpcfg.getProbForGenerateStop(lrsg)* lexpcfg.getProbForGenerateStop(rrsg);
		pro = 0.5;
		Assert.assertTrue(pro1 == pro);

		// 生成两侧孩子，以(NP(NP(NR h))(VV d)(NP(NR e)))的左侧第二个孩子为例
		headLabel = "NPB";
		parentLabel = "NP";
		headPos = "NR";
		headWord = "e";
		boolean coor = false;
		boolean pu = false;
		String sideLabel = "NPB";
		String sideHeadPos = "NR";
		String sideHeadWord = "h";
		int direction = 1;
		Distance distance = new Distance(false, true);
		OccurenceSides rsg = new OccurenceSides(headLabel, parentLabel, headPos, headWord, direction, sideLabel,
				sideHeadPos, sideHeadWord, coor, pu, distance);
		pro1 = lexpcfg.getProbForGenerateSides(rsg);
		pro = 0.7011316872427983;
		Assert.assertTrue(pro1 == pro);
	}
}