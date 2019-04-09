package com.lc.nlp4han.csc.checker;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.lc.nlp4han.csc.util.CommonUtils;
import com.lc.nlp4han.ml.ngram.utils.AbstractGramSentenceStream;
import com.lc.nlp4han.ml.ngram.utils.Gram;
import com.lc.nlp4han.ml.ngram.utils.StringGram;

public class StringGramSentenceStream extends AbstractGramSentenceStream
{

	public StringGramSentenceStream(String pathname, String encoding)
			throws FileNotFoundException, UnsupportedEncodingException
	{
		super(pathname, encoding);
	}

	@Override
	protected Iterator<Gram[]> createGrams(List<String> lines)
	{
		List<Gram[]> list = new ArrayList<>();

		for (int i = 0; i < lines.size(); i++)
		{
			String line = CommonUtils.ToDBC(lines.get(i));
			line = line.replaceAll("\\s+", "").trim();

			if (line.equals(""))
				continue;

			String[] strings = line.split("");
			Gram[] grams = new Gram[strings.length];

			for (int j = 0; j < strings.length; j++)
				grams[j] = new StringGram(strings[j]);

			list.add(grams);
		}

		return list.iterator();
	}
}
