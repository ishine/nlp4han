package com.lc.nlp4han.constituent.unlex;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.lc.nlp4han.constituent.ConstituentParser;
import com.lc.nlp4han.constituent.ConstituentTree;
import com.lc.nlp4han.constituent.TreeNode;
import com.lc.nlp4han.constituent.pcfg.ConstituentParserCKYLoosePCNF;
import com.lc.nlp4han.constituent.pcfg.PCFG;
import com.lc.nlp4han.constituent.pcfg.PRule;
import com.lc.nlp4han.constituent.pcfg.UncompatibleGrammar;

/**
 * 将带有隐藏符号的语法当作PCFG直接解析，解析得到派生树当作最终的不带隐藏符号的结构树。
 * 
 * @author 王宁
 */
public class ConstituentParserUnlex implements ConstituentParser
{
	// TODO:使用ConstituentParserCKYPCNF
	private ConstituentParserCKYLoosePCNF parserLoosePCNF;

	public ConstituentParserUnlex(Grammar gLatent) throws UncompatibleGrammar
	{
		this(gLatent, 0.0001, false, false);
	}

	public ConstituentParserUnlex(Grammar gLatent, double pruneThreshold, boolean secondPrune,
			boolean prior) throws UncompatibleGrammar
	{
		PCFG pcfg = gLatent.getPCFG();
		// HashMap<String, Double> posMap = new HashMap<>();
		for (short preterminal : gLatent.allPreterminal())
		{
			String originalPre = gLatent.symbolStrValue(preterminal);
			// posMap.put(originalPre, 1.0);
			for (short subPreterminal = 0; subPreterminal < gLatent.getNumSubSymbol(preterminal); subPreterminal++)
			{
				String subPre;
				if (gLatent.getNumSubSymbol(preterminal) == 1)
					subPre = originalPre;
				else// 新添加的，以往没有else时对于句子包含没有被分裂的tag时会直接导致该句子解析不了。
					subPre = originalPre + "_" + subPreterminal;
				
				pcfg.addNonTerminal(subPre);
				PRule pRule = new PRule(1.0, subPre, originalPre);
				pcfg.add(pRule);
			}
		}
		// pcfg.setPosMap(posMap);
		parserLoosePCNF = new ConstituentParserCKYLoosePCNF(pcfg, pruneThreshold, secondPrune, prior);

	}

	@Override
	public ConstituentTree parse(String[] words, String[] poses)
	{
		ConstituentTree[] allTree = parse(words, poses, 1);
		
		if (allTree != null && allTree[0] != null)
			return allTree[0];
		
		return null;
	}

	@Override
	public ConstituentTree[] parse(String[] words, String[] poses, int k)
	{
		ConstituentTree[] result = parserLoosePCNF.parse(words, poses, k);
		if(result ==null)
			return null;
		
		ArrayList<ConstituentTree> trees = new ArrayList<>();
		for (ConstituentTree tree : result)
		{
			if (tree != null)
			{
				removeOriginalTag(tree.getRoot());
				TreeUtil.removeLatentLabel(tree.getRoot());
				TreeBinarization.unbinarize(tree.getRoot());
				trees.add(tree);
			}
		}
		
		if (trees.size() != 0)
			return trees.toArray(new ConstituentTree[trees.size()]);
		else
			return null;
	}

	/**
	 * 去掉为了viterbi解析特意添加的额外的规则
	 * 
	 * @param tree
	 * @return
	 */
	private TreeNode removeOriginalTag(TreeNode tree)
	{
		if (tree.getChildren().size() == 1 && tree.getNodeName().split("_")[0].equals(tree.getChild(0).getNodeName())
				&& !tree.getChild(0).isLeaf())
		{
			ArrayList<TreeNode> children = new ArrayList<TreeNode>(tree.getChild(0).getChildren());
			for (TreeNode child : children)
				child.setParent(tree);
			
			tree.setChildren(children);
		}
		else
		{
			for (TreeNode subTree : tree.getChildren())
				removeOriginalTag(subTree);
		}
		
		return tree;
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		DataInput in = new DataInputStream(new FileInputStream((args[0])));
		Grammar g = new Grammar();
		g.read(in);

		ConstituentParser parser = new ConstituentParserUnlex(g);

		Scanner input = new Scanner(System.in);
		String text = "";
		while (true)
		{
			System.out.println("请输入待分析的文本：");
			text = input.nextLine();

			if (text.equals(""))
			{
				System.out.println("内容为空，请重新输入！");
			}
			else if (text.equals("exit"))
			{
				break;
			}
			else
			{
				String[] wps = text.split("\\s+");
				String[] words = new String[wps.length];
				String[] poses = new String[wps.length];
				for(int i=0; i<wps.length; i++)
				{
					String[] wp = wps[i].split("_");
					words[i] = wp[0];
					poses[i] = wp[1];
				}
				ConstituentTree tree = parser.parse(words, poses);
				if (tree != null)
					System.out.println(tree.toPrettyString());
				else
					System.out.println("Can't parse.");
			}
		}

		input.close();
	}
}
