package com.lc.nlp4han.chunk.svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.lc.nlp4han.chunk.svm.liblinear.Linear;
import com.lc.nlp4han.chunk.svm.liblinear.Model;
import com.lc.nlp4han.chunk.svm.libsvm.svm;
import com.lc.nlp4han.chunk.svm.libsvm.svm_model;

public class ModelLoadingUtil
{
	public static svm_model loadLibSVMModelFromDisk(String filePath) throws IOException
	{
		svm_model model = svm.svm_load_model(filePath);
		return model;
	}
	
	public static svm_model loadLibSVMModelFromResources(String fileName) throws IOException
	{
		InputStream stream = ModelLoadingUtil.class.getClassLoader().getResourceAsStream(fileName);
		
		return loadLibSVMModel(stream);
	}
	
	public static svm_model loadLibSVMModel(InputStream input) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		
		svm_model model = svm.svm_load_model(br);
		
		return model;
	}
	
	public static Model loadLinearSVMModelFromDisk(String filePath) throws IOException
	{
		Linear.loadModel(new File(filePath));
		return Model.load(new File(filePath));
		
	}
	
	public static Model loadLinearSVMModelFromResources(String fileName) throws IOException
	{
		InputStream stream = ModelLoadingUtil.class.getClassLoader().getResourceAsStream(fileName);
		
		return loadLinearSVMModel(stream);
	}
	
	public static Model loadLinearSVMModel(InputStream input) throws IOException
	{
		Reader r = new InputStreamReader(input);
		
		return Model.load(r);
	}
}
