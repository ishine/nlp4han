package com.lc.nlp4han.ml.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StringInputStreamFactory implements InputStreamFactory
{
	private String str; 
	
	public StringInputStreamFactory(String str)
	{
		this.str = str;
	}
	
	@Override
	public InputStream createInputStream() throws IOException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
		return bais;
	}

}
