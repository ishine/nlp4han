package com.lc.nlp4han.srl.propbank;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.lc.nlp4han.constituent.AbstractHeadGenerator;
import com.lc.nlp4han.constituent.BracketExpUtil;
import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreePreprocessTool;
import com.lc.nlp4han.ml.model.ClassificationModel;
import com.lc.nlp4han.ml.model.Event;
import com.lc.nlp4han.ml.model.SequenceClassificationModel;
import com.lc.nlp4han.ml.util.BeamSearch;
import com.lc.nlp4han.ml.util.EventTrainer;
import com.lc.nlp4han.ml.util.FileInputStreamFactory;
import com.lc.nlp4han.ml.util.ModelWrapper;
import com.lc.nlp4han.ml.util.ObjectStream;
import com.lc.nlp4han.ml.util.Sequence;
import com.lc.nlp4han.ml.util.SequenceValidator;
import com.lc.nlp4han.ml.util.TrainerFactory.TrainerType;
import com.lc.nlp4han.ml.util.TrainingParameters;
import com.lc.nlp4han.ml.util.TrainerFactory;

/**
 * 论元分类模型的训练(包含NULL标记)
 * @author 王馨苇
 *
 */
public class SRLMEForClassificationWithNull implements SemanticRoleLabeler{

	public static final int DEFAULT_BEAM_SIZE = 15;
	private SRLContextGenerator contextGeneratorClas;
	private SRLContextGenerator contextGeneratorIden;
	private SequenceClassificationModel<TreeNodeWrapper<HeadTreeNode>> modelClas;
	private SequenceClassificationModel<TreeNodeWrapper<HeadTreeNode>> modelIden;
    private SequenceValidator<TreeNodeWrapper<HeadTreeNode>> sequenceValidator;
    private AbstractParseStrategy<HeadTreeNode> parse; 
    private AbstractHeadGenerator ahg;

	public SRLMEForClassificationWithNull(ModelWrapper modelIden, ModelWrapper modelClas, SRLContextGenerator contextGenIden, SRLContextGenerator contextGenClas, AbstractParseStrategy<HeadTreeNode> parse, AbstractHeadGenerator ahg) {
		init(modelIden, modelClas, contextGenIden, contextGenClas);
		
	}
    /**
     * 初始化工作
     * @param model 模型
     * @param contextGen 特征
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void init(ModelWrapper modelIden, ModelWrapper modelClas, SRLContextGenerator contextGenIden, SRLContextGenerator contextGenClas) {
		sequenceValidator = new DefaultSRLSequenceValidator();
		
		int beamSizeClas = SRLMEForClassificationWithNull.DEFAULT_BEAM_SIZE;
        contextGeneratorClas = contextGenClas;       
        this.modelClas = new BeamSearch(beamSizeClas,
        			modelClas.getModel(), 0);
        
        int beamSizeIden = SRLMEForIdentification.DEFAULT_BEAM_SIZE;
        contextGeneratorIden = contextGenIden;
        this.modelIden = new BeamSearch(beamSizeIden,
        			modelIden.getModel(), 0);		
	}

	/**
	 * 训练模型
	 * @param file 训练文件
	 * @param params 训练
	 * @param contextGen 特征
	 * @param encoding 编码
	 * @param tagger 识别阶段训练出来的模型和特征类
	 * @param parse 样本解析类
	 * @param ahg 头结点生成类
	 * @return 模型和模型信息的包裹结果
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static ModelWrapper train(File file, TrainingParameters params, SRLContextGenerator contextGen,
			String encoding, SRLMEForIdentification tagger, AbstractParseStrategy<HeadTreeNode> parse, AbstractHeadGenerator ahg){
		ModelWrapper model = null;
		
		try {
			ObjectStream<String[]> lineStream = new PlainTextByTreeStream(new FileInputStreamFactory(file), encoding);
			ObjectStream<SRLSample<HeadTreeNode>> sampleStream = new SRLSampleStream(lineStream, parse, ahg);
			model = SRLMEForClassificationWithNull.train("zh", sampleStream, params, contextGen, tagger);
			return model;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 训练模型，并将模型写出
	 * @param file 训练的文本
	 * @param modelFile 模型文件
	 * @param params 训练的参数配置
	 * @param contextGen 上下文 产生器
	 * @param encoding 编码方式
	 * @param parse 样本解析类
	 * @param ahg 头结点生成类
	 * @param tagger 识别阶段训练出来的模型和特征类
	 * @return
	 */
	public static ModelWrapper train(File file, File modelFile, TrainingParameters params,
			SRLContextGenerator contextGen, String encoding, SRLMEForIdentification tagger, AbstractParseStrategy<HeadTreeNode> parse, AbstractHeadGenerator ahg) {
		OutputStream modelOut = null;
		ModelWrapper model = null;
		
		try {
			ObjectStream<String[]> lineStream = new PlainTextByTreeStream(new FileInputStreamFactory(file), encoding);
			ObjectStream<SRLSample<HeadTreeNode>> sampleStream = new SRLSampleStream(lineStream, parse, ahg);
			model = SRLMEForClassificationWithNull.train("zh", sampleStream, params, contextGen, tagger);
            //模型的写出，文本文件
            modelOut = new BufferedOutputStream(new FileOutputStream(modelFile));           
            model.serialize(modelOut);
            
            return model;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {			
            if (modelOut != null) {
                try {
                	modelOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }	
		
		return null;
	}
	
	/**
	 * 训练模型
	 * @param languageCode 编码
	 * @param sampleStream 文件流
	 * @param contextGen 特征
	 * @param tagger 识别阶段训练出来的模型和特征类
	 * @return 模型和模型信息的包裹结果
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static ModelWrapper train(String languageCode, ObjectStream<SRLSample<HeadTreeNode>> sampleStream, TrainingParameters params,
			SRLContextGenerator contextGen, SRLMEForIdentification tagger) throws IOException {
		String beamSizeString = params.getSettings().get(BeamSearch.BEAM_SIZE_PARAMETER);
		int beamSize = SRLMEForClassificationWithNull.DEFAULT_BEAM_SIZE;
        if (beamSizeString != null) {
            beamSize = Integer.parseInt(beamSizeString);
        }
        
        ClassificationModel SRLModel = null;
        Map<String, String> manifestInfoEntries = new HashMap<String, String>();
        TrainerType trainerType = TrainerFactory.getTrainerType(params.getSettings());
        
        if (TrainerType.EVENT_MODEL_TRAINER.equals(trainerType)) {
            ObjectStream<Event> es = new SRLEventStreamForClassificationWithNull(sampleStream, contextGen, tagger);
            EventTrainer trainer = TrainerFactory.getEventTrainer(params.getSettings(),
                    manifestInfoEntries);
            SRLModel = trainer.train(es);                       
        }

        return new ModelWrapper(SRLModel, beamSize);
	}
	
	/**
	 * 得到最好的结果序列
	 * @param argumentree 论元子树序列
	 * @param predicatetree 谓词树
	 * @return
	 */
	public Sequence topSequences(TreeNodeWrapper<HeadTreeNode>[] argumentree, Object[] predicatetree) {
        return modelClas.bestSequences(1, argumentree, predicatetree, contextGeneratorClas, sequenceValidator)[0];
    }
	
	/**
	 * 得到最好的结果字符串
	 * @param argumentree 论元子树序列
	 * @param predicatetree 谓词树
	 * @return
	 */
	public String[] tag(TreeNodeWrapper<HeadTreeNode>[] argumentree, Object[] predicatetree){
		Sequence sequence = modelClas.bestSequence(argumentree, predicatetree, contextGeneratorClas, sequenceValidator);
		List<String> outcome = sequence.getOutcomes();
		return outcome.toArray(new String[outcome.size()]);
	}
	
	/**
	 * 得到最好的k个识别结果
	 * @param k 最好的结果数
	 * @param argumentree 论元子树序列
	 * @param predicatetree 谓词树
	 * @return
	 */
	public Sequence[] topKSequencesForIden(int k, TreeNodeWrapper<HeadTreeNode>[] argumentree, Object[] predicatetree) {
        return modelIden.bestSequences(k, argumentree, predicatetree, contextGeneratorIden, sequenceValidator);
    }
	
	/**
	 * 得到最好的识别结果
	 * @param headtree 子树序列
	 * @param semanticinfo 语义信息
	 * @return
	 */
	public Sequence topSequencesForIden(TreeNodeWrapper<HeadTreeNode>[] argumentree, Object[] predicatetree) {
        return modelIden.bestSequences(1, argumentree, predicatetree, contextGeneratorIden, sequenceValidator)[0];
    }
	
	/**
	 * 得到最好的识别结果
	 * @param argumentree 论元子树序列
	 * @param predicatetree 谓词树
	 * @return
	 */
	public String[] tagForIden(TreeNodeWrapper<HeadTreeNode>[] argumentree, Object[] predicatetree){
		Sequence sequence = modelIden.bestSequence(argumentree, predicatetree, contextGeneratorIden, sequenceValidator);
		List<String> outcome = sequence.getOutcomes();
		return outcome.toArray(new String[outcome.size()]);
	}
	
	/**
	 * 得到最好的k个结果序列
	 * @param k 最好的结果数
	 * @param argumentree 论元子树序列
	 * @param predicatetree 谓词树
	 * @return
	 */
	public Sequence[] topKSequences(int k, TreeNodeWrapper<HeadTreeNode>[] argumentree, Object[] predicatetree) {
        return modelClas.bestSequences(k, argumentree, predicatetree, contextGeneratorClas, sequenceValidator);
    }
	

	/**
	 * 得到一棵树的语义角色标注
	 * @param tree 句法分析得到的树
	 * @param predicateinfo 谓词下标信息
	 * @return
	 */
	@Override
	public SRLTree srltree(TreeNode tree, int[] predicateinfo) {
		return kSrltree(1, tree, predicateinfo)[0];
	}
	
	/**
	 * 得到一棵树的语义角色标注
	 * @param tree 句法分析得到的树的括号表达式形式
	 * @param predicateinfo 谓词信息
	 * @return
	 */
	@Override
	public SRLTree srltree(String treeStr, int[] predicateinfo) {
		TreeNode node = BracketExpUtil.generateTreeNoTopBracket("("+treeStr+")");
		return srltree(node,predicateinfo);
	}
	
	/**
	 * 得到一棵树最好的K个角色标注
	 * @param k 最好的结果数
	 * @param tree 句法分析得到的树
	 * @param predicateinfo 谓词信息
	 * @return
	 */
	@Override
	public SRLTree[] kSrltree(int k, TreeNode tree, int[] predicateinfo) {
		SRLSample<HeadTreeNode> sample = null;
		TreePreprocessTool.deleteNone(tree);
		String str = SRLSample.indexToTrainSample(predicateinfo);
        sample = parse.parse(tree, str, ahg);
        List<SRLTree> srllist = new ArrayList<>();
        Sequence[] sequence = topKSequencesForIden(k, sample.getArgumentTree(), sample.getPredicateTree());
        TreeMap<Sequence,TreeNodeWrapper<HeadTreeNode>[]> result = new TreeMap<>(); 
        
        for (int i = 0; i < sequence.length; i++) {
        	String[] newlabelinfo = sequence[i].getOutcomes().toArray(new String[sequence[i].getOutcomes().size()]);
    		List<Integer> index = SRLSample.filterNotNULLLabelIndex(newlabelinfo);
    		TreeNodeWrapper<HeadTreeNode>[] argumenttree = SRLSample.getArgumentTreeFromIndex(sample.getArgumentTree(), index);
    		Sequence[] res = topKSequences(k, argumenttree, sample.getPredicateTree());
    		for (int j = 0; j < res.length; j++) {
				result.put(res[i], argumenttree);
			}
        }   
        
        for (Sequence s : result.keySet()) {
        	SRLTreeNode srltreenode = TreeToSRLTree.treeToSRLTree(tree, result.get(s), s.getOutcomes().toArray(new String[s.getOutcomes().size()]));
    		SRLTree srltree = new SRLTree();
    		srltree.setSRLTree(srltreenode);
    		srllist.add(srltree);
		}
        
		return srllist.toArray(new SRLTree[srllist.size()]);
	}
	
	/**
	 * 得到一棵树最好的K个角色标注
	 * @param k 最好的结果数
	 * @param treeStr 句法分析得到的树的括号表示
	 * @param predicateinfo 谓词下标信息
	 * @return
	 */
	@Override
	public SRLTree[] kSrltree(int k, String treeStr, int[] predicateinfo) {
		TreeNode node = BracketExpUtil.generateTreeNoTopBracket("("+treeStr+")");
		return kSrltree(k, node, predicateinfo);
	}
	
	/**
	 * 得到一棵树的语义角色标注的中括号表达式形式
	 * @param tree 句法分析得到的树
	 * @param predicateinfo 谓词下标信息
	 * @return
	 */
	@Override
	public String srlstr(TreeNode tree, int[] predicateinfo) {
		return kSrlstr(1, tree, predicateinfo)[0];
	}
	
	/**
	 * 得到一棵树的语义角色标注的中括号表达式形式
	 * @param treeStr 句法分析得到的树的括号表达式形式
	 * @param predicateinfo 谓词下标信息
	 * @return
	 */
	@Override
	public String srlstr(String treeStr, int[] predicateinfo) {
		TreeNode node = BracketExpUtil.generateTreeNoTopBracket("("+treeStr+")");
		return srlstr(node, predicateinfo);
	}
	
	/**
	 * 得到一棵树最好的K个角色标注的中括号表达式形式
	 * @param k 最好的结果数
	 * @param tree 句法分析得到的树
	 * @param predicateinfo 谓词下标信息
	 * @return
	 */
	@Override
	public String[] kSrlstr(int k, TreeNode tree, int[] predicateinfo) {
		SRLTree[] srltree = kSrltree(k, tree, predicateinfo);
		String[] output = new String[srltree.length];
		
 		for (int i = 0; i < srltree.length; i++) {
			String str = SRLTreeNode.printSRLBracket(srltree[i].getSRLTreeRoot());
			output[i] = str;
		}
 		
		return output;
	}
	
	/**
	 * 得到一棵树最好的K个角色标注的中括号表达式形式
	 * @param k 最好的结果数
	 * @param treeStr 句法分析得到的树的括号表达式形式
	 * @param predicateinfo 谓词下标信息
	 * @return
	 */
	@Override
	public String[] kSrlstr(int k, String treeStr, int[] predicateinfo) {
		TreeNode node = BracketExpUtil.generateTreeNoTopBracket("("+treeStr+")");
		return kSrlstr(k, node, predicateinfo);
	}
	
	/**
	 * 得到一棵树最好的角色标注
	 * @param treeStr 句法分析得到的树
	 * @param predicateinfo 谓词信息
	 * @return
	 */
	@Override
	public SRLTree srltree(TreeNode tree, String[] predicateinfo) {
		return kSrltree(1, tree, predicateinfo)[0];
	}
	
	/**
	 * 得到一棵树最好的角色标注
	 * @param treeStr 句法分析得到的树的括号表达式形式
	 * @param predicateinfo 谓词信息
	 * @return
	 */
	@Override
	public SRLTree srltree(String treeStr, String[] predicateinfo) {
		TreeNode tree = BracketExpUtil.generateTreeNoTopBracket("("+treeStr+")");
		return srltree(tree, predicateinfo);
	}
	
	/**
	 * 得到一棵树最好的K个角色标注
	 * @param k 最好的结果数
	 * @param tree 句法分析得到的树
	 * @param predicateinfo 谓词信息
	 * @return
	 */
	@Override
	public SRLTree[] kSrltree(int k, TreeNode tree, String[] predicateinfo) {
		int[] index = SRLSample.getPredicateIndex(tree, predicateinfo);
		return kSrltree(k, tree, index);
	}
	
	/**
	 * 得到一棵树最好的K个角色标注
	 * @param k 最好的结果数
	 * @param treeStr 句法分析得到的树的括号表达式形式
	 * @param predicateinfo 谓词信息
	 * @return
	 */
	@Override
	public SRLTree[] kSrltree(int k, String treeStr, String[] predicateinfo) {
		TreeNode tree = BracketExpUtil.generateTreeNoTopBracket("("+treeStr+")");
		return kSrltree(k, tree, predicateinfo);
	}
	
	/**
	 * 得到一棵树最好的角色标注的中括号表达式形式
	 * @param tree 句法分析得到的树
	 * @param predicateinfo 谓词信息
	 * @return
	 */
	@Override
	public String srlstr(TreeNode tree, String[] predicateinfo) {
		return kSrlstr(1, tree, predicateinfo)[0];
	}
	
	/**
	 * 得到一棵树最好的角色标注的中括号表达式形式
	 * @param treeStr 句法分析得到的树的括号表达式形式
	 * @param predicateinfo 谓词信息
	 * @return
	 */
	@Override
	public String srlstr(String treeStr, String[] predicateinfo) {
		TreeNode tree = BracketExpUtil.generateTreeNoTopBracket("("+treeStr+")");
		return srlstr(tree, predicateinfo);
	}
	
	/**
	 * 得到一棵树最好的K个角色标注的中括号表达式形式
	 * @param k 最好的结果数
	 * @param tree 句法分析得到的树
	 * @param predicateinfo 谓词信息
	 * @return
	 */
	@Override
	public String[] kSrlstr(int k, TreeNode tree, String[] predicateinfo) {
		int[] index = SRLSample.getPredicateIndex(tree, predicateinfo);
		return kSrlstr(k, tree, index);
	}
	
	/**
	 * 得到一棵树最好的K个角色标注的中括号表达式形式
	 * @param k 最好的结果数
	 * @param treeStr 句法分析得到的树的括号表达式形式
	 * @param predicateinfo 谓词信息
	 * @return
	 */
	@Override
	public String[] kSrlstr(int k, String treeStr, String[] predicateinfo) {
		TreeNode tree = BracketExpUtil.generateTreeNoTopBracket("("+treeStr+")");
		return kSrlstr(k, tree, predicateinfo);
	}
}
