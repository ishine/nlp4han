package com.lc.nlp4han.constituent;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.lc.nlp4han.constituent.pcfg.ConstituentParserCKYPCNF;
import com.lc.nlp4han.constituent.pcfg.PCFG;

public class ParserFactory
{
	private static ConstituentParser parser;

	private ParserFactory()
	{
	}

	/**
	 * 装入中文词性标注模型，并生成中文词性标注器
	 * 
	 * @return 中文词性标注器
	 * @throws IOException
	 */
	public static ConstituentParser getParser() throws IOException
	{
		if (parser != null)
			return parser;

		String modelName = "com/lc/nlp4han/constituent/ctb8-pcnf.model";

		InputStream modelIn = ParserFactory.class.getClassLoader().getResourceAsStream(modelName);
//		String encoding = "UTF-8";
		
		DataInput in = new DataInputStream(modelIn);
		PCFG pcnf = new PCFG();
		pcnf.read(in);

//		PCFG grammar = new PCFG(modelIn, encoding);
		
//		System.out.println(grammar.isCNF());
//		System.out.println(grammar.isLooseCNF());

		double pruneThreshold = 0.0001;
		boolean secondPrune = false;
		boolean prior = false;

		parser = new ConstituentParserCKYPCNF(pcnf, pruneThreshold, secondPrune, prior);

		return parser;
	}

	public static void main(String[] args) throws IOException
	{
		String[] words = new String[] { "我", "喜欢", "你", "。" };

		ConstituentParser parser = ParserFactory.getParser();
		ConstituentTree tree = parser.parse(words, null);
		System.out.println(tree.toPrettyString());
	}
}
