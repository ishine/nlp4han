package com.lc.nlp4han.srl.propbank;

import com.lc.nlp4han.constituent.TreeNode;

/**
 * 判断当前子树是不是并列结构
 * @author 王馨苇
 *
 */
public class IsCordinateStructureUtil {

	/**
	 * 判断是否是并列结构
	 * @param tree 要判断的树
	 * @return
	 */
	public static boolean isCordinate(TreeNode tree){
		// X CC X
		// X , CC X [.,but.]
		// X : CC X
		// X , X
		// X : X [. ; .]
		if((tree.getFirstChildName().equals("''")) && (tree.getLastChildName().equals("''")) ||
				(tree.getFirstChildName().equals("``")) && (tree.getLastChildName().equals("``")) ||
				(tree.getFirstChildName().equals("''")) && (tree.getLastChildName().equals("``")) ||
				(tree.getFirstChildName().equals("``")) && (tree.getLastChildName().equals("''"))){
			if(tree.getChildrenNum() == 7){
				if(tree.getChildName(5).equals(".")){
					if(tree.getChild(1).equals(tree.getChild(4)) &&
							tree.getChildName(2).equals(",") && tree.getChildName(3).equals("CC")){
						return true;
					}
					if(tree.getChild(1).equals(tree.getChild(4)) &&
							tree.getChildName(2).equals(":") && tree.getChildName(3).equals("CC")){
						return true;
					}
				}
			}else if(tree.getChildrenNum() == 6){
				if(tree.getChildName(4).equals(".")){
					if(tree.getChildName(1).equals(tree.getChildName(3))){
						if(tree.getChildName(2).equals("CC") || tree.getChildName(2).equals(",") || tree.getChildName(2).equals(":")){
							return true;
						}
					}
				}
			}
		}
		if(tree.getLastChildName().equals("''") || tree.getLastChildName().equals("``")){
			if(tree.getChildrenNum() == 6){
				if(tree.getChildName(4).equals(".")){
					if(tree.getFirstChildName().equals(tree.getChildName(3)) &&
							tree.getChildName(1).equals(",") && tree.getChildName(2).equals("CC")){
						return true;
					}
					if(tree.getFirstChildName().equals(tree.getChildName(3)) &&
							tree.getChildName(1).equals(":") && tree.getChildName(2).equals("CC")){
						return true;
					}
				}
			}else if(tree.getChildrenNum() == 5){
				if(tree.getChildName(3).equals(".")){
					if(tree.getFirstChildName().equals(tree.getChildName(2))){
						if(tree.getChildName(1).equals("CC") || tree.getChildName(1).equals(",") || tree.getChildName(1).equals(":")){
							return true;
						}
					}
				}
			}
		}
				
		if(tree.getLastChildName().equals(".")){	
			if(tree.getChildrenNum() == 6){
				if(tree.getFirstChildName().equals("''") || tree.getFirstChildName().equals("``")){
					if(tree.getChildName(1).equals(tree.getChildName(4)) &&
							tree.getChildName(2).equals(",") && tree.getChildName(3).equals("CC")){
						return true;
					}
					if(tree.getChildName(1).equals(tree.getChildName(4)) &&
							tree.getChildName(2).equals(":") && tree.getChildName(3).equals("CC")){
						return true;
					}
				}
			}else if(tree.getChildrenNum() == 5){
				if(tree.getFirstChildName().equals(tree.getChildName(3)) &&
						tree.getChildName(1).equals(",") && tree.getChildName(2).equals("CC")){
					return true;
				}
				if(tree.getFirstChildName().equals(tree.getChildName(3)) &&
						tree.getChildName(1).equals(":") && tree.getChildName(2).equals("CC")){
					return true;
				}
			}else if(tree.getChildrenNum() == 4){
				if(tree.getFirstChildName().equals(tree.getChildName(2))){
					if(tree.getChildName(1).equals("CC") || tree.getChildName(1).equals(",") || tree.getChildName(1).equals(":")){
						return true;
					}
				}
			}
		}else{
			if(tree.getChildrenNum() == 4){
				if(tree.getFirstChildName().equals(tree.getChildName(3)) &&
						tree.getChildName(1).equals(",") && tree.getChildName(2).equals("CC")){
					return true;
				}
				if(tree.getFirstChildName().equals(tree.getChildName(3)) &&
						tree.getChildName(1).equals(":") && tree.getChildName(2).equals("CC")){
					return true;
				}
			}else if(tree.getChildrenNum() == 3){
				if(tree.getFirstChildName().equals(tree.getChildName(2))){
					if(tree.getChildName(1).equals("CC") || tree.getChildName(1).equals(",") || tree.getChildName(1).equals(":")){
						return true;
					}
				}
			}
		}
		return false;
	}
}
