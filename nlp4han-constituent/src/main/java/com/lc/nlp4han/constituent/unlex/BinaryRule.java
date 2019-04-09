package com.lc.nlp4han.constituent.unlex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeMap;

/**
 * 二元规则
 * 
 * @author 王宁
 */
public class BinaryRule extends Rule
{
	private short leftChild;
	private short rightChild;
	private ArrayList<ArrayList<ArrayList<Double>>> scores = new ArrayList<ArrayList<ArrayList<Double>>>();// 保存规则例如A_i
																													// ->
	// B_j

	public BinaryRule(short parent, short lChild, short rChild)
	{
		super.parent = parent;
		this.leftChild = lChild;
		this.rightChild = rChild;
	}

	/**
	 * 所有子规则的概率都为零，待录入
	 */
	public BinaryRule(short parent, short nSubP, short lChild, short nSubLC, short rChild, short nSubRC)
	{
		super.parent = parent;
		this.leftChild = lChild;
		this.rightChild = rChild;
		for (int i = 0; i < nSubP; i++)
		{
			ArrayList<ArrayList<Double>> LR = new ArrayList<ArrayList<Double>>();
			for (int j = 0; j < nSubLC; j++)
			{
				ArrayList<Double> R = new ArrayList<Double>();
				for (int k = 0; k < nSubRC; k++)
				{
					R.add(0.0);
				}
				LR.add(R);
			}
			scores.add(LR);
		}
	}

	public void setSubRuleScore(short indexSubP, short indexSubLC, short indexSubRC, double score)
	{
		scores.get(indexSubP).get(indexSubLC).set(indexSubRC, score);
	}

	@Override
	public void split()
	{
		Random random = GrammarExtractor.random;
		boolean randomPerturbation = true;
		// split rightChild
		int pNumSubSymbol = scores.size();
		for (int i = 0; i < pNumSubSymbol; i++)
		{
			int lCNumSubsymbol = scores.get(i).size();
			for (int j = 0; j < lCNumSubsymbol; j++)
			{
				int rCNumSubsymbol = scores.get(i).get(j).size();
				for (int k = rCNumSubsymbol - 1; k >= 0; k--)
				{
					scores.get(i).get(j).add(k + 1, scores.get(i).get(j).get(k) / 2);
					scores.get(i).get(j).set(k, scores.get(i).get(j).get(k + 1));
				}
			}
		}
		// split leftChild
		for (int i = 0; i < pNumSubSymbol; i++)
		{
			int lCNumSubsymbol = scores.get(i).size();
			for (int j = lCNumSubsymbol - 1; j >= 0; j--)
			{
				scores.get(i).get(j).replaceAll(e -> e / 2);
				ArrayList<Double> sameRC = new ArrayList<Double>(scores.get(i).get(j));
				scores.get(i).add(j + 1, sameRC);
			}

		}
		if (parent != 0)
			for (int i = pNumSubSymbol - 1; i >= 0; i--)
			{
				ArrayList<ArrayList<Double>> sameFather = new ArrayList<ArrayList<Double>>();
				for (int j = 0; j < scores.get(i).size(); j++)
				{
					ArrayList<Double> sameLRC = new ArrayList<Double>(scores.get(i).get(j));
					sameFather.add(sameLRC);
				}
				scores.add(i + 1, sameFather);
			}

		if (randomPerturbation)
		{
			double randomness = 1.0;
			int parentSplitFactor = parent == 0 ? 1 : 2;
			int lChildSplitFactor = 2;
			int rChildSplitFactor = 2;
			int lCNumSub_beforeSplit = scores.get(0).size() / 2;
			int rCNumSub_beforeSplit = scores.get(0).get(0).size() / 2;
			int pNumSub_beforeSplit = parentSplitFactor == 1 ? 1 : scores.size() / 2;
			for (short lcS = 0; lcS < lCNumSub_beforeSplit; lcS++)
			{
				for (short rcS = 0; rcS < rCNumSub_beforeSplit; rcS++)
				{
					for (short pS = 0; pS < pNumSub_beforeSplit; pS++)
					{
						scores.get(pS * parentSplitFactor).get(lcS * lChildSplitFactor);
						final double oldScore_beforeSplit = scores.get(pS * parentSplitFactor)
								.get(lcS * lChildSplitFactor).get(rcS * lChildSplitFactor) * 4;
						for (short p = 0; p < parentSplitFactor; p++)
						{
							double divFactor = lChildSplitFactor * rChildSplitFactor;
							// double randomValue = (random.nextDouble() + 0.25) * 0.8;
							double randomValue = (random.nextDouble() - 0.5);
							double randomComponentLC = oldScore_beforeSplit / divFactor * randomness / 100
									* randomValue;
							for (short lc = 0; lc < lChildSplitFactor; lc++)
							{
								if (lc == 1)
								{
									randomComponentLC = randomComponentLC * -1;
								}
								// double randomValue2 = (random.nextDouble() + 0.25) * 0.8;
								double randomValue2 = (random.nextDouble() - 0.5);
								double randomComponentRC = oldScore_beforeSplit / divFactor * randomness / 100
										* randomValue2;
								for (short rc = 0; rc < rChildSplitFactor; rc++)
								{
									if (rc == 1)
									{
										randomComponentRC = randomComponentRC * -1;
									}
									short newPS = (short) (parentSplitFactor * pS + p);
									short newLCS = (short) (lChildSplitFactor * lcS + lc);
									short newRCS = (short) (rChildSplitFactor * rcS + rc);
									double splitFactor = lChildSplitFactor * rChildSplitFactor;
									scores.get(newPS).get(newLCS).set(newRCS,
											oldScore_beforeSplit / splitFactor + randomComponentLC + randomComponentRC);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void merge(Short[][] symbolToMerge, double[][] weights)
	{
		if (symbolToMerge[parent] == null && symbolToMerge[leftChild] == null && symbolToMerge[rightChild] == null)
			return;
		// 合并rightchild
		if (symbolToMerge[rightChild] != null)
		{
			int nRCToMerge = symbolToMerge[rightChild].length;
			for (int indexRCToMerge = nRCToMerge - 1; indexRCToMerge >= 0; indexRCToMerge--)
			{
				short indexRC = symbolToMerge[rightChild][indexRCToMerge];
				for (int indexP = 0; indexP < scores.size(); indexP++)
				{
					for (int indexLC = 0; indexLC < scores.get(0).size(); indexLC++)
					{
						double scoreP2LCRC1 = scores.get(indexP).get(indexLC).get(indexRC);
						double scoreP2LCRC2 = scores.get(indexP).get(indexLC).get(indexRC + 1);
						scores.get(indexP).get(indexLC).set(indexRC, scoreP2LCRC1 + scoreP2LCRC2);
						scores.get(indexP).get(indexLC).remove(indexRC + 1);
					}
				}
			}
		}

		// 合并leftChild
		if (symbolToMerge[leftChild] != null)
		{
			int nLCToMerge = symbolToMerge[leftChild].length;
			for (int indexLCToMerge = nLCToMerge - 1; indexLCToMerge >= 0; indexLCToMerge--)
			{
				int indexLC = symbolToMerge[leftChild][indexLCToMerge];
				for (int indexP = 0; indexP < scores.size(); indexP++)
				{
					ArrayList<Double> scoresP2LC1 = scores.get(indexP).get(indexLC);
					ArrayList<Double> scoresP2LC2 = scores.get(indexP).get(indexLC + 1);
					for (int indexRC = 0; indexRC < scoresP2LC1.size(); indexRC++)
					{
						scoresP2LC1.set(indexRC, scoresP2LC1.get(indexRC) + scoresP2LC2.get(indexRC));
					}
					scores.get(indexP).remove(indexLC + 1);
				}
			}
		}

		// 合并parent
		if (symbolToMerge[parent] != null)
		{
			int nPToMerge = symbolToMerge[parent].length;
			for (int indexPToMerge = nPToMerge - 1; indexPToMerge >= 0; indexPToMerge--)
			{
				int indexP = symbolToMerge[parent][indexPToMerge];
				ArrayList<ArrayList<Double>> scoresP1 = scores.get(indexP);
				ArrayList<ArrayList<Double>> scoresP2 = scores.get(indexP + 1);
				for (int indexLC = 0; indexLC < scoresP1.size(); indexLC++)
				{
					ArrayList<Double> scoresP1ToLC = scoresP1.get(indexLC);
					ArrayList<Double> scoresP2ToLC = scoresP2.get(indexLC);
					for (int indexRC = 0; indexRC < scoresP1ToLC.size(); indexRC++)
					{
						// 合并parent的subSymbol时需要赋予规则概率权重
						scoresP1ToLC.set(indexRC, scoresP1ToLC.get(indexRC) * weights[parent][indexP]
								+ scoresP2ToLC.get(indexRC) * weights[parent][indexP + 1]);
					}
				}
				scores.remove(indexP + 1);
			}
		}
	}

	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + leftChild;
		result = prime * result + rightChild;
		return result;
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BinaryRule other = (BinaryRule) obj;
		if (leftChild != other.leftChild)
			return false;
		if (rightChild != other.rightChild)
			return false;
		return true;
	}

	public short getLeftChild()
	{
		return leftChild;
	}

	public void setLeftChild(short leftChild)
	{
		this.leftChild = leftChild;
	}

	public short getRightChild()
	{
		return rightChild;
	}

	public void setRightChild(short rightChild)
	{
		this.rightChild = rightChild;
	}

	public double getScore(short subP, short subLC, short subRC)
	{
		return scores.get(subP).get(subLC).get(subRC);
	}

	public void setScore(short subP, short subLC, short subRC, double score)
	{
		scores.get(subP).get(subLC).set(subRC, score);
	}
	
	public void initScores(short pNumSub, short lCNumSub, short rCNumSub)
	{
		for (short i = 0; i < pNumSub; i++)
		{
			scores.add(new ArrayList<ArrayList<Double>>());
			for (short j = 0; j < lCNumSub; j++)
			{
				scores.get(i).add(new ArrayList<Double>());
				for (short k = 0; k < rCNumSub; k++)
				{
					scores.get(i).get(j).add(0.0);
				}
			}
		}
	}
	
	@Override
	boolean withIn(HashSet<? extends Rule> rules)
	{
		if (rules.contains(this))
			return true;
		else
			return false;
	}

	public static void main(String[] args)
	{
		HashSet<BinaryRule> set = new HashSet<BinaryRule>();
		BinaryRule bRule = new BinaryRule((short) 1, (short) 2, (short) 3);
		bRule.withIn(set);
	}

	public String[] toStringRules(Grammar g)
	{
		String[] strs = new String[scores.size() * scores.get(0).size() * scores.get(0).get(0).size()];
		int count = 0;
		for (int i = 0; i < scores.size(); i++)
		{
			for (int j = 0; j < scores.get(0).size(); j++)
			{
				for (int k = 0; k < scores.get(0).get(0).size(); k++)
				{
					String parentStr;
					String lChildStr;
					String rChildStr;
					if (g.getNumSubSymbol(parent) == 1)
						parentStr = g.symbolStrValue(parent);
					else
						parentStr = g.symbolStrValue(parent) + "_" + i;
					
					if (g.getNumSubSymbol(leftChild) == 1)
						lChildStr = g.symbolStrValue(leftChild);
					else
						lChildStr = g.symbolStrValue(leftChild) + "_" + j;
					
					if (g.getNumSubSymbol(rightChild) == 1)
						rChildStr = g.symbolStrValue(rightChild);
					else
						rChildStr = g.symbolStrValue(rightChild) + "_" + k;
					
					String str = parentStr + " -> " + lChildStr + " " + rChildStr + " " + scores.get(i).get(j).get(k);
					strs[count] = str;
					
					count++;
				}
			}
		}
		return strs;
	}

	public String toStringIgnoreSubSymbol(Grammar g)
	{
		String parentStr = g.symbolStrValue(parent);
		String lChildStr = g.symbolStrValue(leftChild);
		String rChildStr = g.symbolStrValue(rightChild);
		return parentStr + " -> " + lChildStr + " " + rChildStr;
	}

	public TreeMap<String, Double> getParent_i_ScoceSum(Grammar g)
	{
		TreeMap<String, Double> A_iBCRuleSum = new TreeMap<>();

		for (int i = 0; i < scores.size(); i++)
		{
			String parentStr;
			if (scores.size() == 1)
			{
				parentStr = g.symbolStrValue(parent);
			}
			else
			{
				parentStr = g.symbolStrValue(parent) + "_" + i;
			}
			double A_iScore = 0.0;
			for (int j = 0; j < scores.get(0).size(); j++)
			{
				for (int k = 0; k < scores.get(0).get(0).size(); k++)
				{
					A_iScore = A_iScore + scores.get(i).get(j).get(k);
				}
			}
			A_iBCRuleSum.put(parentStr, A_iScore);
		}

		return A_iBCRuleSum;
	}

	public double getParent_i_ScoceSum(short subParentIndex)
	{
		double A_iScore = 0.0;
		for (int j = 0; j < scores.get(0).size(); j++)
		{
			for (int k = 0; k < scores.get(0).get(0).size(); k++)
			{
				A_iScore = A_iScore + scores.get(subParentIndex).get(j).get(k);
			}
		}
		return A_iScore;
	}
}
