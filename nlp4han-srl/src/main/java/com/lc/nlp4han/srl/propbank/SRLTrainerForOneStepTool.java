package com.lc.nlp4han.srl.propbank;

import java.io.File;
import java.io.IOException;

import com.lc.nlp4han.constituent.AbstractHeadGenerator;
import com.lc.nlp4han.constituent.HeadGeneratorCollins;
import com.lc.nlp4han.constituent.HeadRuleSetPTB;
import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.ml.util.TrainingParameters;

/**
 * 一步完成模型的训练
 * @author 王馨苇
 *
 */
public class SRLTrainerForOneStepTool {
	
	//parse指示文本解析的方式，共四种
	//normal  normalprune addnull addnullprune
	private static void usage(){
		System.out.println(SRLTrainerForOneStepTool.class.getName()+"-data <corpusFile> -model <modelFile> -parse <parsetype> -type <algorithom>"
				+ "-encoding <encoding>"+"[-cutoff <num>] [-iters <num>]");
	}
	
	public static void main(String[] args) throws IOException {
		if(args.length < 1){
			usage();
			return;
		}
		AbstractParseStrategy<HeadTreeNode> parse = null;
		int cutoff = 3;
		int iters = 100;
        File corpusFile = null;
        File modelFile = null;       
        String parsestr = "";
        String encoding = "UTF-8";
        String type = "MAXENT";
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-data"))
            {
                corpusFile = new File(args[i + 1]);
                i++;
            }
            else if (args[i].equals("-model"))
            {
                modelFile = new File(args[i + 1]);
                i++;
            }
            else if (args[i].equals("-parse"))
            {
            	parsestr = args[i + 1];
                i++;
            }
            else if (args[i].equals("-type"))
            {
                type = args[i + 1];
                i++;
            }
            else if (args[i].equals("-encoding"))
            {
                encoding = args[i + 1];
                i++;
            }
            else if (args[i].equals("-cutoff"))
            {
                cutoff = Integer.parseInt(args[i + 1]);
                i++;
            }
            else if (args[i].equals("-iters"))
            {
                iters = Integer.parseInt(args[i + 1]);
                i++;
            }
        }
        
        SRLContextGenerator contextGen = new SRLContextGeneratorConf();
        TrainingParameters params = TrainingParameters.defaultParams();
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(cutoff));
        params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(iters));
        params.put(TrainingParameters.ALGORITHM_PARAM, type.toUpperCase());

        if(parsestr.equals("normal")){
    		parse = new SRLParseNormal();   		
    	}else if(parsestr.equals("normalprune")){
    		parse = new SRLParseNormalWithPruning();
    	}else if(parsestr.equals("addnull")){
    		parse = new SRLParseWithNULL_101();
    	}else{
    		parse = new SRLParseWithNULL_101AndPruning();
    	}
        
        AbstractHeadGenerator ahg = new HeadGeneratorCollins(new HeadRuleSetPTB());
        SRLMEForOneStep.train(corpusFile, modelFile, params, contextGen, encoding, parse, ahg);
	}
}
