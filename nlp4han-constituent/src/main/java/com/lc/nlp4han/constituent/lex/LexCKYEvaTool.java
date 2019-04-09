package com.lc.nlp4han.constituent.lex;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.lc.nlp4han.constituent.ConstituentMeasure;
import com.lc.nlp4han.constituent.ConstituentTree;
import com.lc.nlp4han.constituent.PlainTextByTreeStream;
import com.lc.nlp4han.constituent.pcfg.CKYParserEvaluator;
import com.lc.nlp4han.constituent.pcfg.ConstituentTreeStream;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;
import com.lc.nlp4han.ml.util.ObjectStream;

public class LexCKYEvaTool
{
	public static void main(String args[]) throws ClassNotFoundException, IOException
	{
		if (args.length == 0)
		{
			return;
		}

		String modelpath = null;
		String goldpath = null;
		double pruneThreshold = 0.001;
		String encoding = "utf-8";
		boolean secondPrune = false;
		boolean prior = false;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-model"))
			{
				modelpath = args[i + 1];
				i++;
			}
			else if (args[i].equals("-gold"))
			{
				goldpath = args[i + 1];
				i++;
			}
			else if (args[i].equals("-encoding"))
			{
				encoding = args[i + 1];
				i++;
			}
			else if (args[i].equals("-secondePrune"))
			{
				secondPrune = Boolean.parseBoolean(args[i + 1]);
				i++;
			}
			else if (args[i].equals("-prior"))
			{
				prior = Boolean.parseBoolean(args[i + 1]);
				i++;
			}
		}
		eval(modelpath, goldpath, encoding, pruneThreshold, secondPrune, prior);
	}

	private static void eval(String modelpath, String goldpath, String encoding, double pruneThreshold,
			boolean secondPrune, boolean prior) throws ClassNotFoundException, IOException
	{
		DataInput in = new DataInputStream(new FileInputStream(modelpath));
		LexPCFG lexPCFG = new LexPCFG();
		lexPCFG.read(in);
		
//		LexPCFG lexpcfg = LexPCFGModelIOUtil.loadModel(modelpath);
		
		CKYParserEvaluator evaluator = new CKYParserEvaluator(lexPCFG,pruneThreshold,secondPrune,prior);
		
		ConstituentMeasure measure = new ConstituentMeasure();
		evaluator.setMeasure(measure);
		
		ObjectStream<String> treeStream = new PlainTextByTreeStream(new FileInputStreamFactory(new File(goldpath)),
				encoding);
		ObjectStream<ConstituentTree> sampleStream = new ConstituentTreeStream(treeStream);
		evaluator.evaluate(sampleStream);
		
		ConstituentMeasure measureRes = evaluator.getMeasure();
		System.out.println(measureRes);
	}
}
