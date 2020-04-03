package com.tydic.dcm.util.condition;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.tydic.bp.common.utils.tools.BlankUtil;
import com.tydic.bp.common.utils.tools.DateUtil;
import com.tydic.dcm.util.tools.StringTool;

/**
 * 条件过滤
 * @author Yuanh
 *
 */
public class Condition {

	class LogicItem {
		protected static final String KEY_TIME = "$time";
		protected static final String DEFUALT_TIME_FMT = "yyyy-MM-dd HH:mm:ss";

		protected static final String EQ = "==";
		protected static final String BT = ">";
		protected static final String LT = "<";
		protected static final String NEQ = "!=";
		protected static final String BTE = ">=";
		protected static final String LTE = "<=";

		protected static final String IEQ = "%==";
		protected static final String IBT = "%>";
		protected static final String ILT = "%<";
		protected static final String INEQ = "%!=";
		protected static final String IBTE = "%>=";
		protected static final String ILTE = "%<=";

		protected static final String BLIKE = "blike";
		protected static final String NBLIKE = "!blike";
		protected static final String ELIKE = "elike";
		protected static final String NELIKE = "!elike";
		protected static final String LIKE = "like";
		protected static final String NLIKE = "!like";
		protected String name;
		protected String desc;
		protected String value;
		protected String op;

		protected LogicItem(String item) throws Exception {
			
			//获取运算表达式
			op = getOP(item);
			if (BlankUtil.isBlank(op)) {
				throw new RuntimeException("find op fail, item:" + item);
			}
			
			//获取表达式对应的值
			int pos = item.indexOf(op);
			value = item.substring(pos + op.length()).trim();
			
			//获取表达式名称和描述
			String tempName = item.substring(0, pos).trim();
			pos = tempName.indexOf("@");
			if (pos >= 0) {
				name = tempName.substring(0, pos).trim();
				desc = tempName.substring(pos + 1).trim();
			} else {
				name = tempName;
			}
		}
		
		
		/**
		 * 获取校验对象表达式
		 * @param str
		 * @return
		 */
		protected String getOP(String str) {
			if (str.indexOf(INEQ) >= 0)
				return INEQ;
			else if (str.indexOf(IBTE) >= 0)
				return IBTE;
			else if (str.indexOf(ILTE) >= 0)
				return ILTE;
			else if (str.indexOf(IEQ) >= 0)
				return IEQ;
			else if (str.indexOf(IBT) >= 0)
				return IBT;
			else if (str.indexOf(ILT) >= 0)
				return ILT;

			else if (str.indexOf(NEQ) >= 0)
				return NEQ;
			else if (str.indexOf(BTE) >= 0)
				return BTE;
			else if (str.indexOf(LTE) >= 0)
				return LTE;
			else if (str.indexOf(EQ) >= 0)
				return EQ;
			else if (str.indexOf(BT) >= 0)
				return BT;
			else if (str.indexOf(LT) >= 0)
				return LT;

			else if (str.indexOf(NBLIKE) >= 0)
				return NBLIKE;
			else if (str.indexOf(BLIKE) >= 0)
				return BLIKE;
			else if (str.indexOf(NELIKE) >= 0)
				return NELIKE;
			else if (str.indexOf(ELIKE) >= 0)
				return ELIKE;
			else if ((str.indexOf(LIKE) >= 0) && (str.indexOf(NLIKE) < 0))
				return LIKE;
			else if (str.indexOf(NLIKE) >= 0)
				return NLIKE;
			else
				return null;
		}
	    
		/**
		 * 校验当前数据是否符合校验规则
		 * @param params
		 * @return
		 */
		protected boolean check(Hashtable<String, String> params){
			String v = null;
			if (KEY_TIME.equals(name)) {
				if (BlankUtil.isBlank(desc)) {
					desc = DateUtil.allPattern;
				}
				v = DateUtil.format(new Date(), desc);
			} else {
				v = (String) params.get(name);
			}
			
			if (BlankUtil.isBlank(v)) {
				throw new java.lang.NullPointerException("LogicItem::check value=null  item:" + toString());
			} else {
				if (op.equals(EQ))
					return v.equals(value);
				else if (op.equals(NEQ))
					return !v.equals(value);
				else if (op.equals(BT))
					return v.compareTo(value) > 0;
				else if (op.equals(BTE))
					return v.compareTo(value) >= 0;
				else if (op.equals(LT))
					return v.compareTo(value) < 0;
				else if (op.equals(LTE))
					return v.compareTo(value) <= 0;
				
				else if (op.equals(IEQ))
					return Long.parseLong(v) == Long.parseLong(value);
				else if (op.equals(INEQ))
					return Long.parseLong(v) != Long.parseLong(value);
				else if (op.equals(IBT))
					return Long.parseLong(v) > Long.parseLong(value);
				else if (op.equals(IBTE))
					return Long.parseLong(v) >= Long.parseLong(value);
				else if (op.equals(ILT))
					return Long.parseLong(v) < Long.parseLong(value);
				else if (op.equals(ILTE))
					return Long.parseLong(v) <= Long.parseLong(value);

				else if (op.equals(BLIKE))
					return v.startsWith(value);
				else if (op.equals(NBLIKE))
					return !v.startsWith(value);
				else if (op.equals(ELIKE))
					return v.endsWith(value);
				else if (op.equals(NELIKE))
					return !v.endsWith(value);
				else if (op.equals(LIKE)) {
					if (value.equals("*"))
						return true;
					return v.indexOf(value) >= 0;
				} else if (op.equals(NLIKE)) {
					return v.indexOf(value) < 0;
				} else
					return false;
			}
		}

		/**
		 * 条件详细信息
		 */
		public String toString() {
			if (desc == null)
				return name + "    " + op + " " + value;
			else
				return name + "@" + desc + "    " + op + " " + value;
		}
	}
	
	
	protected Vector<Vector<LogicItem>> cdtList;
	//根据||划分为一层集合，&&划分为二层集合
	public Condition(String condition) throws Exception {
		cdtList = new Vector<Vector<LogicItem>>();
		//transfer_condition参数格式:ori_file_name like * && tonowdays %<=5
		//根据||来设置集合内容
		Vector<String> strOrList = StringTool.tokenString(condition, "||");
		for (int i=0; i<strOrList.size(); i++) {
			String andStr = strOrList.get(i);
			Vector<LogicItem> andList = new Vector<LogicItem>();			
			Vector<String> strAndList = StringTool.tokenString(andStr, "&&");
			for (int j=0; j<strAndList.size(); j++) {
				LogicItem logicItem = new LogicItem(strAndList.get(j));
				andList.add(logicItem);
			}
			cdtList.add(andList);
		}
	}
	
	/**
	 * 文件过滤检查
	 * @param params
	 * @return
	 */
	public boolean check(Hashtable<String, String> params) {
		
		//判断当前参数是否存在
		for (int i=0; i<cdtList.size(); i++) {
			Vector<LogicItem> andList = cdtList.get(i);
			for (int j=0; j<andList.size(); j++) {
				LogicItem logicItem = andList.get(j);
				if (logicItem.name.equalsIgnoreCase(LogicItem.KEY_TIME)) {
					continue;
				} else if (BlankUtil.isBlank(params.get(logicItem.name))) {
					throw new java.lang.NullPointerException("Condition::check value=null " + logicItem.toString());
				}
			}
		}
		
		for (int i=0; i<cdtList.size(); i++) {
			Vector<LogicItem> andList = cdtList.get(i);
			int j=0;
			for (; j<andList.size(); j++) {
				LogicItem item = andList.get(j);
				if (!item.check(params)) {
					break;
				}
			}
			if (j >= andList.size()) {
				return true;
			}
		}
		return false;
	}
	
}
