package com.lc.nlp4han.chunk.svm;

import java.io.IOException;
import java.io.InputStream;

import com.lc.nlp4han.chunk.svm.liblinear.InvalidInputDataException;
import com.lc.nlp4han.chunk.svm.liblinear.Model;
import com.lc.nlp4han.chunk.svm.liblinear.PredictLinear;
import com.lc.nlp4han.chunk.svm.liblinear.Train;

/**
 * 基于SVMLinear的组块分析器
 *
 */
public class ChunkerLinearSVM extends ChunkerSVM
{
	Model model = null;

	@Override
	public double predictOneLine(String line, Object model) throws IOException
	{
		Model modelLinear = (Model) model;
		return PredictLinear.doPredict(line, modelLinear);

	}

	@Override
	public void train(String[] arg) throws IOException
	{
		try
		{
			Train.main(arg);
		}
		catch (IOException | InvalidInputDataException e)
		{
			throw new IOException(e);
		}
	}

	@Override
	public void setModel(String modelPath) throws IOException
	{
		this.model = ModelLoadingUtil.loadLinearSVMModelFromDisk(modelPath);
	}

	@Override
	public void setModel(Object model)
	{
		this.model = (Model) model;
	}

	@Override
	public Object getModel()
	{
		return this.model;
	}

	@Override
	public void setModel(InputStream input) throws IOException
	{
		this.model = ModelLoadingUtil.loadLinearSVMModel(input);
	}
}
