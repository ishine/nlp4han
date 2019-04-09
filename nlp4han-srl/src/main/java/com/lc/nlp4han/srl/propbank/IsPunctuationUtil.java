package com.lc.nlp4han.srl.propbank;

import java.util.ArrayList;
import java.util.List;

/**
 * 判断是否是标点符号的工具类
 * @author 王馨苇
 *
 */
public class IsPunctuationUtil {

	/**
	 * 判断是否是标点符号
	 * @param str 要判断的字符串
	 * @return
	 */
	public static boolean isPunctuation(String str){
		List<String> list = new ArrayList<>();
		list.add(":");
		list.add(",");
		list.add(".");
		list.add("''");
		list.add("``");
		return list.contains(str);
	}
}
