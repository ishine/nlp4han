package com.lc.nlp4han.srl.propbank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lc.nlp4han.constituent.AbstractHeadGenerator;
import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreePreprocessTool;
import com.lc.nlp4han.ml.util.FilterObjectStream;
import com.lc.nlp4han.ml.util.ObjectStream;

/**
 * 解析样本流
 * @author 王馨苇
 *
 */
public class SRLSampleStream extends FilterObjectStream<String[],SRLSample<HeadTreeNode>>{

	private Logger logger = Logger.getLogger(SRLSampleStream.class.getName());
	private AbstractParseStrategy<HeadTreeNode> parse;
	private AbstractHeadGenerator aghw;
	
	public SRLSampleStream(ObjectStream<String[]> samples, AbstractParseStrategy<HeadTreeNode> parse, AbstractHeadGenerator aghw) {
		super(samples);
		this.parse = parse;
		this.aghw = aghw;
	}

	public SRLSample<HeadTreeNode> read() throws IOException {				
		String[] sentence = samples.read();	
		SRLSample<HeadTreeNode> sample = null;
		
		if(sentence[0]!= null && sentence[1] != null){
			try{
				TreeNode tree = BracketExpUtil.generateTreeNoTopBracket(sentence[0]);
				TreePreprocessTool.deleteNone(tree);
                sample = parse.parse(tree, sentence[1], aghw);
			}catch(Exception e){
				
				if (logger.isLoggable(Level.WARNING)) {						
	                logger.warning("Error during parsing, ignoring sentence: " + sentence[1]);
	            }	
				sample = new SRLSample<HeadTreeNode>(new HeadTreeNode(""), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
				}
			
			return sample;
		}else {
			return null;
		}
	}
}
