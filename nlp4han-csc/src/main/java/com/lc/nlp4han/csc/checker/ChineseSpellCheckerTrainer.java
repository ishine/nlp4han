package com.lc.nlp4han.csc.checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.lc.nlp4han.csc.model.AbstractNoisyChannelModel;
import com.lc.nlp4han.csc.model.BCWSNoisyChannelModel;
import com.lc.nlp4han.csc.model.BCWSNoisyChannelModelBasedBigram;
import com.lc.nlp4han.csc.model.BCWSNoisyChannelModelBasedCharacter;
import com.lc.nlp4han.csc.model.BCWSNoisyChannelModelBasedCharacterAndBigram;
import com.lc.nlp4han.csc.model.BCWSNoisyChannelModelBasedCharacterInBalance;
import com.lc.nlp4han.csc.model.DoubleStageNoisyChannelModel;
import com.lc.nlp4han.csc.model.DoubleStageNoisyChannelModelBasedBigram;
import com.lc.nlp4han.csc.model.DoubleStageNoisyChannelModelBasedCharacter;
import com.lc.nlp4han.csc.model.DoubleStageNoisyChannelModelBasedCharacterAndBigram;
import com.lc.nlp4han.csc.model.DoubleStageNoisyChannelModelBasedCharacterInBalance;
import com.lc.nlp4han.csc.model.HUSTNoisyChannelModel;
import com.lc.nlp4han.csc.model.HUSTNoisyChannelModelBasedBigram;
import com.lc.nlp4han.csc.model.HUSTNoisyChannelModelBasedCharacter;
import com.lc.nlp4han.csc.model.HUSTNoisyChannelModelBasedCharacterAndBigram;
import com.lc.nlp4han.csc.model.HUSTNoisyChannelModelBasedCharacterInBalance;
import com.lc.nlp4han.csc.model.SIMDNoisyChannelModel;
import com.lc.nlp4han.csc.model.SIMDNoisyChannelModelBasedBigram;
import com.lc.nlp4han.csc.model.SIMDNoisyChannelModelBasedCharacter;
import com.lc.nlp4han.csc.model.SIMDNoisyChannelModelBasedCharacterAndBigram;
import com.lc.nlp4han.csc.model.SIMDNoisyChannelModelBasedCharacterInBalance;
import com.lc.nlp4han.csc.ngram.NGramModelImpl;
import com.lc.nlp4han.csc.ngram.NGramModel;
import com.lc.nlp4han.csc.util.ConfusionSet;
import com.lc.nlp4han.csc.util.Dictionary;
import com.lc.nlp4han.csc.util.CommonUtils;
import com.lc.nlp4han.csc.wordseg.AbstractWordSegment;
import com.lc.nlp4han.csc.wordseg.WordSegmentNLP4Han;
import com.lc.nlp4han.ml.ngram.model.KneserNeyLanguageModelTrainer;

/**
 *<ul>
 *<li>Description: 中国文拼写纠错模型训练器
 *<li>Company: HUST
 *<li>@author Sonly
 *<li>Date: 2017年11月21日
 *</ul>
 */
public class ChineseSpellCheckerTrainer {
	
	private AbstractNoisyChannelModel noisyChannelModel;
	private NGramModel nGramModel;
	private ConfusionSet confusionSet;
	
	public ChineseSpellCheckerTrainer() {
		
	}
	
	public ChineseSpellCheckerTrainer(NGramModel nGramModel, String method, String charType) throws IOException {
		this(nGramModel, null, method, charType);
	}
	
	public ChineseSpellCheckerTrainer(NGramModel nGramModel, Dictionary dictionary, String method, String charType) throws IOException {
		this.nGramModel = nGramModel;
		
		String path = "com/lc/nlp4han/csc/confusionSet/" + charType + "Pro.utf8";//资源文件中获取混淆集文件
		confusionSet = constructConfusionSet(new File(this.getClass().getClassLoader().getResource(path).getFile()));
		
		selectNoisyChannelModel(dictionary, method, charType);
	}

	/**
	 * 训练中文拼写纠错模型
	 * @return	中文拼写纠错模型
	 */
	public ChineseSpellChecker trainCSCModel() {
		return new ChineseSpellChecker(noisyChannelModel);
	}
	
	/**
	 * 训练噪音通道模型
	 * @param trainCorpus	训练语料
	 * @param encoding		语料编码
	 * @param method		噪音通道模型训练方法
	 * @throws IOException
	 */
	private void selectNoisyChannelModel(Dictionary dictionary, String method, String charType) throws IOException {
		if(nGramModel == null) {
			System.err.println("模型数据缺少语言模型.");
			System.exit(0);
			
		}else if(!method.equals("bcws") && dictionary == null) {
			System.err.println("模型数据缺少字典数据， 已转为“bcws”方法");
			method = "bcws";
		}
		
		AbstractWordSegment wordSegment = null;
		
		switch (method.toLowerCase()) {
		case "ds":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new DoubleStageNoisyChannelModel(dictionary, nGramModel, confusionSet, wordSegment);
			break;
		case "dsc":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new DoubleStageNoisyChannelModelBasedCharacter(dictionary, nGramModel, confusionSet, wordSegment);
			break;
		case "dscib":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new DoubleStageNoisyChannelModelBasedCharacterInBalance(dictionary, nGramModel, confusionSet, wordSegment, charType);
			break;
		case "dsb":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new DoubleStageNoisyChannelModelBasedCharacterAndBigram(dictionary, nGramModel, confusionSet, wordSegment);
			break;
		case "dscb":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new DoubleStageNoisyChannelModelBasedBigram(dictionary, nGramModel, confusionSet, wordSegment);
			break;
		case "hust":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new HUSTNoisyChannelModel(dictionary, nGramModel, confusionSet, wordSegment);
			break;
		case "hustc":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new HUSTNoisyChannelModelBasedCharacter(dictionary, nGramModel, confusionSet, wordSegment);
			break;
		case "hustcib":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new HUSTNoisyChannelModelBasedCharacterInBalance(dictionary, nGramModel, confusionSet, wordSegment, charType);
			break;
		case "hustb":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new HUSTNoisyChannelModelBasedBigram(dictionary, nGramModel, confusionSet, wordSegment);
			break;
		case "hustcb":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new HUSTNoisyChannelModelBasedCharacterAndBigram(dictionary, nGramModel, confusionSet, wordSegment);
			break;
		case "bcws":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new BCWSNoisyChannelModel(nGramModel, confusionSet, wordSegment);
			break;
		case "bcwsc":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new BCWSNoisyChannelModelBasedCharacter(dictionary, nGramModel, confusionSet, wordSegment);
			break;
		case "bcwscib":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new BCWSNoisyChannelModelBasedCharacterInBalance(nGramModel, confusionSet, wordSegment, charType);
			break;
		case "bcwsb":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new BCWSNoisyChannelModelBasedBigram(dictionary, nGramModel, confusionSet, wordSegment);
			break;
		case "bcwscb":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new BCWSNoisyChannelModelBasedCharacterAndBigram(dictionary, nGramModel, confusionSet, wordSegment);
			break;
		case "simd":
			noisyChannelModel = new SIMDNoisyChannelModel(dictionary, nGramModel, confusionSet);
			break;
		case "simdc":
			noisyChannelModel = new SIMDNoisyChannelModelBasedCharacter(dictionary, nGramModel, confusionSet);
			break;
		case "simdcib":
			wordSegment = new WordSegmentNLP4Han();
			noisyChannelModel = new SIMDNoisyChannelModelBasedCharacterInBalance(dictionary, nGramModel, confusionSet, charType);
			break;
		case "simdb":
			noisyChannelModel = new SIMDNoisyChannelModelBasedBigram(dictionary, nGramModel, confusionSet);
			break;
		case "simdcb":
			noisyChannelModel = new SIMDNoisyChannelModelBasedCharacterAndBigram(dictionary, nGramModel, confusionSet);
			break;
		default:
			break;
		}		
	}
	
	/**
	 * 构造基于ngram频数的字典
	 * @param corpus	ngram语料
	 * @param n			ngram的最大长度
	 * @return			基于ngram频数的字典
	 * @throws IOException 
	 */
	public Dictionary constructNGramDict(String trainCorpus, String encoding, int n) throws IOException {
		Dictionary dictionary = new Dictionary();
				
		File file = new File(trainCorpus);
		InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), encoding);
		BufferedReader corpus = new BufferedReader(inputStreamReader);
		
		String line = "";
		while ((line = corpus.readLine()) != null) {
			line = CommonUtils.ToDBC(line.replaceAll("\\s+", "")).trim();
			ArrayList<String> ngrams = new ArrayList<>();
			
			for(int i = 1; i <= n; i++) {
				ngrams = CommonUtils.generateNGrams(line.split(""), i);
				for(String ngram : ngrams) {
					dictionary.add(ngram);
				}
			}			
		}
		corpus.close();

		return dictionary;
	}
	
	/**
	 * 训练n元模型
	 * @param corpus	n元模型语料
	 * @param encoding	语料格式
	 * @param n			ngram的最大长度
	 * @return			ngram模型
	 * @throws IOException	
	 */
	public NGramModelImpl constructLM(String corpus, String encoding, int n) throws IOException {
		StringGramSentenceStream gramSentenceStream = new StringGramSentenceStream(corpus, encoding);
		KneserNeyLanguageModelTrainer trainer = new KneserNeyLanguageModelTrainer(gramSentenceStream, n);

		return new NGramModelImpl(trainer.trainModel());
	}

	/**
	 * 根据困惑集构造困惑矩阵
	 * @param file			混淆集文件
	 * @return				混淆矩阵
	 * @throws IOException
	 */
	private ConfusionSet constructConfusionSet(File similarityPronunciation) throws IOException {
		HashMap<String, HashSet<String>> Pronunciation = new HashMap<>();
		HashMap<String, HashSet<String>> Shape = new HashMap<>();
		ConfusionSet set = new ConfusionSet(Pronunciation, Shape);
	
		InputStreamReader pronunciation = new InputStreamReader(new FileInputStream(similarityPronunciation), "utf-8");
		BufferedReader proReader = new BufferedReader(pronunciation);
		String line = "";
		try {
			while ((line = proReader.readLine())!= null) {
				line = CommonUtils.ToDBC(line);		//全角转为半角
				line = line.replace("\t", "").trim();	//去除多于的空格
				if(!line.equals("")) {					//过滤空行
					String[] strings = line.split("");
					set.addSimilarityPronunciations(strings);
				}
			}
			proReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}
}