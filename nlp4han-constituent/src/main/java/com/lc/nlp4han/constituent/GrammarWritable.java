package com.lc.nlp4han.constituent;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * 文法模型读写接口
 * 
 * 所有文法模型CFG、PCFG、LexCFG等实现该接口
 * 
 * @author 刘小峰
 *
 */
public interface GrammarWritable
{
	/**
	 * 将文法模型写到流中
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void write(DataOutput out) throws IOException;
	
	/**
	 * 从流从读入文法模型
	 * 
	 * @param in
	 * @throws IOException
	 */
	public void read(DataInput in) throws IOException;
}
