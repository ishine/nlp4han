package com.lc.nlp4han.chunk.svm;

import java.io.IOException;
import java.io.InputStream;

import com.lc.nlp4han.chunk.svm.libsvm.svm_model;

public class ChunkerLibSVM extends ChunkerSVM
{
	private svm_model model = null;

	@Override
	public double predictOneLine(String line, Object model) throws IOException
	{
		svm_model svmModel = (svm_model) model;
		return SVMPredict.predict(line, svmModel, 0);
	}

	@Override
	public void train(String[] arg) throws IOException
	{
		svm_train.main(arg);
	}

	@Override
	public void setModel(String modelPath) throws IOException
	{
		this.model = ModelLoadingUtil.loadLibSVMModelFromDisk(modelPath);
	}

	@Override
	public void setModel(Object model)
	{
		this.model = (svm_model) model;
	}

	@Override
	public Object getModel()
	{
		return this.model;
	}

	@Override
	public void setModel(InputStream input) throws IOException
	{
		this.model = ModelLoadingUtil.loadLibSVMModel(input);
	}
}
