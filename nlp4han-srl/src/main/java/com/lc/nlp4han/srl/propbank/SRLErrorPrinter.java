package com.lc.nlp4han.srl.propbank;

import java.io.OutputStream;
import java.io.PrintStream;

import com.lc.nlp4han.constituent.HeadTreeNode;

public class SRLErrorPrinter extends SRLEvaluateMonitor{
    private PrintStream errOut;
	
	public SRLErrorPrinter(OutputStream out){
		errOut = new PrintStream(out);
	}
	
	/**
	 * 样本和预测的不一样的时候进行输出
	 * @param ref 参考的样本
	 * @param pre 预测的结果
	 */
	@Override
	public void missclassified(SRLSample<HeadTreeNode> ref, SRLSample<HeadTreeNode> pre) {
		 SRLTreeNode srlref = TreeToSRLTree.treeToSRLTree(ref.getTree(), ref.getArgumentTree(), ref.getLabelInfo());
		 SRLTreeNode srlpre = TreeToSRLTree.treeToSRLTree(pre.getTree(), pre.getArgumentTree(), pre.getLabelInfo());
		 String strref = SRLTreeNode.printSRLBracket(srlref);
		 String strpre = SRLTreeNode.printSRLBracket(srlpre);
		 
		 errOut.println("样本的结果：");
		 errOut.println(strref);
		 errOut.println();
		 errOut.println("预测的结果：");
		 errOut.println(strpre);
		 errOut.println();
	}
}
