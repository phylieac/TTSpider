package edu.pantek.databases.serialization;


import com.alibaba.fastjson.JSONObject;

import edu.pantek.databases.mysql.GenerateSQL;
import edu.pantek.databases.mysql.MySQL;

public class MySQLSerialization extends DBSerialization{

	MySQL mysql=null;
	GenerateSQL gs=new GenerateSQL();
	
	
	public MySQLSerialization(String ip,String port,String dbname) {
		mysql=new MySQL(ip,port);
		mysql.setDB(dbname);
	}
	
	public void setUserPasswd(String user,String passwd) {
		mysql.setUserPasswd(user, passwd);
	}
	
	public boolean getConnection() {
		return mysql.getConnection();
	}
	
	public void close() {
		mysql.closeDB();
	}
	
	public void serialize(String tbname,JSONObject json) {
		String sql=gs.generateInsert(tbname, json);
		System.out.println(sql);
		this.mysql.update(sql);
	}

	@Override
	public void serialize() {
		// TODO Auto-generated method stub
	}

}
