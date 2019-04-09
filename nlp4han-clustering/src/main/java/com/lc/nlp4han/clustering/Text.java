package com.lc.nlp4han.clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Text
{
	private static int defaultName = 1;
	private String name;
	private String content;
	private Sample sample;

	public Text()
	{

	}

	public Text(String name, String content)
	{
		this.name = name;
		this.content = content;
	}

	public Text(File f)
	{

	}

	public Text(String content)
	{
		this.content = content;
		this.name = defaultName + "";
		defaultName++;
	}

	/**
	 * 从磁盘路径中加载Text
	 * 
	 * @param folderPath
	 *            文件路径
	 * @param useDefaultName
	 *            是否使用默认名作为Text名，若为false，会将文件的文件名作为Text.name
	 * @return Text的列表
	 * @throws IOException
	 */
	public static List<Text> getTexts(String folderPath, boolean useDefaultName) throws IOException
	{
		return getTexts(folderPath, useDefaultName, "UTF-8");
	}

	/**
	 * 从磁盘路径中加载Text
	 * 
	 * @param folderPath
	 *            文件路径
	 * @param useDefaultName
	 *            是否使用默认名作为Text名，若为false，会将文件的文件名作为Text.name
	 * @param encoding
	 *            文件的编码方式
	 * @return Text的列表
	 * @throws IOException
	 */
	public static List<Text> getTexts(String folderPath, boolean useDefaultName, String encoding) throws IOException
	{
		List<Text> result = new ArrayList<Text>();
		List<File> files = getFiles(folderPath);

		for (File file : files)
		{
			Text t = getText(file, useDefaultName, encoding);
			if (t != null)
				result.add(t);
		}

		return result;
	}

	/**
	 * 生成Sample
	 * 
	 * @param sg
	 *            样本生成器
	 * @param fg
	 *            特征生成器
	 */
	public void generateSample(FeatureGenerator fg)
	{
		List<Feature> fs = fg.getFeatures(this);
		this.sample = new Sample(fs);
	}

	public Sample getSample()
	{
		return this.sample;
	}

	public void setSample(Sample s)
	{
		this.sample = s;

	}

	public String getName()
	{
		return name;
	}

	public String getContent()
	{
		return content;
	}

	/**
	 * 层次遍历pos文件夹下所有文件
	 * 
	 * @param path
	 *            待遍历的文件夹
	 * @return 文件夹pos下的所有文件列表
	 * @throws IOException
	 */
	private static List<File> getFiles(String path) throws IOException
	{
		List<File> files = new LinkedList<File>();
		List<File> result = new LinkedList<File>();
		File tmpFile = new File(path);
		if (path == null)
			throw new IOException("地址为空");

		if (!tmpFile.exists())
			throw new IOException("文件地址不存在");

		files.add(tmpFile);
		while (!files.isEmpty())
		{
			File[] subFiles;
			tmpFile = files.remove(0);
			if (tmpFile.isFile())
			{
				result.add(tmpFile);
			}
			else if (tmpFile.isDirectory())
			{
				subFiles = tmpFile.listFiles();
				for (int i = 0; i < subFiles.length; i++)
					files.add(subFiles[i]);
			}
		}

		return result;

	}

	/**
	 * 处理文档
	 * 
	 * @param f
	 *            待处理的文档
	 * @param useDefaultName
	 *            是否使用默认名字
	 * @param texts
	 *            存储生成的文本层文本
	 * @throws IOException 
	 */
	private static Text getText(File f, boolean useDefaultName, String encoding) throws IOException
	{

		StringBuffer content = new StringBuffer();
		String tmp;
		BufferedReader bufr;

		bufr = new BufferedReader(new InputStreamReader(new FileInputStream(f), encoding));

		while ((tmp = bufr.readLine()) != null)
			content.append(tmp);

		bufr.close();

		if (content.length() > 0)
		{
			Text t = null;
			if (useDefaultName || f.getName() == null || f.getName() == "")
				t = new Text(content.toString());
			else
				t = new Text(f.getName(), content.toString());

			return t;
		}
		else
			return null;

	}

	@Override
	public String toString()
	{
		return "Text [name=" + name + ", content=" + content + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Text other = (Text) obj;
		if (content == null)
		{
			if (other.content != null)
				return false;
		}
		else if (!content.equals(other.content))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

}
