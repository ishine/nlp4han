package com.lc.nlp4han.constituent.pcfg;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

public class GrammarIOTest
{

	@Test
	public void test() throws IOException
	{
		String r1 = "(A (B b) (C c))";
		String r2 = "(A (B b) (C c) (D d))";
		
		ArrayList<String> bracketStrList = new ArrayList<String>();
		bracketStrList.add(r2);
		bracketStrList.add(r1);
		
		PCFG pcfg = GrammarExtractor.getPCFG(bracketStrList);
		
		ByteArrayOutputStream ss = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(ss);
		
		pcfg.write(out);
		out.close();
		
		byte[] bs = ss.toByteArray();
		
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bs));
		
		PCFG pcfg2 = new PCFG();
		pcfg2.read(in);
		
		assertEquals(pcfg, pcfg2);
	}

}
