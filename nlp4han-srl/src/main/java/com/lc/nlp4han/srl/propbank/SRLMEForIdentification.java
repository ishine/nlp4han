package com.lc.nlp4han.srl.propbank;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lc.nlp4han.constituent.AbstractHeadGenerator;
import com.lc.nlp4han.constituent.HeadTreeNode;
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
import com.lc.nlp4han.ml.util.TrainerFactory;
import com.lc.nlp4han.ml.util.TrainerFactory.TrainerType;
import com.lc.nlp4han.ml.util.TrainingParameters;

/**
 * 论元识别模型的训练
 * @author 王馨苇
 *
 */
public class SRLMEForIdentification {

	public static final int DEFAULT_BEAM_SIZE = 15;
	private SRLContextGenerator contextGenerator;
	private int size;
	private SequenceClassificationModel<TreeNodeWrapper<HeadTreeNode>> model;
    private SequenceValidator<TreeNodeWrapper<HeadTreeNode>> sequenceValidator;	
    
	/**
	 * 构造函数，初始化工作
	 * @param model 模型
	 * @param contextGen 特征
	 * @param parse 解析文本类
	 * @param ahg 生成头结点的方法
	 */
	public SRLMEForIdentification(ModelWrapper model, SRLContextGenerator contextGen) {
		init(model , contextGen);
	}
    /**
     * 初始化工作
     * @param model 模型
     * @param contextGen 特征
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void init(ModelWrapper model, SRLContextGenerator contextGen) {
		
		int beamSize = SRLMEForIdentification.DEFAULT_BEAM_SIZE;
        contextGenerator = contextGen;
        size = beamSize;
        sequenceValidator = new DefaultSRLSequenceValidator();
        this.model = new BeamSearch(beamSize, model.getModel(), 0);
	}

	/**
	 * 训练模型
	 * @param file 训练文件
	 * @param params 训练
	 * @param contextGen 特征
	 * @param encoding 编码
	 * @param parse 解析文本类
	 * @param ahg 生成头结点的方法
	 * @return
	 */
	public static ModelWrapper train(File file, TrainingParameters params, SRLContextGenerator contextGen,
			String encoding, AbstractParseStrategy<HeadTreeNode> parse, AbstractHeadGenerator ahg){
		ModelWrapper model = null;
		
		try {
			ObjectStream<String[]> lineStream = new PlainTextByTreeStream(new FileInputStreamFactory(file), encoding);
			ObjectStream<SRLSample<HeadTreeNode>> sampleStream = new SRLSampleStream(lineStream, parse, ahg);
			model = train("zh", sampleStream, params, contextGen);
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
	 * @param parse 解析文本类
	 * @param ahg 生成头结点的方法
	 * @return
	 */
	public static ModelWrapper train(File file, File modelFile, TrainingParameters params,
			SRLContextGenerator contextGen, String encoding, AbstractParseStrategy<HeadTreeNode> parse, AbstractHeadGenerator ahg) {
		OutputStream modelOut = null;
		ModelWrapper model = null;
		
		try {
			ObjectStream<String[]> lineStream = new PlainTextByTreeStream(new FileInputStreamFactory(file), encoding);
			ObjectStream<SRLSample<HeadTreeNode>> sampleStream = new SRLSampleStream(lineStream, parse, ahg);
			model = train("zh", sampleStream, params, contextGen);
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
	 * @param encoding 编码
	 * @return
	 * @throws IOException  
	 */
	public static ModelWrapper train(String languageCode, ObjectStream<SRLSample<HeadTreeNode>> sampleStream, TrainingParameters params,
			SRLContextGenerator contextGen) throws IOException {
		String beamSizeString = params.getSettings().get(BeamSearch.BEAM_SIZE_PARAMETER);
		int beamSize = SRLMEForIdentification.DEFAULT_BEAM_SIZE;
        if (beamSizeString != null) {
            beamSize = Integer.parseInt(beamSizeString);
        }
        
        ClassificationModel SRLModel = null;
        Map<String, String> manifestInfoEntries = new HashMap<String, String>();
        TrainerType trainerType = TrainerFactory.getTrainerType(params.getSettings());
        
        if (TrainerType.EVENT_MODEL_TRAINER.equals(trainerType)) {
            ObjectStream<Event> es = new SRLEventStreamForIdentification(sampleStream, contextGen);
            EventTrainer trainer = TrainerFactory.getEventTrainer(params.getSettings(), manifestInfoEntries);
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
        return model.bestSequences(1, argumentree, predicatetree, contextGenerator, sequenceValidator)[0];
    }
	
	/**
	 * 得到最好的结果字符串
	 * @param argumentree 论元子树序列
	 * @param predicatetree 谓词树
	 * @return
	 */
	public String[] tag(TreeNodeWrapper<HeadTreeNode>[] argumentree, Object[] predicatetree){
		Sequence sequence = model.bestSequence(argumentree, predicatetree, contextGenerator, sequenceValidator);
		List<String> outcome = sequence.getOutcomes();
		return outcome.toArray(new String[outcome.size()]);
	}
	
	/**
	 * 得到最好的结果序列
	 * @param argumentree 论元子树序列
	 * @param predicatetree 谓词树
	 * @return
	 */
	public Sequence[] topKSequences(TreeNodeWrapper<HeadTreeNode>[] argumentree, Object[] predicatetree) {
        return model.bestSequences(size, argumentree, predicatetree, contextGenerator, sequenceValidator);
    }
}
