package com.lc.nlp4han.constituent.pcfg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文法转换器
 *
 */
public class GrammarConvertor
{
	/**
	 * 包含单元规则的CNF文法
	 * 
	 * 允许A->B类型的规则
	 * 
	 * @param pcfg
	 * @return
	 */
	public static PCFG PCFG2LoosePCNF(PCFG pcfg)
	{
		PCFG pcnf = new PCFG();
		
		addPosProb(pcfg, pcnf);
		toLooseCNF(pcfg, pcnf);

		return pcnf;
	}

	/**
	 * 这里的转换为PCNF在消除单元规则时,消除到POS层次即停止
	 * 
	 * @param pcfg
	 * @return
	 * @throws IOException
	 */
	public static PCFG PCFG2PCNF(PCFG pcfg) throws IOException
	{
		PCFG pcnf = new PCFG();

		addPosProb(pcfg, pcnf);

		toLooseCNF(pcfg, pcnf);

		removeUnitProduction(pcnf.getPosSet(), pcnf);

		return pcnf;
	}

	/**
	 * 由宽松PCNF转换为PCNF
	 * 
	 * @param cnf
	 * @return
	 */
	public static PCFG loosePCNF2PCNF(CFG cnf)
	{
		HashSet<String> posSet = cnf.getPosSet();
		removeUnitProduction(posSet, cnf);
		return (PCFG) cnf;
	}

	private static void addPosProb(PCFG pcfg, PCFG pcnf)
	{
		HashMap<String, Double> posProb = new HashMap<String, Double>();

		for (String pos : pcfg.getPosSet())
		{
			posProb.put(pos, pcfg.getPosPro(pos));
		}

		pcnf.setPosProb(posProb);
	}

	/**
	 * 将规则转换为宽松PCNF形式（即不消除单元规则的乔姆斯基范式）
	 * 
	 * @param cfg
	 */
	private static void toLooseCNF(CFG cfg, CFG cnf)
	{
		cnf.setNonTerminalSet(cfg.getNonTerminalSet());
		cnf.setTerminalSet(cfg.getTerminalSet());
		cnf.setStartSymbol(cfg.getStartSymbol());

		reduceAndNormRight(cfg, cnf);
	}

	/**
	 * 将字符串个数多于两个的递归的减为两个, 将终结符和非终结符混合转换为两个非终结符, 直接添加右侧只有一个字符串的规则
	 */
	private static void reduceAndNormRight(CFG cfg, CFG cnf)
	{
		for (RewriteRule rule : cfg.getRuleSet())
		{
			if (rule.getRHS().size() >= 3)
			{
				// 如果右侧中有终结符，则转换为伪非终结符
				if (!cnf.getNonTerminalSet().containsAll(rule.getRHS()))
				{
					convertToNonTerRHS(rule, cnf);
				}

				reduceRHSNum(rule, cnf);
			}

			// 先检测右侧有两个字符串的规则是否为终结符和非终结符混合，若混合则先将终结符转换为非终结符
			if (rule.getRHS().size() == 2)
			{
				// 如果右侧中有终结符，则转换为伪非终结符
				if (!cnf.getNonTerminalSet().containsAll(rule.getRHS()))
				{
					convertToNonTerRHS(rule, cnf);
				}

				cnf.add(rule);
			}

			// 先添加进cnf随后处理
			if (rule.getRHS().size() == 1)
			{
				cnf.add(rule);
			}
		}
	}

	/**
	 * 将右侧全部转换为非终结符，并添加新的非终结符，新的规则
	 * 
	 * 	A->B a变成
	 * $a$->a  
	 * A->B $a$
	 */
	private static void convertToNonTerRHS(RewriteRule rule, CFG cnf)
	{
		ArrayList<String> rhs = new ArrayList<String>();
		for (String string : rule.getRHS())
		{
			if (cnf.isTerminal(string))
			{
				String newString = "$" + string + "$";
				cnf.addNonTerminal(newString);// 添加新的伪非终结符

				// 添加新的规则
				cnf.add(new PRule(1.0, newString, string));

				rhs.add(newString);
			}
			else
			{
				rhs.add(string);
			}
		}

		rule.setRHS(rhs);
	}

	/**
	 * 每次选择最右侧字符串的两个为新的规则的右侧字符串，以&联接两个非终结符
	 * 
	 * A->B C D变成
	 * C&D->C D
	 * A->B C&D
	 */
	private static void reduceRHSNum(RewriteRule rule, CFG cnf)
	{
		if (rule.getRHS().size() == 2)
		{
			cnf.add(rule);
			return;
		}

		List<String> list = rule.getRHS();
		int size = list.size();
		String str = list.get(size - 2) + "&" + list.get(size - 1);// 新规则的左侧

		// 最右侧的两个非终结符合成一个，并形成新的规则
		cnf.add(new PRule(1.0, str, list.get(size - 2), list.get(size - 1)));

		cnf.addNonTerminal(str);// 添加新的合成非终结符

		ArrayList<String> rhsList = new ArrayList<String>();
		rhsList.addAll(rule.getRHS().subList(0, rule.getRHS().size() - 2));
		rhsList.add(str);
		rule.setRHS(rhsList);

		// 递归，直到rhs的个数为2时
		reduceRHSNum(rule, cnf);
	}

	/**
	 * 消除单元规则
	 */
	private static void removeUnitProduction(HashSet<String> posSet, CFG cnf)
	{
		HashSet<RewriteRule> deletePRuleSet = new HashSet<RewriteRule>();
		Set<String> nonterSet = cnf.getNonTerminalSet();

		for (String nonTer : cnf.getNonTerminalSet())
		{
			for (RewriteRule rule : cnf.getRuleByLHS(nonTer))
			{
				if (rule.getRHS().size() == 1) // 单元规则
				{
					String rhs = rule.getRHS().get(0);
					if (posSet.contains(rhs)) // 右部是词性
					{// 消除单元规则终止于POS层次
						continue;
					}

					if (nonterSet.contains(rhs))
					{
						deletePRuleSet.add(rule);
						removeUPAndAddNewRule(rule, posSet, cnf);
					}
				}
			}
		}

		deletePRuleSet(deletePRuleSet, cnf);
	}

	private static void removeUPAndAddNewRule(RewriteRule rule, HashSet<String> posSet, CFG cnf)
	{
		String lhs = rule.getLHS();
		String rhs = rule.getRHS().get(0);

		String[] lhs1 = lhs.split("@");
		if (lhs1.length >= 3)
		{
			return;// 如果单元规则迭代有3次以上，则返回
		}

		if (posSet.contains(rule.getRHS().get(0)))
		{
			cnf.add(rule);// 若该规则右侧为词性标注则直接添加
			return;
		}

		for (String lhs2 : lhs1)
		{
			if (lhs2.equals(rhs))
			{
				return;// 如果出现循环非终结符则返回
			}
		}

		for (RewriteRule rule1 : cnf.getRuleByLHS(rule.getRHS().get(0)))
		{
			RewriteRule rule2;

			PRule prule1 = (PRule) rule1;
			PRule prule = (PRule) rule;

			rule2 = new PRule(prule.getProb() * prule1.getProb(), prule.getLHS() + "@" + prule1.getLHS(),
					prule1.getRHS());

			if (rule1.getRHS().size() == 2 || !cnf.getNonTerminalSet().contains(rule1.getRHS().get(0)))
			{
				cnf.add(rule2);
			}
			else
			{
				removeUPAndAddNewRule(rule2, posSet, cnf);
			}
		}
	}

	private static void deletePRuleSet(HashSet<RewriteRule> deletePRuleSet, CFG cnf)
	{
		for (RewriteRule rule : deletePRuleSet)
		{
			cnf.getRuleSet().remove(rule);
			cnf.getRuleByLHS(rule.getLHS()).remove(rule);
			cnf.getRuleByRHS(rule.getRHS()).remove(rule);
		}
	}
}