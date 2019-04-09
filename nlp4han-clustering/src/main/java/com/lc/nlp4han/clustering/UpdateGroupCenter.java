package com.lc.nlp4han.clustering;

import java.util.List;

/**
 * 更新簇的中心
 * @author 杨智超
 *
 */
public interface UpdateGroupCenter
{
	/**
	 * 更新所有簇的中心
	 * @param grps 所有待更新的簇
	 * @return 若有簇的中心发生改变，返回true；若无一簇中心改变，返回false
	 */
	public boolean updateCenter(List<Group> grps);
}
