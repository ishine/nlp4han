package com.lc.nlp4han.constituent.unlex;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

/**
 * 一元规则
 * 
 * @author 王宁
 */
public class UnaryRule extends Rule
{
	private short child;
	private ArrayList<ArrayList<Double>> scores = new ArrayList<ArrayList<Double>>();// 保存规则例如Ai -> Bj 的概率

	public UnaryRule(short parent, short child)
	{
		super.parent = parent;
		this.child = child;
	}

	public UnaryRule(short parent, short nSubP, short child, short nSubC)
	{
		super.parent = parent;
		this.child = child;
		for (int i = 0; i < nSubP; i++)
		{
			ArrayList<Double> c = new ArrayList<Double>();
			for (int j = 0; j < nSubC; j++)
			{
				c.add(0.0);
			}
			scores.add(c);
		}
	}

	public void setSubRuleScore(short indexSubP, short indexSubC, double score)
	{
		scores.get(indexSubP).set(indexSubC, score);
	}

	@Override
	public void split()
	{
		Random random = GrammarExtractor.random;
		boolean randomPerturbation = true;
		// split child
		int pNumSubSymbol = scores.size();
		for (int i = 0; i < pNumSubSymbol; i++)
		{
			ArrayList<Double> sameFather = scores.get(i);// Father均为A_i的scores
			int cNumSubSymbol = sameFather.size();
			for (int j = cNumSubSymbol - 1; j >= 0; j--)
			{
				sameFather.add(j + 1, sameFather.get(j) / 2);
				sameFather.set(j, sameFather.get(j + 1));
			}
			scores.set(i, sameFather);
		}

		// split father
		if (parent != 0)
			for (int i = pNumSubSymbol - 1; i >= 0; i--)
			{
				ArrayList<Double> sameChild = new ArrayList<>(scores.get(i));
				scores.add(i + 1, sameChild);
			}
		
		if (randomPerturbation)
		{
			double randomness = 1.0;
			int parentSplitFactor = parent == 0 ? 1 : 2;
			int childSplitFactor = 2;
			int cNumSub_beforeSplit = scores.get(0).size() / 2;
			int pNumSub_beforeSplit = parentSplitFactor == 1 ? 1 : scores.size() / 2;
			for (short cS = 0; cS < cNumSub_beforeSplit; cS++)
			{
				for (short pS = 0; pS < pNumSub_beforeSplit; pS++)
				{
					final double oldScore_beforeSplit = scores.get(pS * parentSplitFactor).get(cS * childSplitFactor)
							* 2;
					for (short p = 0; p < parentSplitFactor; p++)
					{
						double divFactor = childSplitFactor;
						// double randomValue = (random.nextDouble() + 0.25) * 0.8;
						double randomValue = (random.nextDouble() - 0.5);
						double randomComponent = oldScore_beforeSplit / divFactor * randomness / 100.0 * randomValue;
						for (short c = 0; c < childSplitFactor; c++)
						{
							if (c == 1)
							{
								randomComponent = randomComponent * -1;
							}
							short newPS = (short) (parentSplitFactor * pS + p);
							short newLCS = (short) (childSplitFactor * cS + c);
							double splitFactor = childSplitFactor;
							scores.get(newPS).set(newLCS, oldScore_beforeSplit / splitFactor + randomComponent);
						}
					}
				}
			}
		}
	}

	@Override
	public void merge(Short[][] symbolToMerge, double[][] weights)
	{
		if (symbolToMerge[parent] == null && symbolToMerge[child] == null)
			return;
		// 合并child
		if (symbolToMerge[child] != null)
		{
			int nCToMerge = symbolToMerge[child].length;
			for (int indexCToMerge = nCToMerge - 1; indexCToMerge >= 0; indexCToMerge--)
			{
				short indexC = symbolToMerge[child][indexCToMerge];
				for (int indexP = 0; indexP < scores.size(); indexP++)
				{

					double scoreP2C1 = scores.get(indexP).get(indexC);
					double scoreP2C2 = scores.get(indexP).get(indexC + 1);
					scores.get(indexP).set(indexC, scoreP2C1 + scoreP2C2);
					scores.get(indexP).remove(indexC + 1);

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
				ArrayList<Double> scoresP1 = scores.get(indexP);
				ArrayList<Double> scoresP2 = scores.get(indexP + 1);
				for (int indexC = 0; indexC < scoresP1.size(); indexC++)
				{
					double scoresP1ToC = scoresP1.get(indexC);
					double scoresP2ToC = scoresP2.get(indexC);

					// 合并parent的subSymbol时需要赋予规则概率权重
					scoresP1.set(indexC,
							scoresP1ToC * weights[parent][indexP] + scoresP2ToC * weights[parent][indexP + 1]);

				}
				scores.remove(indexP + 1);
			}
		}
	}

	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + child;
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
		UnaryRule other = (UnaryRule) obj;
		if (child != other.child)
			return false;
		return true;
	}

	public short getChild()
	{
		return child;
	}

	public void setChild(short child)
	{
		this.child = child;
	}

	public double getScore(short subP, short subC)
	{
		return scores.get(subP).get(subC);
	}

	public void setScore(short subP, short subC, double score)
	{
		scores.get(subP).set(subC, score);
	}

	public void initScores(short nSubP, short nSubC)
	{
		for (short subP = 0; subP < nSubP; subP++)
		{
			scores.add(new ArrayList<Double>());
			for (short subC = 0; subC < nSubC; subC++)
			{
				scores.get(subP).add(0.0);
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

	public String toStringIgnoreSubSymbol(Grammar g)
	{
		String parentStr = g.symbolStrValue(parent);
		String childStr = g.symbolStrValue(child);
		return parentStr + " -> " + childStr;
	}

	@Override
	public String[] toStringRules(Grammar g)
	{
		String[] strs = new String[scores.size() * scores.get(0).size()];
		int count = 0;
		for (int i = 0; i < scores.size(); i++)
		{
			for (int j = 0; j < scores.get(0).size(); j++)
			{
				String parentStr;
				String childStr;
				if (g.getNumSubSymbol(parent) == 1)
					parentStr = g.symbolStrValue(parent);
				else
					parentStr = g.symbolStrValue(parent) + "_" + i;
				
				if (g.getNumSubSymbol(child) == 1)
					childStr = g.symbolStrValue(child);
				else
					childStr = g.symbolStrValue(child) + "_" + j;
				
				String str = parentStr + " -> " + childStr + " " + scores.get(i).get(j);
				strs[count] = str;
				
				count++;
			}
		}
		return strs;
	}


	public TreeMap<String, Double> getParent_i_ScoceSum(Grammar g)
	{
		TreeMap<String, Double> A_iBRuleSum = new TreeMap<>();
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
			double A_iScore = getParent_i_ScoceSum((short) i);
			A_iBRuleSum.put(parentStr, A_iScore);
		}
		return A_iBRuleSum;
	}

	public double getParent_i_ScoceSum(short subParentIndex)
	{
		double A_iScore = 0.0;
		for (int j = 0; j < scores.get(0).size(); j++)
		{
			A_iScore = A_iScore + scores.get(subParentIndex).get(j);
		}
		return A_iScore;
	}
}
