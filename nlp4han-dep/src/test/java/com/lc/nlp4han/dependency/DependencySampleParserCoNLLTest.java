package com.lc.nlp4han.dependency;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * 解析CoNLL语料的单元测试
 * 测试解析后样本输出为CoNLL格式、输出为Stanford样式
 * 
 * @author 王馨苇
 *
 */
public class DependencySampleParserCoNLLTest {

	private String corpus;
	private DependencySampleParser parser;
	private DependencySample sample;
	
	@Before
	public void setUp(){
		corpus = "1	坚决	坚决	a	a	_	2	方式	_	_" + "\n" +
	             "2	惩治	惩治	v	v	_	0	核心成分	_	_" + "\n" +
	             "3	贪污	贪污	v	v	_	7	限定	_	_" + "\n" +
	             "4	贿赂	贿赂	n	n	_	3	连接依存	_	_" + "\n" +
	             "5	等	等	u	u	_	3	连接依存	_	_" + "\n" +
	             "6	经济	经济	n	n	_	7	限定	_	_" + "\n" +
	             "7	犯罪	犯罪	v	v	_	2	受事	_	_" + "\n" ;
		
		parser = new DependencySampleParserCoNLL();
		sample = parser.parse(corpus);
	}
	
	/**
	 * 测试输出为CoNLL格式
	 */
	@Test
	public void testCoNLL(){
		assertEquals(corpus, sample.toCoNLLString());
	}
	
	/**
	 * 输出为Stanford样式
	 */
	@Test
	public void testStanford(){
		String result = 
				"核心成分(" + DependencyParser.RootWord + "-0,惩治-2)" + "\n" +
				"方式(惩治-2,坚决-1)" + "\n" +
				"限定(犯罪-7,贪污-3)" + "\n" +
				"连接依存(贪污-3,贿赂-4)" + "\n" +
				"连接依存(贪污-3,等-5)" + "\n" +
				"限定(犯罪-7,经济-6)" + "\n" +
				"受事(惩治-2,犯罪-7)" + "\n" ;
		assertEquals(result, sample.toStanfordString());
	}
}
