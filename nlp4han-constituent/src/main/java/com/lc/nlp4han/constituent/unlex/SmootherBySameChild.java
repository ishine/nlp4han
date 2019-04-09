package com.lc.nlp4han.constituent.unlex;

/**
 * 通过不同父亲（原始符号相同）相同的孩子（原始符号和字符号的后缀也相同）的派生规则来平滑规则概率
 * 
 * @author 王宁
 */
public class SmootherBySameChild extends Smoother
{
	public static int smoothType = 1;

	public SmootherBySameChild(double same)
	{
		super(same);

	}

	@Override
	protected BinaryRule smoothBRule(BinaryRule bRule, short nSubSymP, short nSubSymLC, short nSubSymRC)
	{
		if (nSubSymP == 1)
			return bRule;
		
		double[][] scoreAverage = new double[nSubSymLC][];
		for (short subLC = 0; subLC < nSubSymLC; subLC++)
		{
			for (short subRC = 0; subRC < nSubSymRC; subRC++)
			{
				for (short subP = 0; subP < nSubSymP; subP++)
				{
					if (scoreAverage[subLC] == null)
						scoreAverage[subLC] = new double[nSubSymRC];

					scoreAverage[subLC][subRC] += bRule.getScore(subP, subLC, subRC) / nSubSymP;
				}
			}
		}
		
		for (short subP = 0; subP < nSubSymP; subP++)
		{
			for (short subLC = 0; subLC < nSubSymLC; subLC++)
			{
				for (short subRC = 0; subRC < nSubSymRC; subRC++)
					bRule.setScore(subP, subLC, subRC,
							bRule.getScore(subP, subLC, subRC) * same + scoreAverage[subLC][subRC] * diff);
			}
		}
		
		return bRule;
	}

	@Override
	protected UnaryRule smoothURule(UnaryRule uRule, short nSubSymP, short nSubSymC)
	{
		if (nSubSymP == 1)
			return uRule;
		
		double[] scoreAverage = new double[nSubSymC];
		for (short subC = 0; subC < nSubSymC; subC++)
		{
			for (short subP = 0; subP < nSubSymP; subP++)
				scoreAverage[subC] += uRule.getScore(subP, subC) / nSubSymP;
		}
		
		for (short subP = 0; subP < nSubSymP; subP++)
		{
			for (short subC = 0; subC < nSubSymC; subC++)
				uRule.setScore(subP, subC,
						uRule.getScore(subP, subC) * (1 - diff * 10) + scoreAverage[subC] * diff * 10);
		}
		return uRule;
	}

	@Override
	protected PreterminalRule smoothPreRule(PreterminalRule preRule, short nSubSymP)
	{
		if (nSubSymP == 1)
			return preRule;
		
		double scoreAverage = 0.0;
		for (short subP = 0; subP < nSubSymP; subP++)
			scoreAverage += preRule.getScore(subP) / nSubSymP;
		
		for (short subP = 0; subP < nSubSymP; subP++)
			preRule.setScore(subP, preRule.getScore(subP) * same + scoreAverage * diff);

		return preRule;
	}
}
