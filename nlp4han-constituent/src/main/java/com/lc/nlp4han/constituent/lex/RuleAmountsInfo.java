package com.lc.nlp4han.constituent.lex;

/**
 * 在模型中描述某实体类的数目和其子类型数目
 * 
 * @author qyl
 *
 */
public class RuleAmountsInfo
{
	private int amount = 0;
	private int subtypeAmount = 0;

	public RuleAmountsInfo(int amount, int subtypeAmount)
	{
		this.amount = amount;
		this.subtypeAmount = subtypeAmount;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	public int getSubtypeAmount()
	{
		return subtypeAmount;
	}

	public void setSubtypeAmount(int subtypeAmount)
	{
		this.subtypeAmount = subtypeAmount;
	}

	public void addAmount(int num)
	{
		amount += num;
	}

	public void addSubtypeAmount(int num)
	{
		subtypeAmount += num;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + amount;
		result = prime * result + subtypeAmount;
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
		RuleAmountsInfo other = (RuleAmountsInfo) obj;
		if (amount != other.amount)
			return false;
		if (subtypeAmount != other.subtypeAmount)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return amount + " " + subtypeAmount;
	}

}
