package com.lc.nlp4han.segpos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 解析后文本样式类
 * 
 * @author 王馨苇
 *
 */
public class WordSegAndPosSample
{
	public List<String> characters;
	public List<String> tags;
	public List<String> words;
	public List<String> poses;
	private String[][] addtionalContext;

	/**
	 * 构造
	 * 
	 * @param characters
	 *            字符
	 * @param tags
	 *            字符标记序列
	 * @param words
	 *            词语
	 * @param poses
	 *            词性
	 */
	public WordSegAndPosSample(String[] characters, String[] tags, String[] words, String[] poses)
	{
		this(characters, tags, words, poses, null);
	}

	/**
	 * 构造
	 * 
	 * @param characters
	 *            字符
	 * @param tags
	 *            字符标记序列
	 * @param words
	 *            词语
	 * @param poses
	 *            词性
	 */
	public WordSegAndPosSample(List<String> characters, List<String> tags, List<String> words, List<String> poses)
	{
		this(characters, tags, words, poses, null);
	}

	/**
	 * 构造
	 * 
	 * @param characters
	 *            字符
	 * @param tags
	 *            字符标记序列
	 * @param words
	 *            词语
	 * @param poses
	 *            词性
	 * @param additionalContext
	 */
	public WordSegAndPosSample(String[] characters, String[] tags, String[] words, String[] poses,
			String[][] additionalContext)
	{
		this(Arrays.asList(characters), Arrays.asList(tags), Arrays.asList(words), Arrays.asList(poses),
				additionalContext);
	}

	/**
	 * 构造
	 * 
	 * @param characters
	 *            字符
	 * @param tags
	 *            字符标记序列
	 * @param words
	 *            词语
	 * @param poses
	 *            词性
	 * @param additionalContext
	 */
	public WordSegAndPosSample(List<String> characters, List<String> tags, List<String> words, List<String> poses,
			String[][] additionalContext)
	{
		this.characters = Collections.unmodifiableList(characters);
		this.tags = Collections.unmodifiableList(tags);
		this.words = Collections.unmodifiableList(words);
		this.poses = Collections.unmodifiableList(poses);

		String[][] ac;
		if (additionalContext != null)
		{
			ac = new String[additionalContext.length][];

			for (int i = 0; i < additionalContext.length; i++)
			{
				ac[i] = new String[additionalContext[i].length];
				System.arraycopy(additionalContext[i], 0, ac[i], 0, additionalContext[i].length);
			}
		}
		else
		{
			ac = null;
		}
		this.addtionalContext = ac;
	}

	/**
	 * 得到词语
	 * 
	 * @return
	 */
	public String[] getWords()
	{
		return this.words.toArray(new String[words.size()]);
	}

	/**
	 * 返回词性标记
	 * 
	 * @return
	 */
	public String[] getPoses()
	{
		return this.poses.toArray(new String[poses.size()]);
	}

	/**
	 * 获得字符
	 * 
	 * @return
	 */
	public String[] getCharacters()
	{
		return this.characters.toArray(new String[characters.size()]);
	}

	/**
	 * 字符标记序列
	 * 
	 * @return
	 */
	public String[] getTags()
	{
		return this.tags.toArray(new String[tags.size()]);
	}

	/**
	 * 额外的信息
	 * 
	 * @return
	 */
	public String[][] getAditionalContext()
	{
		return this.addtionalContext;
	}

	/**
	 * 得到对应的词性序列
	 * 
	 * @param tagsandposesPre
	 *            字的边界_词性 这种格式组成的序列
	 * @return
	 */
	public static String[] toPos(String[] tagsandposesPre)
	{
		List<String> poses = new ArrayList<String>();
		for (int i = 0; i < tagsandposesPre.length; i++)
		{
			String tag = tagsandposesPre[i].split("_")[0];
			String pos = tagsandposesPre[i].split("_")[1];
			if (tag.equals("B"))
			{
				poses.add(pos);
			}
			else if (tag.equals("M"))
			{

			}
			else if (tag.equals("E"))
			{

			}
			else if (tag.equals("S"))
			{
				poses.add(pos);
			}
		}
		return poses.toArray(new String[poses.size()]);
	}

	/**
	 * 得到对应的字的标记序列
	 * 
	 * @param tagsandposesPre
	 *            字的边界_词性 这种格式组成的序列
	 * @return
	 */
	public static String[] toTag(String[] tagsandposesPre)
	{
		List<String> tags = new ArrayList<String>();
		for (int i = 0; i < tagsandposesPre.length; i++)
		{
			String tag = tagsandposesPre[i].split("_")[0];
			tags.add(tag);
		}
		return tags.toArray(new String[tags.size()]);
	}

	/**
	 * 得到对应的分词序列
	 * 
	 * @param characters
	 *            字符序列
	 * @param tagsandposesPre
	 *            字的边界_词性 这种格式组成的序列
	 * @return
	 */
	public static String[] toWord(String[] characters, String[] tagsandposesPre)
	{
		String word = new String();
		ArrayList<String> words = new ArrayList<String>();
		for (int i = 0; i < tagsandposesPre.length; i++)
		{
			word += characters[i];
			String tags = tagsandposesPre[i].split("_")[0];

			if (tags.equals("S") || tags.equals("E"))
			{
				words.add(word);
				word = "";
			}
		}

		if (word.length() > 0)
		{
			words.add(word);
		}

		return words.toArray(new String[words.size()]);
	}

	/**
	 * 得到和输入样本一致的样式
	 * 
	 * @return
	 */
	public String toSample()
	{
		String sample = "";
		for (int i = 0; i < words.size(); i++)
		{
			sample += words.get(i) + "/" + poses.get(i) + " ";
		}
		return sample;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj instanceof WordSegAndPosSample)
		{
			WordSegAndPosSample a = (WordSegAndPosSample) obj;

			return Arrays.equals(getWords(), a.getWords()) && Arrays.equals(getPoses(), a.getPoses());
		}
		else
		{
			return false;
		}
	}
}
