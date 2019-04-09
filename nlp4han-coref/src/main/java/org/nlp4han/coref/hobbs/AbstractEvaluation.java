package org.nlp4han.coref.hobbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.nlp4han.coref.AnaphoraResult;

import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.TreeNodeUtil;

public abstract class AbstractEvaluation
{

	public void parse(String[] args)
	{

		String[] path_encoding = parseArgs(args);
		readFileByLines(path_encoding[0], path_encoding[1]);
	}

	public String[] parseArgs(String[] args)
	{
		String usage = "[-path DOC_PATH] [-encoding ENCODING]\n\n";

		String encoding = "utf-8";

		String docPath = null;

		for (int i = 0; i < args.length; i++)
		{
			if ("-encoding".equals(args[i]))
			{
				encoding = args[i + 1];
				i++;
			}
			else if ("-path".equals(args[i]))
			{
				docPath = args[i + 1];
				i++;
			}
		}

		if (docPath == null)
		{
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		final java.nio.file.Path docDir = java.nio.file.Paths.get(docPath);

		if (!java.nio.file.Files.isReadable(docDir))
		{
			System.out.println("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		String[] result = new String[2];
		result[0] = docPath;
		result[1] = encoding;
		return result;
	}

	public void readFileByLines(String path, String encoding)
	{
		FileInputStream fis = null;
		BufferedReader reader = null;

		List<String> information1 = new ArrayList<String>();	//单个样本的信息1
		List<String> information2 = new ArrayList<String>();	//单个样本的信息2
		List<String> information3 = new ArrayList<String>();	//单个样本的信息3

		try
		{
			fis = new FileInputStream(new File(path));

			reader = new BufferedReader(new InputStreamReader(fis, encoding));

			String tempString = null;

			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null)
			{
				processingData(tempString, information1, information2, information3);
			}

			reader.close();

		}
		catch (IOException e)
		{

			e.printStackTrace();

		}
		finally
		{

			if (reader != null)
			{

				try
				{
					reader.close();
				}
				catch (IOException e1)
				{

				}

			}

		}
	}

	private void processingData(String oneLine, List<String> information1, List<String> information2,
			List<String> information3)
	{
		boolean tag = false;
		tag = extractAnEvaluationSample(oneLine, information1, information2, information3);
		if (tag == true)
		{
			processAnEvaluationSample(information1, information2, information3);
			information1.clear();
			information2.clear();
			information3.clear();
		}
	}

	/**
	 * 处理从文本中抽取的单个样本的所有信息
	 * 
	 * @param information1
	 *            单个样本的信息1
	 * @param information2
	 *            单个样本的信息2
	 * @param information3
	 *            单个样本的信息3
	 */
	public abstract void processAnEvaluationSample(List<String> information1, List<String> information2,
			List<String> information3);

	/**
	 * 读取文本的单行信息，提取需要的信息，分别存入information1，information2，information3中，
	 * 如果一条评价样本的所有信息抽取完了返回true，否则，返回false
	 * 
	 * @param oneLine
	 *            从文本中读取的单行信息
	 * @param information1
	 *            用于存储评价样本的信息1
	 * @param information2
	 *            用于存储评价样本的信息2
	 * @param information3
	 *            用于存储评价样本的信息3
	 * @return
	 */
	public abstract boolean extractAnEvaluationSample(String oneLine, List<String> information1,
			List<String> information2, List<String> information3);
	
	
	/**
	 * 将指代消解结果转换成字符串格式的结果
	 * @param resolveResult
	 * @param trees
	 * @return
	 */
	public static List<String> toStringFormat(List<AnaphoraResult> resolveResult, List<TreeNode> trees)
	{
		List<String> result = new ArrayList<String>();
		
		for (AnaphoraResult ar : resolveResult)
		{
			TreeNode ponoun = ar.getPronNode();
			TreeNode antecedent = ar.getAntecedentNode();
			
			TreeNode p1;
			TreeNode a1;
			
			p1 = TreeNodeUtil.getAllLeafNodes(ponoun).get(0);
			
			List<TreeNode> ts = TreeNodeUtil.getAllLeafNodes(antecedent);
			if (ts.size() > 1)
			{
				//p1 = TreeNodeUtil.getAllLeafNodes(TreeNodeUtil.getHead(ponoun, NPHeadRuleSetPTB.getNPRuleSet())).get(0);
				a1 = TreeNodeUtil.getAllLeafNodes(TreeNodeUtil.getHead(antecedent)).get(0);
			}
			else
			{
				//p1 = TreeNodeUtil.getAllLeafNodes(ponoun).get(0);
				a1 = TreeNodeUtil.getAllLeafNodes(antecedent).get(0);
			}
			
			TreeNode root = TreeNodeUtil.getRootNode(ponoun);
			int index1 = trees.indexOf(root);
			int size1 = TreeNodeUtil.getAllLeafNodes(root).indexOf(p1);
			
			root = TreeNodeUtil.getRootNode(antecedent);
			int index2 = trees.indexOf(root);
			int size2 = TreeNodeUtil.getAllLeafNodes(root).indexOf(a1);
			
			String temp = p1.getNodeName() + "(" + (index1+1) + "-" + (size1+1) + ")" + "->" + a1.getNodeName() + "(" + (index2+1) + "-" + (size2+1) + ")";
			
			result.add(temp);
		}
		return result;
	}

}
