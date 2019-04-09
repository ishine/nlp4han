package com.lc.nlp4han.segpos;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.lc.nlp4han.util.DictionaryLoader;
import com.lc.nlp4han.util.CharTypeUtil;
import com.lc.nlp4han.util.FullHalfWidthUtil;

public class WordSegAndPosContextGeneratorConf implements WordSegAndPosContextGenerator
{

	private boolean c_2Set;
	private boolean c_1Set;
	private boolean c0Set;
	private boolean c1Set;
	private boolean c2Set;
	
	private boolean c_2c_1Set;
	private boolean c_1c0Set;
	private boolean c0c1Set;
	private boolean c1c2Set;
	private boolean c_1c1Set;
	
	private boolean c0prefix;
	
	private boolean w0c0Set;
	private boolean PuSet;
	private boolean TSet;
	
	private boolean Bc_1w0Pc_1w0Set;
	private boolean Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set;

	private boolean Lt0Set;
	private boolean c_1t0Set;
	private boolean c0t0Set;
	private boolean c1t0Set;

	private boolean c_2c0set;
	private boolean c_1c0c1set;

	Set<String> dictionalWords;

	private String dictResource;
	private String dictEncoding;

	/**
	 * 无参构造，加载feature配置文件
	 * 
	 * @throws IOException
	 */
	public WordSegAndPosContextGeneratorConf() throws IOException
	{
		Properties featureConf = new Properties();
		InputStream featureStream = WordSegAndPosContextGeneratorConf.class.getClassLoader()
				.getResourceAsStream("com/lc/nlp4han/segpos/feature.properties");
		featureConf.load(featureStream);

		init(featureConf);
	}

	/**
	 * 有参数的构造函数
	 * 
	 * @param properties
	 *            配置文件
	 * @throws IOException
	 */
	public WordSegAndPosContextGeneratorConf(Properties properties) throws IOException
	{

		init(properties);
	}

	/**
	 * 根据配置文件初始化特征
	 * 
	 * @param properties
	 *            配置文件
	 * @throws IOException
	 */
	private void init(Properties config) throws IOException
	{
		dictResource = config.getProperty("feature.dict");
		dictEncoding = config.getProperty("feature.dict.encoding");

		InputStream dictIn = WordSegAndPosContextGeneratorConf.class.getClassLoader().getResourceAsStream(dictResource);
		dictionalWords = DictionaryLoader.getWords(dictIn, dictEncoding);

		// dictionalWords =
		// DictionaryLoader.getWords("E:\\pku_training_maxentModelC.txt", "gbk");

		c_2Set = (config.getProperty("feature.c_2", "true").equals("true"));
		c_1Set = (config.getProperty("feature.c_1", "true").equals("true"));
		c0Set = (config.getProperty("feature.c0", "true").equals("true"));
		c1Set = (config.getProperty("feature.c1", "true").equals("true"));
		c2Set = (config.getProperty("feature.c2", "true").equals("true"));

		c_2c_1Set = (config.getProperty("feature.c_2c_1", "true").equals("true"));
		c_1c0Set = (config.getProperty("feature.c_1c0", "true").equals("true"));
		c0c1Set = (config.getProperty("feature.c0c1", "true").equals("true"));
		c1c2Set = (config.getProperty("feature.c1c2", "true").equals("true"));

		c_1c1Set = (config.getProperty("feature.c_1c1", "true").equals("true"));

		c0prefix = (config.getProperty("feature.c0pre", "true").equals("true"));

		w0c0Set = (config.getProperty("feature.w0c0", "true").equals("true"));

		PuSet = (config.getProperty("feature.Pu", "true").equals("true"));

		TSet = (config.getProperty("feature.T", "true").equals("true"));

		Bc_1w0Pc_1w0Set = (config.getProperty("feature.Bc_1w0Pc_1w0", "true").equals("true"));

		Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set = (config.getProperty("feature.Bc_2w0Pc_2w0Bc_1w0Pc_1w0", "true").equals("true"));

		// 获取配置文件中的字典特征的设置值
		Lt0Set = (config.getProperty("feature.Lt0", "true").equals("true"));
		c_1t0Set = (config.getProperty("feature.c_1t0", "true").equals("true"));
		c0t0Set = (config.getProperty("feature.c0t0", "true").equals("true"));
		c1t0Set = (config.getProperty("feature.c1t0", "true").equals("true"));

	}

	@Override
	public String toString()
	{
		return "WordSegPosContextGeneratorConfExtend{" + "c_2Set=" + c_2Set + ", c_1Set=" + c_1Set + ", c0Set=" + c0Set
				+ ", c1Set=" + c1Set + ", c2Set=" + c2Set + ", c_2c_1Set=" + c_2c_1Set + ", c_1c0Set=" + c_1c0Set
				+ ", c0c1Set=" + c0c1Set + ", c1c2Set=" + c1c2Set + ", c_1c1Set=" + c_1c1Set + ",c0prefix=" + c0prefix
				+ ",w0c0Set=" + w0c0Set + ",PuSet=" + PuSet + ",TSet=" + TSet + ",Bc_1w0Pc_1w0Set=" + Bc_1w0Pc_1w0Set
				+ ",Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set=" + Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set + ",Lt0Set=" + Lt0Set + ",c_1t0Set="
				+ c_1t0Set + ",c0t0Set=" + c0t0Set + ",c1t0Set=" + c1t0Set + ",c_2c0set=" + c_2c0set + ",c_1c0c1set"
				+ c_1c0c1set;
	}

	/**
	 * 词性标注的训练语料生成特征
	 * 
	 * @param i
	 *            当前字符的位置
	 * @param j
	 *            当前字符属于词语的位置
	 * @param characters
	 *            字符的序列
	 * @param tags
	 *            字符的标记序列
	 * @param words
	 *            词语序列
	 * @param poses
	 *            词性序列
	 * @param ac
	 *            额外的信息
	 * @return
	 */
	public String[] getContext(int i, int j, String[] characters, String[] tags, String[] words, String[] poses,
			Object[] ac)
	{

		return getContext(i, j, characters, tags, words, poses);
	}

	/**
	 * 词性标注的训练语料生成特征
	 * 
	 * @param i
	 *            当前字符的位置
	 * @param j
	 *            当前字符属于词语的位置
	 * @param characters
	 *            字符的序列
	 * @param tags
	 *            字符的标记序列
	 * @param words
	 *            词语序列
	 * @param poses
	 *            词性序列
	 * @return
	 */
	private String[] getContext(int i, int j, String[] characters, String[] tags, String[] words, String[] poses)
	{
		String c1, c2, c3, c0, c_1, c_2, c_3;
		c1 = c2 = c3 = c0 = c_1 = c_2 = c_3 = null;
		String TC_1, TC_2, TC0, TC1, TC2;
		TC_1 = TC_2 = TC0 = TC1 = TC2 = null;
		String w0, w_1, w_2, p_1, p_2;
		w0 = w_1 = w_2 = p_1 = p_2 = null;
		c0 = characters[i];
		w0 = words[j];
		TC0 = CharTypeUtil.featureType(c0);
		if (characters.length > i + 1)
		{
			c1 = characters[i + 1];
			TC1 = CharTypeUtil.featureType(c1);

			if (characters.length > i + 2)
			{
				c2 = characters[i + 2];
				TC2 = CharTypeUtil.featureType(c2);
				if (characters.length > i + 3)
				{
					c3 = characters[i + 3];
				}
			}
		}

		if (i - 1 >= 0)
		{
			c_1 = characters[i - 1];
			TC_1 = CharTypeUtil.featureType(c_1);

			if (i - 2 >= 0)
			{
				c_2 = characters[i - 2].toString();
				TC_2 = CharTypeUtil.featureType(c_2);
				if (i - 3 >= 0)
				{
					c_3 = characters[i - 3];
				}
			}
		}

		List<String> features = new ArrayList<String>();
		// c_2
		if (c_2 != null)
		{
			if (c_2Set)
			{
				features.add("c_2=" + c_2);
			}
		}
		// c_1
		if (c_1 != null)
		{
			if (c_1Set)
			{
				features.add("c_1=" + c_1);
			}
		}
		// c0
		if (c0Set)
		{
			features.add("c0=" + c0);
		}
		// c1
		if (c1 != null)
		{
			if (c1Set)
			{
				features.add("c1=" + c1);
			}
		}
		// c2
		if (c2 != null)
		{
			if (c2Set)
			{
				features.add("c2=" + c2);
			}
		}
		// c_2c_1
		if (c_2 != null && c_1 != null)
		{
			if (c_2c_1Set)
			{
				features.add("c_2c_1=" + c_2 + c_1);
			}
		}
		// c_1c0
		if (c_1 != null)
		{
			if (c_1c0Set)
			{
				features.add("c_1c0=" + c_1 + c0);
			}
		}
		// c0c1
		if (c1 != null)
		{
			if (c0c1Set)
			{
				features.add("c0c1=" + c0 + c1);
			}
		}
		// c1c2
		if (c1 != null && c2 != null)
		{
			if (c1c2Set)
			{
				features.add("c1c2=" + c1 + c2);
			}
		}
		// c_1c1
		if (c_1 != null && c1 != null)
		{
			if (c_1c1Set)
			{
				features.add("c_1c1=" + c_1 + c1);
			}
		}

		// c0pre
		if (c0prefix)
		{
			features = addC0Prefix(features, c0);
		}

		// W0C0
		if (w0c0Set)
		{
			features.add("w0c0=" + w0 + c0);
		}
		// Pu
		if (PuSet)
		{
			if (CharTypeUtil.isChinesePunctuation(FullHalfWidthUtil.toHalfWidth(c0)))
				features.add("Pu=" + 1);
			else
				features.add("Pu=" + 0);
		}
		// T
		if (TC_2 != null && TC_1 != null && TC1 != null && TC2 != null)
		{
			if (TSet)
			{
				features.add("T=" + TC_2 + TC_1 + TC0 + TC1 + TC2);
			}
		}

		// 判断当前词语前面还有没有词
		if (j - 1 >= 0)
		{
			w_1 = words[j - 1];
			p_1 = poses[j - 1];
			if (j - 2 >= 0)
			{
				w_2 = words[j - 2];
				p_2 = poses[j - 2];
			}
		}

		// 当前词前面一个词的长度为1
		if (w_1 != null && (w_1.length() == 1))
		{
			if (Bc_1w0Pc_1w0Set)
			{
				// 当前词的前面一个词的长度为1，字符标记肯定为S,
				features.add("Bc_1w0Pc_1w0=" + "S" + p_1);
			}
			if (w_2 != null)
			{
				// 当前词前面第二个词,长度为1,字符标记为S
				if (w_2.length() == 1)
				{
					if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + "S" + p_2 + "S" + p_1);
					}
				}
				else
				{// 当前词前面第二个词,长度不为1,字符标记为E
					if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + "E" + p_2 + "S" + p_1);
					}
				}

			}
		}

		// 当前词前面一个词语的长度不为1
		if (w_1 != null && w_1.length() > 1)
		{
			// 只要当前词前面一个词语的长度不为1，字符标记肯定是E
			if (Bc_1w0Pc_1w0Set)
			{
				features.add("Bc_1w0Pc_1w0=" + "E" + p_1);
			}
			// 当前词前面一个词长度为二的话，前面第二个字符的标记为B
			if (w_1.length() == 2)
			{
				if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
				{
					features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + "B" + p_1 + "E" + p_1);
				}
				// 当前词前面一个词长度大于二的话，前面第二个字符的标记为M
			}
			else if (w_1.length() > 2)
			{
				if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
				{
					features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + "M" + p_1 + "E" + p_1);
				}
			}

		}

		// 下面是增加和词典匹配后特征的情况

		// 标志位:(1)如果匹配四字词成功则不需要匹配三字词，两字词
		// (2)如果匹配四字词不成功则需要匹配三字词，匹配三字词成功，则不需要匹配两字词
		boolean flagByThree = true;
		boolean flagByTwo = true;
		// 加入词典中提取出来的特征
		// 1.如果当前词左右两侧三个都不为空，则匹配四字词
		// (1)c_3c_2c_1c0
		if (c_3 != null && c_2 != null && c_1 != null && c0 != null)
		{

			if (inDictionary(c_3 + c_2 + c_1 + c0))
			{

				if (Lt0Set)
				{
					features.add("Le=" + 4);
				}
				if (c_1 != null)
				{
					if (c_1t0Set)
					{
						features.add("c_1e=" + c_1);
					}
				}

				if (c0t0Set)
				{
					features.add("c0e=" + c0);
				}
				if (c1 != null)
				{
					if (c1t0Set)
					{
						features.add("c1e=" + c1);
					}
				}

				flagByThree = false;
				flagByTwo = false;
			}
		}

		// (2)c_2c_1c0c1与c_1c0c1c2记录的特征是一样的
		if (c_2 != null && c_1 != null && c0 != null && c1 != null)
		{
			if (inDictionary(c_2 + c_1 + c0 + c1))
			{
				if (Lt0Set)
				{
					features.add("Lm=" + 4);
				}
				if (c_1 != null)
				{
					if (c_1t0Set)
					{
						features.add("c_1m=" + c_1);
					}
				}

				if (c0t0Set)
				{
					features.add("c0m=" + c0);
				}
				if (c1 != null)
				{
					if (c1t0Set)
					{
						features.add("c1m=" + c1);
					}
				}
				flagByThree = false;
				flagByTwo = false;
			}
		}
		// (3)c_2c_1c0c1与c_1c0c1c2记录的特征是一样的
		if (c_1 != null && c0 != null && c1 != null && c2 != null)
		{
			if (inDictionary(c_1 + c0 + c1 + c2))
			{
				if (Lt0Set)
				{
					features.add("Lm=" + 4);
				}
				if (c_1t0Set)
				{
					features.add("c_1m=" + c_1);
				}
				if (c0t0Set)
				{
					features.add("c0m=" + c0);
				}
				if (c1t0Set)
				{
					features.add("c1m=" + c1);
				}

				flagByThree = false;
				flagByTwo = false;
			}
		}
		// (4)c0c1c2c3
		if (c0 != null && c1 != null && c2 != null && c3 != null)
		{
			if (inDictionary(c0 + c1 + c2 + c3))
			{
				if (Lt0Set)
				{
					features.add("Lb=" + 4);
				}
				if (c_1 != null)
				{
					if (c_1t0Set)
					{
						features.add("c_1b=" + c_1);
					}
				}

				if (c0t0Set)
				{
					features.add("c0b=" + c0);
				}
				if (c1t0Set)
				{
					features.add("c1b=" + c1);
				}

				flagByThree = false;
				flagByTwo = false;
			}
		}

		// 2.匹配三字词的情形
		// （1）c_2c_1c0
		if (c_2 != null && c_1 != null && c0 != null && flagByThree)
		{
			if (inDictionary(c_2 + c_1 + c0))
			{
				if (Lt0Set)
				{
					features.add("Le=" + 4);
				}
				if (c_1t0Set)
				{
					features.add("c_1e=" + c_1);
				}
				if (c0t0Set)
				{
					features.add("c0e=" + c0);
				}
				if (c1 != null)
				{
					if (c1t0Set)
					{
						features.add("c1e=" + c1);
					}
				}

				flagByTwo = false;
			}
		}
		// (2)c_1c0c1
		if (c_1 != null && c0 != null && c1 != null && flagByThree)
		{
			if (inDictionary(c_1 + c0 + c1))
			{
				if (Lt0Set)
				{
					features.add("Lm=" + 4);
				}
				if (c_1t0Set)
				{
					features.add("c_1m=" + c_1);
				}
				if (c0t0Set)
				{
					features.add("c0m=" + c0);
				}
				if (c1t0Set)
				{
					features.add("c1m=" + c1);
				}

				flagByTwo = false;
			}
		}
		// (3)c0c1c2
		if (c0 != null && c1 != null && c2 != null && flagByThree)
		{
			if (inDictionary(c0 + c1 + c2))
			{
				if (Lt0Set)
				{
					features.add("Lb=" + 4);
				}
				if (c_1 != null)
				{
					if (c_1t0Set)
					{
						features.add("c_1b=" + c_1);
					}
				}

				if (c0t0Set)
				{
					features.add("c0b=" + c0);
				}
				if (c1t0Set)
				{
					features.add("c1b=" + c1);
				}

				flagByTwo = false;
			}
		}
		// 3.匹配两字的情形
		// （1）c_1c0
		if (c_1 != null && c0 != null && flagByTwo)
		{
			if (inDictionary(c_1 + c0))
			{
				if (Lt0Set)
				{
					features.add("Le=" + 4);
				}
				if (c_1t0Set)
				{
					features.add("c_1e=" + c_1);
				}
				if (c0t0Set)
				{
					features.add("c0e=" + c0);
				}
				if (c1 != null)
				{
					if (c1t0Set)
					{
						features.add("c1e=" + c1);
					}
				}

			}
		}
		// (2)c0c1
		if (c0 != null && c1 != null && flagByTwo)
		{
			if (inDictionary(c0 + c1))
			{
				if (Lt0Set)
				{
					features.add("Lb=" + 4);
				}
				if (c_1 != null)
				{
					if (c_1t0Set)
					{
						features.add("c_1b=" + c_1);
					}
				}

				if (c0t0Set)
				{
					features.add("c0b=" + c0);
				}
				if (c1t0Set)
				{
					features.add("c1b=" + c1);
				}

			}
		}

		String[] contexts = features.toArray(new String[features.size()]);
		return contexts;
	}

	/**
	 * 生成c0pre
	 * 
	 * @param features
	 *            已有的特征
	 * @param c0
	 *            当前字符
	 * @return
	 */
	private List<String> addC0Prefix(List<String> features, String c0)
	{
		List<String> result = new ArrayList<String>();

		for (String feature : features)
		{
			result.add(feature);

			int p = feature.indexOf("=");
			String name = feature.substring(0, p);
			String value = feature.substring(p + 1);

			String cof = "c0_" + name + "=" + c0 + value;

			result.add(cof);
		}

		return result;
	}

	/**
	 * 没有分词的测试语料生成特征
	 * 
	 * @param i
	 *            当前字符的位置
	 * @param characters
	 *            字符的序列
	 * @param tags
	 *            字符的标记序列
	 * @param ac
	 *            额外的信息
	 */
	public String[] getContext(int i, String[] characters, String[] tags, Object[] ac)
	{
		return getContext(i, characters, tags);
	}

	/**
	 * 没有分词的测试语料生成特征
	 * 
	 * @param i
	 *            当前字符的位置
	 * @param characters
	 *            字符的序列
	 * @param tags
	 *            字符的标记序列
	 */
	@SuppressWarnings("unused")
	private String[] getContext(int i, String[] characters, String[] tags)
	{
		// HashMap<String,List<String>> dictionary = measure.getDictionary();
		// System.out.println(i);
		String[] tag = null;
		String[] pos = null;
		String c1, c2, c3, c0, c_1, c_2, c_3;
		c1 = c2 = c3 = c0 = c_1 = c_2 = c_3 = null;
		String TC_1, TC_2, TC0, TC1, TC2;
		TC_1 = TC_2 = TC0 = TC1 = TC2 = null;
		String w0, w_1, w_2, p_1, p_2;
		w0 = w_1 = w_2 = p_1 = p_2 = null;
		c0 = characters[i];
		TC0 = CharTypeUtil.featureType(c0);
		if (characters.length > i + 1)
		{
			c1 = characters[i + 1];
			TC1 = CharTypeUtil.featureType(c1);

			if (characters.length > i + 2)
			{
				c2 = characters[i + 2];
				TC2 = CharTypeUtil.featureType(c2);
				if (characters.length > i + 3)
				{
					c3 = characters[i + 3];
				}
			}
		}

		if (i - 1 >= 0)
		{
			// 将 词的边界_词性 转成两个数组，分别是词的边界数组，词性数组
			tag = new String[i];
			pos = new String[i];
			for (int j = i - 1; j >= 0; j--)
			{
				tag[j] = tags[j].split("_")[0];
				pos[j] = tags[j].split("_")[1];
			}

			c_1 = characters[i - 1];
			TC_1 = CharTypeUtil.featureType(c_1);

			if (i - 2 >= 0)
			{
				c_2 = characters[i - 2];
				TC_2 = CharTypeUtil.featureType(c_2);
				if (i - 3 >= 0)
				{
					c_3 = characters[i - 3];
				}
			}
		}

		List<String> features = new ArrayList<String>();
		// c_2
		if (c_2 != null)
		{
			if (c_2Set)
			{
				features.add("c_2=" + c_2);
			}
		}
		// c_1
		if (c_1 != null)
		{
			if (c_1Set)
			{
				features.add("c_1=" + c_1);
			}
		}
		// c0
		if (c0Set)
		{
			features.add("c0=" + c0);
		}
		// c1
		if (c1 != null)
		{
			if (c1Set)
			{
				features.add("c1=" + c1);
			}
		}
		// c2
		if (c2 != null)
		{
			if (c2Set)
			{
				features.add("c2=" + c2);
			}
		}
		// c_2c_1
		if (c_2 != null && c_1 != null)
		{
			if (c_2c_1Set)
			{
				features.add("c_2c_1=" + c_2 + c_1);
			}
		}
		// c_1c0
		if (c_1 != null)
		{
			if (c_1c0Set)
			{
				features.add("c_1c0=" + c_1 + c0);
			}
		}
		// c0c1
		if (c1 != null)
		{
			if (c0c1Set)
			{
				features.add("c0c1=" + c0 + c1);
			}
		}
		// c1c2
		if (c1 != null && c2 != null)
		{
			if (c1c2Set)
			{
				features.add("c1c2=" + c1 + c2);
			}
		}
		// c_1c1
		if (c_1 != null && c1 != null)
		{
			if (c_1c1Set)
			{
				features.add("c_1c1=" + c_1 + c1);
			}
		}

		// c0pre
		if (c0prefix)
		{
			features = addC0Prefix(features, c0);
		}
		// Pu
		if (PuSet)
		{
			if (CharTypeUtil.isChinesePunctuation(FullHalfWidthUtil.toHalfWidth(c0)))
				features.add("Pu=" + 1);
			else
				features.add("Pu=" + 0);
		}
		// T
		if (TC_2 != null && TC_1 != null && TC1 != null && TC2 != null)
		{
			if (TSet)
			{
				features.add("T=" + TC_2 + TC_1 + TC0 + TC1 + TC2);
			}
		}

		// 下面是增加和词典匹配后特征的情况

		// 标志位:(1)如果匹配四字词成功则不需要匹配三字词，两字词
		// (2)如果匹配四字词不成功则需要匹配三字词，匹配三字词成功，则不需要匹配两字词
		boolean flagByThree = true;
		boolean flagByTwo = true;
		// 加入词典中提取出来的特征
		// 1.如果当前词左右两侧三个都不为空，则匹配四字词
		// (1)c_3c_2c_1c0
		if (c_3 != null && c_2 != null && c_1 != null && c0 != null)
		{
			if (inDictionary(c_3 + c_2 + c_1 + c0))
			{
				if (Lt0Set)
				{
					features.add("Le=" + 4);
				}
				if (c_1 != null)
				{
					if (c_1t0Set)
					{
						features.add("c_1e=" + c_1);
					}
				}

				if (c0t0Set)
				{
					features.add("c0e=" + c0);
				}
				if (c1 != null)
				{
					if (c1t0Set)
					{
						features.add("c1e=" + c1);
					}
				}

				if (w0c0Set)
				{
					features.add("w0c0=" + c_3 + c_2 + c_1 + c0 + c0);
				}
				if (i - 4 >= 0)
				{
					if (Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_1w0Pc_1w0=" + tag[i - 4] + pos[i - 4]);
					}
				}
				if (i - 5 >= 0)
				{
					if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + tag[i - 5] + pos[i - 5] + tag[i - 4] + pos[i - 4]);
					}
				}
				flagByThree = false;
				flagByTwo = false;
			}
		}

		// (2)c_2c_1c0c1与c_1c0c1c2记录的特征是一样的
		if (c_2 != null && c_1 != null && c0 != null && c1 != null)
		{
			if (inDictionary(c_2 + c_1 + c0 + c1))
			{
				if (Lt0Set)
				{
					features.add("Lm=" + 4);
				}
				if (c_1 != null)
				{
					if (c_1t0Set)
					{
						features.add("c_1m=" + c_1);
					}
				}

				if (c0t0Set)
				{
					features.add("c0m=" + c0);
				}
				if (c1 != null)
				{
					if (c1t0Set)
					{
						features.add("c1m=" + c1);
					}
				}

				if (w0c0Set)
				{
					features.add("w0c0=" + c_2 + c_1 + c0 + c1 + c0);
				}
				if (i - 3 >= 0)
				{
					if (Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_1w0Pc_1w0=" + tag[i - 3] + pos[i - 3]);
					}
				}
				if (i - 4 >= 0)
				{
					if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + tag[i - 4] + pos[i - 4] + tag[i - 3] + pos[i - 3]);
					}
				}

				flagByThree = false;
				flagByTwo = false;
			}
		}
		// (3)c_2c_1c0c1与c_1c0c1c2记录的特征是一样的
		if (c_1 != null && c0 != null && c1 != null && c2 != null)
		{
			if (inDictionary(c_1 + c0 + c1 + c2))
			{
				if (Lt0Set)
				{
					features.add("Lm=" + 4);
				}
				if (c_1t0Set)
				{
					features.add("c_1m=" + c_1);
				}
				if (c0t0Set)
				{
					features.add("c0m=" + c0);
				}
				if (c1t0Set)
				{
					features.add("c1m=" + c1);
				}

				if (w0c0Set)
				{
					features.add("w0c0=" + c_1 + c0 + c1 + c2 + c0);
				}
				if (i - 2 >= 0)
				{
					if (Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_1w0Pc_1w0=" + tag[i - 2] + pos[i - 2]);
					}
				}
				if (i - 3 >= 0)
				{
					if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + tag[i - 3] + pos[i - 3] + tag[i - 2] + pos[i - 2]);
					}
				}
				flagByThree = false;
				flagByTwo = false;
			}
		}
		// (4)c0c1c2c3
		if (c0 != null && c1 != null && c2 != null && c3 != null)
		{
			if (inDictionary(c0 + c1 + c2 + c3))
			{
				if (Lt0Set)
				{
					features.add("Lb=" + 4);
				}
				if (c_1 != null)
				{
					if (c_1t0Set)
					{
						features.add("c_1b=" + c_1);
					}
				}

				if (c0t0Set)
				{
					features.add("c0b=" + c0);
				}
				if (c1t0Set)
				{
					features.add("c1b=" + c1);
				}

				if (w0c0Set)
				{
					features.add("w0c0=" + c0 + c1 + c2 + c3 + c0);
				}
				if (i - 1 >= 0)
				{
					if (Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_1w0Pc_1w0=" + tag[i - 1] + pos[i - 1]);
					}
				}
				if (i - 2 >= 0)
				{
					if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + tag[i - 2] + pos[i - 2] + tag[i - 1] + pos[i - 1]);
					}
				}
				flagByThree = false;
				flagByTwo = false;
			}
		}

		// 2.匹配三字词的情形
		// （1）c_2c_1c0
		if (c_2 != null && c_1 != null && c0 != null && flagByThree)
		{
			if (inDictionary(c_2 + c_1 + c0))
			{
				if (Lt0Set)
				{
					features.add("Le=" + 4);
				}
				if (c_1t0Set)
				{
					features.add("c_1e=" + c_1);
				}
				if (c0t0Set)
				{
					features.add("c0e=" + c0);
				}
				if (c1 != null)
				{
					if (c1t0Set)
					{
						features.add("c1e=" + c1);
					}
				}

				if (w0c0Set)
				{
					features.add("w0c0=" + c_2 + c_1 + c0 + c0);
				}
				if (i - 3 >= 0)
				{
					if (Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_1w0Pc_1w0=" + tag[i - 3] + pos[i - 3]);
					}
				}
				if (i - 4 >= 0)
				{
					if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + tag[i - 4] + pos[i - 4] + tag[i - 3] + pos[i - 3]);
					}
				}

				flagByTwo = false;
			}
		}
		// (2)c_1c0c1
		if (c_1 != null && c0 != null && c1 != null && flagByThree)
		{
			if (inDictionary(c_1 + c0 + c1))
			{
				if (Lt0Set)
				{
					features.add("Lm=" + 4);
				}
				if (c_1t0Set)
				{
					features.add("c_1m=" + c_1);
				}
				if (c0t0Set)
				{
					features.add("c0m=" + c0);
				}
				if (c1t0Set)
				{
					features.add("c1m=" + c1);
				}

				if (w0c0Set)
				{
					features.add("w0c0=" + c_1 + c0 + c1 + c0);
				}
				if (i - 2 >= 0)
				{
					if (Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_1w0Pc_1w0=" + tag[i - 2] + pos[i - 2]);
					}
				}
				if (i - 3 >= 0)
				{
					if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + tag[i - 3] + pos[i - 3] + tag[i - 2] + pos[i - 2]);
					}
				}
				flagByTwo = false;
			}
		}
		// (3)c0c1c2
		if (c0 != null && c1 != null && c2 != null && flagByThree)
		{
			if (inDictionary(c0 + c1 + c2))
			{
				if (Lt0Set)
				{
					features.add("Lb=" + 4);
				}
				if (c_1 != null)
				{
					if (c_1t0Set)
					{
						features.add("c_1b=" + c_1);
					}
				}

				if (c0t0Set)
				{
					features.add("c0b=" + c0);
				}
				if (c1t0Set)
				{
					features.add("c1b=" + c1);
				}

				if (w0c0Set)
				{
					features.add("w0c0=" + c0 + c1 + c2 + c0);
				}
				if (i - 1 >= 0)
				{
					if (Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_1w0Pc_1w0=" + tag[i - 1] + pos[i - 1]);
					}
				}
				if (i - 2 >= 0)
				{
					if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + tag[i - 2] + pos[i - 2] + tag[i - 1] + pos[i - 1]);
					}
				}
				flagByTwo = false;
			}
		}
		// 3.匹配两字的情形
		// （1）c_1c0
		if (c_1 != null && c0 != null && flagByTwo)
		{
			if (inDictionary(c_1 + c0))
			{
				if (Lt0Set)
				{
					features.add("Le=" + 4);
				}
				if (c_1t0Set)
				{
					features.add("c_1e=" + c_1);
				}
				if (c0t0Set)
				{
					features.add("c0e=" + c0);
				}
				if (c1 != null)
				{
					if (c1t0Set)
					{
						features.add("c1e=" + c1);
					}
				}

				if (w0c0Set)
				{
					features.add("w0c0=" + c_1 + c0 + c0);
				}
				if (i - 2 >= 0)
				{
					if (Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_1w0Pc_1w0=" + tag[i - 2] + pos[i - 2]);
					}
				}
				if (i - 3 >= 0)
				{
					if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + tag[i - 3] + pos[i - 3] + tag[i - 2] + pos[i - 2]);
					}
				}

			}
		}
		// (2)c0c1
		if (c0 != null && c1 != null && flagByTwo)
		{
			if (inDictionary(c0 + c1))
			{
				if (Lt0Set)
				{
					features.add("Lb=" + 4);
				}
				if (c_1 != null)
				{
					if (c_1t0Set)
					{
						features.add("c_1b=" + c_1);
					}
				}

				if (c0t0Set)
				{
					features.add("c0b=" + c0);
				}
				if (c1t0Set)
				{
					features.add("c1b=" + c1);
				}

				if (w0c0Set)
				{
					features.add("w0c0=" + c0 + c1 + c0);
				}
				if (i - 1 >= 0)
				{
					if (Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_1w0Pc_1w0=" + tag[i - 1] + pos[i - 1]);
					}
				}
				if (i - 2 >= 0)
				{
					if (Bc_2w0Pc_2w0Bc_1w0Pc_1w0Set)
					{
						features.add("Bc_2w0Pc_2w0Bc_1w0Pc_1w0=" + tag[i - 2] + pos[i - 2] + tag[i - 1] + pos[i - 1]);
					}
				}
			}
		}
		String[] contexts = features.toArray(new String[features.size()]);
		return contexts;
	}

	// 判断是否为词典中的词语
	private boolean inDictionary(String words)
	{
		if (dictionalWords.contains(words))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

}
