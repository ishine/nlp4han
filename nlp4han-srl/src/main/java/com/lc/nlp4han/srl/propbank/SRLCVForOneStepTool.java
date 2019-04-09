package com.lc.nlp4han.srl.propbank;

import java.io.File;
import java.io.IOException;

import com.lc.nlp4han.constituent.AbstractHeadGenerator;
import com.lc.nlp4han.constituent.HeadGeneratorCollins;
import com.lc.nlp4han.constituent.HeadRuleSetPTB;
import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.ml.util.CrossValidationPartitioner;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;
import com.lc.nlp4han.ml.util.ModelWrapper;
import com.lc.nlp4han.ml.util.ObjectStream;
import com.lc.nlp4han.ml.util.TrainingParameters;

/**
 * 一步训练模型的交叉验证类
 * @author 王馨苇
 *
 */
public class SRLCVForOneStepTool {
	private final String languageCode;
    private final TrainingParameters params;
    private SRLEvaluateMonitor[] listeners;
    
    public SRLCVForOneStepTool(String languageCode, TrainingParameters trainParam, SRLEvaluateMonitor... listeners){
    	this.languageCode = languageCode;
        this.params = trainParam;
        this.listeners = listeners;
    }
    
    public void evaluate(ObjectStream<SRLSample<HeadTreeNode>> samples, AbstractParseStrategy<HeadTreeNode> parse, AbstractHeadGenerator ahg, int nFolds, SRLContextGenerator contextGen) throws IOException{
    	CrossValidationPartitioner<SRLSample<HeadTreeNode>> partitioner = new CrossValidationPartitioner<SRLSample<HeadTreeNode>>(samples, nFolds);
		int run = 1;
		//小于折数的时候
		while(partitioner.hasNext()){
			System.out.println("Run"+run+"...");
			CrossValidationPartitioner.TrainingSampleStream<SRLSample<HeadTreeNode>> trainingSampleStream = partitioner.next();
			ModelWrapper model = SRLMEForOneStep.train(languageCode, trainingSampleStream, params, contextGen);
			SRLMEForOneStep tagger = new SRLMEForOneStep(model, contextGen, parse, ahg);
			SRLMeasure measure = new SRLMeasure();
			SRLEvaluatorForOneStep evaluator = new SRLEvaluatorForOneStep(tagger,listeners);
			evaluator.setMeasure(measure);
	        //设置测试集（在测试集上进行评价）
	        evaluator.evaluate(trainingSampleStream.getTestSampleStream());
	        
	        System.out.println(measure);
	        run++;
		}
    }
    
    private static void usage(){
    	System.out.println(SRLCVForOneStepTool.class.getName() + " -data <corpusFile> -parse <parsetype> -encoding <encoding> -type<algorithm>" + 
    "[-cutoff <num>] [-iters <num>] [-folds <nFolds>] ");
    }
    
    public static void main(String[] args) throws IOException {
    	if (args.length < 1)
        {
            usage();
            return;
        }

    	AbstractParseStrategy<HeadTreeNode> parse = null;
        int cutoff = 3;
        int iters = 100;
        int folds = 10;
        File corpusFile = null;
        String parsestr = null;
        String encoding = "UTF-8";
        String type = "MAXENT";
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals("-data"))
            {
                corpusFile = new File(args[i + 1]);
                i++;
            }
            else if (args[i].equals("-parse"))
            {
            	parsestr = args[i + 1];
                i++;
            }
            else if (args[i].equals("-encoding"))
            {
                encoding = args[i + 1];
                i++;
            }
            else if (args[i].equals("-type"))
            {
                type = args[i + 1];
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
            else if (args[i].equals("-folds"))
            {
                folds = Integer.parseInt(args[i + 1]);
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
        
        SRLContextGenerator contextGen = new SRLContextGeneratorConf();
        System.out.println(contextGen);
        
        ObjectStream<String[]> lineStream = new PlainTextByTreeStream(new FileInputStreamFactory(corpusFile), encoding);       
        AbstractHeadGenerator ahg = new HeadGeneratorCollins(new HeadRuleSetPTB());
        ObjectStream<SRLSample<HeadTreeNode>> sampleStream = new SRLSampleStream(lineStream, parse, ahg);
        
        SRLCVForOneStepTool run = new SRLCVForOneStepTool("zh",params);
        run.evaluate(sampleStream, parse, ahg, folds, contextGen);
	}
}
