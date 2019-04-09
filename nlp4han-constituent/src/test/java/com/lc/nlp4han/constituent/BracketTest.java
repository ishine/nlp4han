package com.lc.nlp4han.constituent;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

import org.junit.Test;

public class BracketTest
{
	
	@Test
	public void formatTest()
	{
		String bracketStr = "(A (B1(C1 d1)(C2 d2)) (D3 d3))  ";
		String expectStr1 = "A(B1(C1 d1)(C2 d2))(D3 d3)";
		String expectStr2 = "(A(B1(C1 d1)(C2 d2))(D3 d3))";
		
		String formatStr1 = BracketExpUtil.formatNoTopBracket(bracketStr);	
		assertEquals(expectStr1, formatStr1);
		
		String formatStr2 = BracketExpUtil.format(bracketStr);	
		assertEquals(expectStr2, formatStr2);
	}
	
	@Test
	public void generateTest()
	{
		String bracketStr = "(A (B1(C1 d1)(C2 d2)) (D3 d3))  ";
		String expectStr1 = "(A(B1(C1 d1)(C2 d2))(D3 d3))";
		
		TreeNode tree1 = BracketExpUtil.generateTree(bracketStr);
		String s1 = tree1.toString();
		String formatStr1 = BracketExpUtil.format(s1);	
		assertEquals(expectStr1, formatStr1);
		
		String bracketStr2 = "(A (B1(C1 d1)  (C2 d2)) ) ";
		String expectStr2 = "B1(C1 d1)(C2 d2)";
		String expectStr3 = "(B1(C1 d1)(C2 d2))";
		
		TreeNode tree2 = BracketExpUtil.generateTreeNoTopBracket(bracketStr2);
		String s2 = tree2.toString();
		assertEquals(expectStr3, s2);
		String formatStr2 = BracketExpUtil.formatNoTopBracket(s2);	
		assertEquals(expectStr2, formatStr2);
	}
	
	@Test
	public void readBrackesTest() throws IOException
	{
		String bracketStr1 = "(A (B1(C1 d1)(C2 d2)) \r\n (D3 d3)) ";
		String bracketStr2 = "(A \n (B1(C1 d1)(C2 d2)) (D3 d3)) \n (A (B1 b1)) ";
		String bstr2 = "(A(B1 b1))";
		
		ArrayList<String> brackets = BracketExpUtil.readBrackets(bracketStr1);		
		assertEquals(1, brackets.size());
		
		brackets = BracketExpUtil.readBrackets(bracketStr2);	
		assertEquals(2, brackets.size());
		
		String bstr = brackets.get(1);
		String fstr = BracketExpUtil.format(bstr);	
		assertEquals(bstr2, fstr);
	}
}
