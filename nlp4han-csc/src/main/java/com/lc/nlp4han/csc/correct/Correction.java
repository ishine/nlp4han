package com.lc.nlp4han.csc.correct;

/**
 * 纠正的字和位置
 */
public class Correction
{

	/**
	 * 纠正的字
	 */
	private String character;

	/**
	 * 纠正的位置
	 */
	private int location;

	public Correction(String character, int location)
	{
		this.character = character;
		this.location = location;
	}

	public int getLocation()
	{
		return location;
	}

	public String getCharacter()
	{
		return character;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((character == null) ? 0 : character.hashCode());
		result = prime * result + location;
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
		Correction other = (Correction) obj;
		if (character == null)
		{
			if (other.character != null)
				return false;
		}
		else if (!character.equals(other.character))
			return false;
		if (location != other.location)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return location + ", " + character;
	}
}
