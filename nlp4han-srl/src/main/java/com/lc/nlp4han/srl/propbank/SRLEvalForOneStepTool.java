package com.lc.nlp4han.srl.propbank;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.lc.nlp4han.constituent.AbstractHeadGenerator;
import com.lc.nlp4han.constituent.HeadGeneratorCollins;
import com.lc.nlp4han.constituent.HeadRuleSetPTB;
import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;
import com.lc.nlp4han.ml.util.ModelWrapper;
import com.lc.nlp4han.ml.util.ObjectStream;
import com.lc.nlp4han.ml.util.TrainingParameters;

/**
 * 一步训练模型的评估类
 * @author 王馨苇
 *
 */
public class SRLEvalForOneStepTool {
	
	private static void usage(){
		System.out.println(SRLEvalForOneStepTool.class.getName() + 
				"-data <corpusFile> -type <algorithom> -parse <parsetype>"
				+ "-gold <goldFile> -error <errorFile> -encoding <encoding>" + " [-cutoff <num>] [-iters <num>]");
	}
	
	public static void eval(File trainFile, AbstractParseStrategy<HeadTreeNode> parse, TrainingParameters params, File goldFile, String encoding, File errorFile) throws IOException{
		long start = System.currentTimeMillis();
		SRLContextGenerator contextGen = new SRLContextGeneratorConf();
        AbstractHeadGenerator ahg = new HeadGeneratorCollins(new HeadRuleSetPTB());
        
		ModelWrapper model = SRLMEForOneStep.train(trainFile, params, contextGen, encoding, parse, ahg);		
        System.out.println("训练时间： " + (System.currentTimeMillis() - start));
        
        SRLMEForOneStep tagger = new SRLMEForOneStep(model, contextGen, parse, ahg);
        SRLMeasure measure = new SRLMeasure();
        SRLEvaluatorForOneStep evaluator = null;
        SRLErrorPrinter printer = null;
        
        if(errorFile != null){
        	System.out.println("Print error to file " + errorFile);
        	printer = new SRLErrorPrinter(new FileOutputStream(errorFile));    	
        	evaluator = new SRLEvaluatorForOneStep(tagger,printer);
        }else{
        	evaluator = new SRLEvaluatorForOneStep(tagger);
        }
        evaluator.setMeasure(measure);
        ObjectStream<String[]> testlinesStream = new PlainTextByTreeStream(new FileInputStreamFactory(goldFile), encoding);
        ObjectStream<SRLSample<HeadTreeNode>> testsampleStream = new SRLSampleStream(testlinesStream, parse, ahg);        
        evaluator.evaluate(testsampleStream);
        
        SRLMeasure measureRes = evaluator.getMeasure();
        System.out.println("标注时间： " + (System.currentTimeMillis() - start));
        System.out.println(measureRes);
	}
	
	public static void main(String[] args) throws IOException {
		if (args.length < 1){
            usage();
            return;
        }
		AbstractParseStrategy<HeadTreeNode> parse = null;
        String trainFile = null;
        String goldFile = null;
        String errorFile = null;
        String encoding = "UTF-8";
        String type = "MAXENT";
        String parsestr = "";
        int cutoff = 3;
        int iters = 100;
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-data"))
            {
                trainFile = args[i + 1];
                i++;
            }
            else if (args[i].equals("-type"))
            {
                type = args[i + 1];
                i++;
            }
            else if (args[i].equals("-parse"))
            {
                parsestr = args[i + 1];
                i++;
            }
            else if (args[i].equals("-gold"))
            {
                goldFile = args[i + 1];
                i++;
            }
            else if (args[i].equals("-error"))
            {
                errorFile = args[i + 1];
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
        if (errorFile != null)
        {
            eval(new File(trainFile), parse, params, new File(goldFile), encoding, new File(errorFile));
        }
        else
            eval(new File(trainFile), parse, params, new File(goldFile), encoding, null);
	}
}
