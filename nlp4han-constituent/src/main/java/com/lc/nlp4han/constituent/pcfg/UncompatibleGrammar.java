package com.lc.nlp4han.constituent.pcfg;

import java.io.IOException;

/**
 * 文法模型不匹配异常
 * 
 * 两个句法解析器有特定的文法模型，不能乱用。
 *
 */
public class UncompatibleGrammar extends IOException
{
	private static final long serialVersionUID = 1L;

}
