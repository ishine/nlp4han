package com.lc.nlp4han.csc.ngram;

import java.io.File;
import java.io.IOException;

import com.lc.nlp4han.csc.util.Sentence;
import com.lc.nlp4han.ml.ngram.io.BinaryFileNGramModelWriter;
import com.lc.nlp4han.ml.ngram.model.NGramLanguageModel;
import com.lc.nlp4han.ml.ngram.utils.Gram;
import com.lc.nlp4han.ml.ngram.utils.StringGram;

/**
 * 用NGramLanguageModel实现
 */
public class NGramModelImpl implements NGramModel
{

	private NGramLanguageModel nGramModel;

	public NGramModelImpl(NGramLanguageModel nGramModel) throws IOException
	{
		this.nGramModel = nGramModel;
	}

	@Override
	public double getSentenceLogProb(Sentence sentence, int n)
	{
		Gram[] sequence = new StringGram[sentence.size()];
		for (int i = 0; i < sentence.size(); i++)
			sequence[i] = new StringGram(sentence.getToken(i));

		return nGramModel.getSequenceLogProbability(sequence, getOrder(), true);
	}

	@Override
	public int getOrder()
	{
		return nGramModel.getOrder();
	}

	@Override
	public double getNGramLogProb(String[] strs, int order)
	{
		Gram[] grams = new StringGram[strs.length];
		for (int i = 0; i < strs.length; i++)
			grams[i] = new StringGram(strs[i]);

		return nGramModel.getSequenceLogProbability(grams, order, false);
	}

	/**
	 * 写ngram模型到指定路径
	 * 
	 * @param path
	 *            写入路径
	 * @throws IOException
	 */
	public void writeLM(String path) throws IOException
	{
		BinaryFileNGramModelWriter lmWriter = new BinaryFileNGramModelWriter(nGramModel, new File(path));
		lmWriter.persist();
	}
}
