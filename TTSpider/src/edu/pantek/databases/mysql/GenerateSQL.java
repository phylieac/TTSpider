package edu.pantek.databases.mysql;

import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;

public class GenerateSQL {

	public String generateInsert(String tbname, JSONObject js) {
		String tbFields = tbname+"(";
		String values = "VALUES(";
		for (Entry<String, Object> entry : js.entrySet()) {
			tbFields+=entry.getKey()+",";
			values+="'"+entry.getValue()+"',";
		}
		tbFields=tbFields.substring(0, tbFields.length()-1)+")";
		values=values.substring(0, values.length()-1)+")";
		
		return "INSERT into "+tbFields+" "+values;
	}
}
