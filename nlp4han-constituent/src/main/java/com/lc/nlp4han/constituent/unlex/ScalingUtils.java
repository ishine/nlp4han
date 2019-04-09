package com.lc.nlp4han.constituent.unlex;

/**
 * 用来对数据缩放,任何一个进行缩放了的数据都由缩放后大小和缩放比例组成。所以一个缩放后的数据的原始数据=缩放后大小*缩放比例。 缩放比例 =
 * Math.exp(100)^scaleFactor.所以原始数据 = 缩放后大小*(Math.exp(100)^scaleFactor)。
 * 
 * @author 王宁
 */
public class ScalingUtils
{
	public static double base = Math.exp(100);

	/**
	 * 计算缩放的真实比例
	 * 
	 * @param 缩放比例
	 * @return
	 */
	public static double calcScaleFactor(double logScale)
	{
		if (logScale == Integer.MIN_VALUE || logScale == Integer.MAX_VALUE)
			return 0.0;

		return Math.pow(base, logScale);
	}

	/**
	 * 将已经缩放的数据进一步缩放，一组数据依照最大的数据的缩放比例进行缩放
	 * 
	 * @param scores
	 *            缩放后的数据
	 * @param previousScale
	 *            之前缩放的比例,之前没有进行缩放则为0
	 * @return 进一步缩放后完整的缩放比例的log值（以Math.exp(100)为底）
	 */
	public static int scaleArray(int previousScale, Double... scores)
	{
		if (previousScale == Integer.MIN_VALUE || previousScale == Integer.MAX_VALUE)
			return previousScale;

		int logScale = 0;// 本次缩放的真实比例的log值
		double scale = 1.0;// 本次缩放的真实比例
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < scores.length; i++)
		{
			if (scores[i] > max)
				max = scores[i];
		}
		
		if (max == Double.POSITIVE_INFINITY)
			return 0;

		if (max == 0)
			return previousScale;
		
		while (max > base)
		{
			max /= base;
			scale *= base;
			logScale += 1;
		}
		
		while (max > 0.0 && max < 1.0 / base)
		{
			max *= base;
			scale /= base;
			logScale -= 1;
		}
		
		if (logScale != 0)
		{
			for (int i = 0; i < scores.length; i++)
				scores[i] /= scale;
		}
		
		return previousScale + logScale;
	}
}
