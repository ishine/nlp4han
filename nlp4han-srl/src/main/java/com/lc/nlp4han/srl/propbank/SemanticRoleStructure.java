package com.lc.nlp4han.srl.propbank;

/**
 * 语义信息工具类
 * @author 王馨苇
 *
 */
public class SemanticRoleStructure {

	int begin;
	int up;
	String role;
	
	public SemanticRoleStructure(int begin,int up,String role){
		this.begin = begin;
		this.up = up;
		this.role = role;
	}
	
	public int getBegin(){
		return this.begin;
	}
	
	public int getUp(){
		return this.up;
	}
	
	public String getRole(){
		return this.role;
	}

	@Override
	public String toString() {
		return this.up+"_"+this.role;
	}
	
	
}
