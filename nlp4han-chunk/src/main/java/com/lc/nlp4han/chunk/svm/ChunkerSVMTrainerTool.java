package com.lc.nlp4han.chunk.svm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lc.nlp4han.chunk.svm.liblinear.InvalidInputDataException;

public class ChunkerSVMTrainerTool
{
	private static final String USAGE = "Usage: ChunkAnalysisSVMTrainerTool [options] -data training_set_file\n"
			+ "options:\n" + "-encoding encoding : set encoding\n" + "-label label : such as BIOE, BIOES\n"
			+ "-model model_path : set the model save path\n" + "-s svm_type : set type of SVM (default 0)\n"
			+ "	0 -- C-SVC		(multi-class classification)\n" + "	1 -- nu-SVC		(multi-class classification)\n"
			+ "	2 -- one-class SVM\n" + "	3 -- epsilon-SVR	(regression)\n" + "	4 -- nu-SVR		(regression)\n"
			+ "-t kernel_type : set type of kernel function (default 2)\n" + "	0 -- linear: u'*v\n"
			+ "	1 -- polynomial: (gamma*u'*v + coef0)^degree\n" + "	2 -- radial basis function: exp(-gamma*|u-v|^2)\n"
			+ "	3 -- sigmoid: tanh(gamma*u'*v + coef0)\n"
			+ "	4 -- precomputed kernel (kernel values in training_set_file)\n"
			+ "-d degree : set degree in kernel function (default 3)\n"
			+ "-g gamma : set gamma in kernel function (default 1/num_features)\n"
			+ "-r coef0 : set coef0 in kernel function (default 0)\n"
			+ "-c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)\n"
			+ "-n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)\n"
			+ "-p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)\n"
			+ "-m cachesize : set cache memory size in MB (default 100)\n"
			+ "-e epsilon : set tolerance of termination criterion (default 0.001)\n"
			+ "-h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1)\n"
			+ "-b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)\n"
			+ "-wi weight : set the parameter C of class i to weight*C, for C-SVC (default 1)\n"
			+ "-q : quiet mode (no outputs)\n";

	public static void main(String[] args) throws IOException, InvalidInputDataException
	{
		long startTime = System.currentTimeMillis();
		Map<String, String[]> as = decomposeArgs(args);

		String[] inputArgs = as.get("input");
		
		// 将语料转换成svm格式，并将转换后的样本写到文件
		SVMFeatureLabelInfo ci = SVMSampleUtil.convert(inputArgs);

		System.out.println("类别总数：" + ci.getClassesNumber());
		System.out.println("样本总数：" + ci.getSamplesNumber());
		System.out.println("特征总数：" + ci.getFeaturesNumber());

		String[] trainArgs = as.get("train");

		// 训练并写出模型
		svm_train.main(trainArgs);

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
			case "-t":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-d":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-g":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-r":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-n":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-m":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-c":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-e":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-p":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-h":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
				break;
			case "-b":
				trainArgs.add(args[i]);
				trainArgs.add(args[i + 1]);
				i++;
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
