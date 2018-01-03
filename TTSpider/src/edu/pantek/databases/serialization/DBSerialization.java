package edu.pantek.databases.serialization;


import com.alibaba.fastjson.JSONObject;

import edu.pantek.serialization.Serialization;

public abstract class DBSerialization extends Serialization{

	public abstract void serialize(String tb,JSONObject json);
	
	public abstract void close();
}
