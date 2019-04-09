package com.lc.nlp4han.csc.wordseg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.lc.nlp4han.csc.util.Sentence;
import com.lc.nlp4han.segment.WordSegFactory;
import com.lc.nlp4han.segment.WordSegmenter;

/**
 * 基于nlp4han的中文分词
 */
public class WordSegmentNLP4Han extends AbstractWordSegment
{

	private WordSegmenter segmenter;

	public WordSegmentNLP4Han() throws IOException
	{
		segmenter = WordSegFactory.getWordSegmenter();
	}

	@Override
	public ArrayList<String> segment(Sentence sentence)
	{
		String[] words = segmenter.segment(sentence.toString());

		return new ArrayList<String>(Arrays.asList(words));
	}

}
