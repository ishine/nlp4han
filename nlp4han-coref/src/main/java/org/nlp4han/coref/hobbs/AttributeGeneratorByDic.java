package org.nlp4han.coref.hobbs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.nlp4han.coref.hobbs.Attribute.Animacy;
import org.nlp4han.coref.hobbs.Attribute.Gender;
import org.nlp4han.coref.hobbs.Attribute.Number;

import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreeNodeUtil;

/**
 * 源于字典的属性生成器
 * 
 * @author 杨智超
 *
 */
public class AttributeGeneratorByDic implements AttributeGenerator
{
	private static Map<String, Properties> dictionaries = new HashMap<String, Properties>();
	Attribute attribute;

	@Override
	public Attribute extractAttributes(TreeNode treeNode)
	{
		attribute = new Attribute();
		attribute.setAnimacy(getAnimacy(treeNode));
		attribute.setGender(getGender(treeNode));
		attribute.setNumber(getNumber(treeNode));
		return attribute;
	}

	private Properties loadProperties(String fileName, String encoding) throws IOException
	{
		String key = fileName + encoding;
		if (!dictionaries.containsKey(key))
		{
			Properties result = new Properties();
			InputStream stream = AttributeGeneratorByDic.class.getClassLoader().getResourceAsStream(fileName);
			result.load(new InputStreamReader(stream, encoding));
			dictionaries.put(key, result);
			return result;
		}
		return dictionaries.get(key);
	}

	/**
	 * 获取性别属性
	 * 
	 * @param treeNode
	 * @return
	 */
	public Set<Gender> getGender(TreeNode treeNode)
	{
		Set<Gender> result = new HashSet<Gender>();
		// 非动物性无性别
		if (this.attribute != null && attribute.getAnimacy().size() > 0)
		{
			if (this.attribute.getAnimacy().contains(Animacy.INANIMACY))
			{
				result.add(Gender.NONE);
				return result;
			}
		}
		else
		{
			if (getAnimacy(treeNode).contains(Animacy.INANIMACY))
			{
				result.add(Gender.NONE);
				return result;
			}
		}
		
		try
		{
			String value = null;
			if (treeNode.getNodeName().equals("PN"))
			{
				Properties genderDic = loadProperties("gender_PN.properties", "utf-8");
				value = genderDic.getProperty(TreeNodeUtil.getString(treeNode));
			}
			else
			{
				TreeNode head = TreeNodeUtil.getHead(treeNode);
				if (head != null)
				{
					Properties genderDic = loadProperties("gender.properties", "utf-8");
					value = genderDic.getProperty(TreeNodeUtil.getString(head));
				}
			}

			if (value != null)
			{
				String[] values = value.split("_");
				for (String str : values)
				{
					if (str != null && str.equalsIgnoreCase("female"))
						result.add(Gender.FEMALE);
					else if (str != null && str.equalsIgnoreCase("male"))
						result.add(Gender.MALE);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取单复数属性
	 * 
	 * @param treeNode
	 * @return
	 */
	public Set<Number> getNumber(TreeNode treeNode)
	{
		Set<Number> result = new HashSet<Number>();
		if (treeNode.getNodeName().equals("PN"))
		{
			String value;
			String[] values;
			Properties numberDic;
			try
			{
				numberDic = loadProperties("number_PN.properties", "utf-8");
				value = numberDic.getProperty(treeNode.getChildName(0));
				if (value != null)
				{
					values = value.split("_");
					for (String str : values)
					{
						if (str.equalsIgnoreCase("singular"))
							result.add(Number.SINGULAR);
						else if (str.equalsIgnoreCase("plural"))
							result.add(Number.PLURAL);
					}
				}
				if (result.size() > 0)
					return result;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			if (treeNode.getNodeName().equals("NP"))
			{
				TreeNode head = TreeNodeUtil.getHead(treeNode);
				if (head == null)
				{
					if (result.size() < 1)
					{
						result.add(Number.PLURAL);
						result.add(Number.SINGULAR);
						return result;
					}
				}
				String stringOfHead = TreeNodeUtil.getString(head);
				if (stringOfHead.contains("们"))
				{
					result.add(Number.PLURAL);
					return result;
				}
				if (TreeNodeUtil.isCoordinatingNP(treeNode))
				{
					result.add(Number.PLURAL);
					return result;
				}
			}
			String stringOfLeafNodes;
			if (TreeNodeUtil.hasNodeName(treeNode.getChildren(), "DP"))
			{// 含有DP结点，则查表number_DP.properties
				stringOfLeafNodes = TreeNodeUtil.getString(treeNode);
				Properties numberDicDP;
				try
				{
					numberDicDP = loadProperties("number_DP.properties", "utf-8");
					Set<Object> keys = numberDicDP.keySet();
					Iterator<Object> it = keys.iterator();
					while (it.hasNext())
					{
						String key = (String) it.next();
						if (stringOfLeafNodes.contains(key))
						{
							String value = numberDicDP.getProperty(key);
							if (value != null)
							{
								String[] values = value.split("_");
								for (String str : values)
								{
									if (str.equalsIgnoreCase("singular"))
									{
										result.add(Number.SINGULAR);
										return result;
									}
									else if (str.equalsIgnoreCase("plural"))
									{
										result.add(Number.PLURAL);
										return result;
									}
								}
							}

						}
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			if (TreeNodeUtil.hasNodeName(treeNode.getChildren(), "QP"))
			{
				List<TreeNode> qpNodes = TreeNodeUtil.getChildNodeWithSpecifiedName(treeNode, new String[] { "QP" });
				TreeNode qpNode = qpNodes.get(0);
				if (TreeNodeUtil.hasNodeName(qpNode.getChildren(), "CD"))
				{
					List<TreeNode> cdNodes = TreeNodeUtil.getNodesWithSpecifiedName(treeNode, new String[] { "CD" });
					TreeNode cdNode = cdNodes.get(0);
					if (cdNode.getChild(0).getNodeName().equals("一") || cdNode.getChild(0).getNodeName().equals("1"))
					{
						result.add(Number.SINGULAR);
						return result;
					}
					result.add(Number.PLURAL);
					return result;
				}
			}
		}

		return result;
	}

	/**
	 * 获取动物性属性
	 * 
	 * @param treeNode
	 * @return
	 */
	public Set<Animacy> getAnimacy(TreeNode treeNode)
	{
		Set<Animacy> result = new HashSet<Animacy>();
		try
		{
			String value = null;
			if (treeNode.getNodeName().equals("PN"))
			{
				Properties animacyDic = loadProperties("animacy_PN.properties", "utf-8");
				value = animacyDic.getProperty(TreeNodeUtil.getString(treeNode));
			}
			else
			{
				TreeNode head = TreeNodeUtil.getHead(treeNode);
				if (head != null)
				{
					Properties animacyDic = loadProperties("animacy.properties", "utf-8");
					Set<Object> keys = animacyDic.keySet();
					Iterator<Object> it = keys.iterator();
					String strOfHead = TreeNodeUtil.getString(head);
					while (it.hasNext())
					{
						String key = (String) it.next();
						if (strOfHead.contains(key))
						{
							value = animacyDic.getProperty(key);
							break;
						}
					}
				}
			}
			
			if (value != null)
			{
				String[] values = value.split("_");
				for (String str : values)
				{
					if (str.equalsIgnoreCase("human"))
						result.add(Animacy.ANI_HUMAN);
					else if (str.equalsIgnoreCase("animal"))
						result.add(Animacy.ANI_ANIMAL);
					else if (str.equalsIgnoreCase("inanimacy"))
						result.add(Animacy.INANIMACY);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return result;
	}

}
