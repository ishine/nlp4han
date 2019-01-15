package org.nlp4han.coref.hobbs;

import java.util.HashSet;
import java.util.Set;

/**
 * Mention属性类
 * 
 * @author 杨智超
 *
 */
public class Attribute
{

	public Attribute()
	{

	}

	/**
	 * 性别： MALE——男性；FEMALE——女性；NONE——无性别；
	 */
	public enum Gender
	{
		MALE, FEMALE, NONE;
	}

	/**
	 * 数量： SINGULAR——单数；PLURAL——复数；
	 */
	public enum Number
	{
		SINGULAR, PLURAL;
	}

	/**
	 * 动物性： ANI_ANIMAL——动物；INANIMACY——非动物；ANI_HUMAN——人；
	 */
	public enum Animacy
	{
		INANIMACY, ANI_HUMAN, ANI_ANIMAL;
	}

	/**
	 * 人称：
	 */
	public enum Person
	{
		FIRST, SECOND, THIRD;
	}

	private Set<Gender> gender = new HashSet<Gender>();
	private Set<Number> number = new HashSet<Number>();
	private Set<Animacy> animacy = new HashSet<Animacy>();
	private Set<Person> person = new HashSet<Person>();

	public Set<Gender> getGender()
	{
		return gender;
	}

	public void setGender(Set<Gender> gender)
	{
		this.gender = gender;
	}

	public Set<Number> getNumber()
	{
		return number;
	}

	public void setNumber(Set<Number> number)
	{
		this.number = number;
	}

	public Set<Animacy> getAnimacy()
	{
		return animacy;
	}

	public void setAnimacy(Set<Animacy> animacy)
	{
		this.animacy = animacy;
	}

	public Set<Person> getPerson()
	{
		return person;
	}

	public void setPerson(Set<Person> person)
	{
		this.person = person;
	}

	@Override
	public String toString()
	{
		return "[gender=" + gender + ", number=" + number + ", animacy=" + animacy + ", person=" + person + "]";
	}

}
