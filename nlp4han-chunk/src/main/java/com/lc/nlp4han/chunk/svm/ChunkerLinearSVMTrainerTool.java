package com.lc.nlp4han.chunk.svm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lc.nlp4han.chunk.svm.liblinear.InvalidInputDataException;
import com.lc.nlp4han.chunk.svm.liblinear.Train;

public class ChunkerLinearSVMTrainerTool
{
	private static final String USAGE = "Usage: ChunkerLinearSVMTrainerTool [options] -data training_set_file\n"
			+ "options:\n" + "-encoding encoding : set encoding\n" + "-label label : such as BIOE, BIOES\n"
			+ "-model model_path : set the model save path\n" + "-s type : set type of solver (default 1)%n"
			+ "  for multi-class classification%n" + "    0 -- L2-regularized logistic regression (primal)%n"
			+ "    1 -- L2-regularized L2-loss support vector classification (dual)%n"
			+ "    2 -- L2-regularized L2-loss support vector classification (primal)%n"
			+ "    3 -- L2-regularized L1-loss support vector classification (dual)%n"
			+ "    4 -- support vector classification by Crammer and Singer%n"
			+ "    5 -- L1-regularized L2-loss support vector classification%n"
			+ "    6 -- L1-regularized logistic regression%n" + "    7 -- L2-regularized logistic regression (dual)%n"
			+ "  for regression%n" + "   11 -- L2-regularized L2-loss support vector regression (primal)%n"
			+ "   12 -- L2-regularized L2-loss support vector regression (dual)%n"
			+ "   13 -- L2-regularized L1-loss support vector regression (dual)%n"
			+ "-c cost : set the parameter C (default 1)%n"
			+ "-p epsilon : set the epsilon in loss function of SVR (default 0.1)%n"
			+ "-e epsilon : set tolerance of termination criterion%n" + "   -s 0 and 2%n"
			+ "       |f'(w)|_2 <= eps*min(pos,neg)/l*|f'(w0)|_2,%n"
			+ "       where f is the primal function and pos/neg are # of%n"
			+ "       positive/negative data (default 0.01)%n" + "   -s 11%n"
			+ "       |f'(w)|_2 <= eps*|f'(w0)|_2 (default 0.001)%n" + "   -s 1, 3, 4 and 7%n"
			+ "       Dual maximal violation <= eps; similar to libsvm (default 0.1)%n" + "   -s 5 and 6%n"
			+ "       |f'(w)|_1 <= eps*min(pos,neg)/l*|f'(w0)|_1,%n"
			+ "       where f is the primal function (default 0.01)%n" + "   -s 12 and 13%n"
			+ "       |f'(alpha)|_1 <= eps |f'(alpha0)|,%n" + "       where f is the dual function (default 0.1)%n"
			+ "-B bias : if bias >= 0, instance x becomes [x; bias]; if < 0, no bias term added (default -1)%n"
			+ "-wi weight: weights adjust the parameter C of different classes (see README for details)%n"
			+ "-C : find parameter C (only for -s 0 and 2)%n" + "-q : quiet mode (no outputs)%n";

	public static void main(String[] args) throws IOException, InvalidInputDataException
	{
		long startTime = System.currentTimeMillis();
		Map<String, String[]> as = decomposeArgs(args);

		String[] inputArgs = as.get("input");
		
		// 转换和保存训练样本
		SVMFeatureLabelInfo ci = SVMSampleUtil.convert(inputArgs);

		System.out.println("类别总数：" + ci.getClassesNumber());
		System.out.println("样本总数：" + ci.getSamplesNumber());
		System.out.println("特征总数：" + ci.getFeaturesNumber());

		String[] trainArgs = as.get("train");

		// 调用SVM框架训练，并保存训练模型
		Train.main(trainArgs);

		long endTime = System.currentTimeMillis();
		System.out.println("共耗时：" + (endTime - startTime) * 1.0 / 60000 + "mins");
	}

	/**
	 * 分解参数
	 */
	private static Map<String, String[]> decomposeArgs(String[] args)
	{
		Map<String, String[]> result = new HashMap<>();
		List<String> standardInputArgs = new ArrayList<String>();
		List<String> trainArgs = new ArrayList<String>();

		String dataPath = null;
		String modelPath = null;

		for (int i = 0; i < args.length; i++)
		{
			switch (args[i])
			{
			/*************** 格式转换 ****************/
			case "-data":
				standardInputArgs.add(args[i]);
				standardInputArgs.add(args[i + 1]);
				dataPath = args[i + 1];
				i++;
				break;
			case "-encoding":
				standardInputArgs.add(args[i]);
				standardInputArgs.add(args[i + 1]);
				i++;
				break;
			case "-label":
				standardInputArgs.add(args[i]);
				standardInputArgs.add(args[i + 1]);
				i++;
				break;
			/*************** 训练参数 ****************/
			case "-s":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-c":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-p":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-e":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-B":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-C":
				trainArgs.add(args[i]);
				break;
			case "-q":
				trainArgs.add(args[i]);
				break;
			case "-model":
				modelPath = args[i + 1];
				i++;
				break;
			default:
				if (args[i].startsWith("-w"))
				{
					trainArgs.add(args[i]);
					trainArgs.add(args[i + 1]);
					i++;
				}
				else
				{
					System.err.println(USAGE);
					System.exit(1);
				}
			}
		}

		if (dataPath == null)
		{
			System.err.println(USAGE);
			System.exit(1);
		}

		final java.nio.file.Path docDir = java.nio.file.Paths.get(dataPath);

		if (!java.nio.file.Files.isReadable(docDir))
		{
			System.out.println("Document directory '" + docDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		String filePath = dataPath + ".svm";
		
		standardInputArgs.add("-save");
		standardInputArgs.add(filePath);
		
		trainArgs.add(filePath);
		if (modelPath != null)
			trainArgs.add(modelPath);
		
		result.put("input", standardInputArgs.toArray(new String[standardInputArgs.size()]));
		result.put("train", trainArgs.toArray(new String[trainArgs.size()]));
		
		return result;
	}

}
