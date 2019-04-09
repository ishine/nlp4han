package com.lc.nlp4han.srl.propbank;

import com.lc.nlp4han.constituent.HeadTreeNode;
import com.lc.nlp4han.ml.util.SequenceValidator;

/**
 * 校验
 * @author 王馨苇
 *
 */
public class DefaultSRLSequenceValidator implements SequenceValidator<TreeNodeWrapper<HeadTreeNode>>{

	@Override
	public boolean validSequence(int arg0, TreeNodeWrapper<HeadTreeNode>[] arg1, String[] arg2, String arg3) {
		return true;
	}
}
