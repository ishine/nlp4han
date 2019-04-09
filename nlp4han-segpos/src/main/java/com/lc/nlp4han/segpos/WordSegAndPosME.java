package com.lc.nlp4han.segpos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lc.nlp4han.ml.model.ClassificationModel;
import com.lc.nlp4han.ml.model.Event;
import com.lc.nlp4han.ml.model.SequenceClassificationModel;
import com.lc.nlp4han.ml.util.BeamSearch;
import com.lc.nlp4han.ml.util.EventTrainer;
import com.lc.nlp4han.ml.util.ModelWrapper;
import com.lc.nlp4han.ml.util.ObjectStream;
import com.lc.nlp4han.ml.util.Sequence;
import com.lc.nlp4han.ml.util.SequenceValidator;
import com.lc.nlp4han.ml.util.TrainerFactory;
import com.lc.nlp4han.ml.util.TrainerFactory.TrainerType;
import com.lc.nlp4han.ml.util.TrainingParameters;

/**
 * 训练模型，标记序列
 * 
 * @author 刘小峰
 * @author 王馨苇
 *
 */
public class WordSegAndPosME implements WordSegAndPos
{

	public static final int DEFAULT_BEAM_SIZE = 8;
	private WordSegAndPosContextGenerator contextGenerator;
	private int size;
	private Sequence bestSequence;
	private SequenceClassificationModel<String> model;

	private SequenceValidator<String> sequenceValidator;

	public WordSegAndPosME(File model) throws IOException
	{
		this(new ModelWrapper(model), new WordSegAndPosContextGeneratorConf());
	}

	public WordSegAndPosME(File model, WordSegAndPosContextGenerator contextGen) throws IOException
	{
		this(new ModelWrapper(model), contextGen);
	}

	public WordSegAndPosME(ModelWrapper model) throws IOException
	{
		init(model, new WordSegAndPosContextGeneratorConf());

	}

	/**
	 * 构造函数，初始化工作
	 * 
	 * @param model
	 *            模型
	 * @param contextGen
	 *            特征
	 */
	public WordSegAndPosME(ModelWrapper model, WordSegAndPosContextGenerator contextGen)
	{
		init(model, contextGen);
	}

	/**
	 * 初始化工作
	 * 
	 * @param model
	 *            模型
	 * @param contextGen
	 *            特征
	 */
	private void init(ModelWrapper model, WordSegAndPosContextGenerator contextGen)
	{
		int beamSize = WordSegAndPosME.DEFAULT_BEAM_SIZE;

		contextGenerator = contextGen;
		size = beamSize;
		sequenceValidator = new DefaultWordSegAndPosSequenceValidator();

		this.model = model.getSequenceModel();
	}

	/**
	 * 训练模型
	 * 
	 * @param languageCode
	 *            编码
	 * @param sampleStream
	 *            文件流
	 * @param contextGen
	 *            特征
	 * @param encoding
	 *            编码
	 * @return 模型和模型信息的包裹结果
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static ModelWrapper train(String languageCode, ObjectStream<WordSegAndPosSample> sampleStream,
			TrainingParameters params, WordSegAndPosContextGenerator contextGen) throws IOException
	{
		String beamSizeString = params.getSettings().get(BeamSearch.BEAM_SIZE_PARAMETER);
		int beamSize = WordSegAndPosME.DEFAULT_BEAM_SIZE;
		if (beamSizeString != null)
		{
			beamSize = Integer.parseInt(beamSizeString);
		}
		ClassificationModel posModel = null;
		Map<String, String> manifestInfoEntries = new HashMap<String, String>();
		// event_model_trainer
		TrainerType trainerType = TrainerFactory.getTrainerType(params.getSettings());
		SequenceClassificationModel<String> seqPosModel = null;
		if (TrainerType.EVENT_MODEL_TRAINER.equals(trainerType))
		{
			// sampleStream为PhraseAnalysisSampleStream对象
			ObjectStream<Event> es = new WordSegAndPosSampleEventStream(sampleStream, contextGen);
			EventTrainer trainer = TrainerFactory.getEventTrainer(params.getSettings(), manifestInfoEntries);
			posModel = trainer.train(es);
		}

		if (posModel != null)
		{
			return new ModelWrapper(posModel, beamSize);
		}
		else
		{
			return new ModelWrapper(seqPosModel);
		}
	}

	public String[] tagAll(String[] characters, Object[] additionaContext)
	{
		bestSequence = model.bestSequence(characters, additionaContext, contextGenerator, sequenceValidator);
		// System.out.println(bestSequence);
		List<String> t = bestSequence.getOutcomes();
		return t.toArray(new String[t.size()]);
	}

	/**
	 * 对生语料进行词性标记
	 * 
	 * @param sentence
	 *            一句完整的话
	 * @return
	 */
	@Override
	public String[] segmentAndTag(String sentence, char wordPosSepChar)
	{
		String[] characters = new String[sentence.length()];
		for (int i = 0; i < sentence.length(); i++)
		{
			characters[i] = sentence.charAt(i) + "";
		}
		return wordsegandpos(characters, wordPosSepChar);
	}

	/**
	 * 对生语料进行词性标记
	 * 
	 * @param characters
	 *            生语料切成的一个个字
	 * @return 得到分词加词性标记的结果
	 */
	public String[] wordsegandpos(String[] characters, char wordPosSepChar)
	{
		String[] characterandpos = tagAll(characters);
		String[] poses = WordSegAndPosSample.toPos(characterandpos);
		String[] words = WordSegAndPosSample.toWord(characters, characterandpos);
		String[] res = new String[words.length];
		for (int i = 0; i < words.length && i < poses.length; i++)
		{
			res[i] = words[i] + wordPosSepChar + poses[i];
		}
		return res;
	}

	/**
	 * 得到的最好的分词加标注的序列
	 * 
	 * @param words
	 *            字数组
	 * @return
	 */
	public String[] tagAll(String[] characters)
	{
		String[] characterandpos = this.tagAll(characters, null);
		return characterandpos;
	}

	/**
	 * 得到最好的numTaggings个标记序列
	 * 
	 * @param characters
	 *            一个个字
	 * @param numTaggings
	 *            个数
	 * @return 分词加词性标注的序列
	 */
	public String[][] tagAll(String[] characters, int numTaggings)
	{
		Sequence[] bestSequences = model.bestSequences(numTaggings, characters, null, contextGenerator,
				sequenceValidator);
		String[][] tagsandposes = new String[bestSequences.length][];
		for (int si = 0; si < tagsandposes.length; si++)
		{
			List<String> t = bestSequences[si].getOutcomes();
			tagsandposes[si] = t.toArray(new String[t.size()]);

		}
		return tagsandposes;
	}

	/**
	 * 最好的K个序列
	 * 
	 * @param characters
	 *            一个个字
	 * @return
	 */
	public Sequence[] topKSequences(String[] characters)
	{
		return this.topKSequences(characters, null);
	}

	/**
	 * 最好的K个序列
	 * 
	 * @param characters
	 *            一个个字
	 * @param additionaContext
	 * @return
	 */
	public Sequence[] topKSequences(String[] characters, Object[] additionaContext)
	{
		return model.bestSequences(size, characters, additionaContext, contextGenerator, sequenceValidator);
	}

	/**
	 * 生成词语-词性词典
	 * 
	 * @param samples
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, List<String>> bulidDictionary(ObjectStream<WordSegAndPosSample> samples)
			throws IOException
	{
		// 词语-词性词典【键为词，值为词性】，一个词语可能有多个词性
		HashMap<String, List<String>> dict = new HashMap<String, List<String>>();
		WordSegAndPosSample sample = null;
		while ((sample = samples.read()) != null)
		{
			String[] words = sample.getWords();
			String[] poses = sample.getPoses();

			for (int i = 0; i < words.length; i++)
			{
				// 是否包含这个词语
				if (dict.containsKey(words[i]))
				{// 包含这个词语
					// 看是否包含这个词性
					if (dict.get(words[i]).contains(poses[i]))
					{// 包含这个词性，什么也不做

					}
					else
					{// 未包含这个词性，就加入
						dict.get(words[i]).add(poses[i]);
						dict.put(words[i], dict.get(words[i]));
					}
				}
				else
				{// 不包含这个词语
					List<String> list = new ArrayList<String>();
					list.add(poses[i]);
					dict.put(words[i], list);
				}
			}
		}

		return dict;
	}

	/**
	 * 
	 * @param file
	 *            训练文件
	 * @param encoding
	 *            编码方式
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String, List<String>> buildDictionary(File file, String encoding) throws IOException
	{
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		String sentences;
		HashMap<String, List<String>> dict = new HashMap<String, List<String>>();
		while ((sentences = data.readLine()) != null)
		{
			if (sentences.compareTo("") != 0)
			{
				String wordsandposes[] = sentences.split("\\s+");
				for (int i = 1; i < wordsandposes.length; i++)
				{
					String[] wordanspos = wordsandposes[i].split("/");
					String word = wordanspos[0];
					String pos = wordanspos[1];
					// 是否包含这个词语
					if (dict.containsKey(word))
					{// 包含这个词语
						// 看是否包含这个词性
						if (dict.get(word).contains(pos))
						{// 包含这个词性，什么也不做

						}
						else
						{// 未包含这个词性，就加入
							dict.get(word).add(pos);
							dict.put(word, dict.get(word));
						}
					}
					else
					{// 不包含这个词语
						List<String> list = new ArrayList<String>();
						list.add(pos);
						dict.put(word, list);
					}
				}
			}
		}

		data.close();

		return dict;
	}

	/**
	 * 对生语料进行分词
	 * 
	 * @param sentence
	 *            生语料
	 * @return 得到分词的结果
	 */
	@Override
	public String[] segment(String sentence)
	{
		String[] characters = new String[sentence.length()];
		for (int i = 0; i < sentence.length(); i++)
		{
			characters[i] = sentence.charAt(i) + "";
		}
		return wordseg(characters);
	}

	/**
	 * 对生语料进行分词
	 * 
	 * @param characters
	 *            生语料分解成的一个个字
	 * @return 得到分词的结果
	 */
	public String[] wordseg(String[] characters)
	{
		String[] characterandpos = tagAll(characters);
		String[] words = WordSegAndPosSample.toWord(characters, characterandpos);
		return words;
	}

	@Override
	public String[] tag(String[] words)
	{
		String sentence = new String();
		for(int i=0; i<words.length; i++)
			sentence += words[i];
		
		String[] characters = new String[sentence.length()];
		for (int i = 0; i < sentence.length(); i++)
		{
			characters[i] = sentence.charAt(i) + "";
		}
		
		String[] characterandpos = tagAll(characters);
		String[] poses = WordSegAndPosSample.toPos(characterandpos);
		return poses;
	}
}
