package edu.pantek.databases.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class MySQL {
	public Connection conn = null;
	public ResultSet rs = null;

	private String DatabaseDriver = "com.mysql.jdbc.Driver";

	private String DatabaseUrl = "localhost:3306";

	private String DatabaseName = "taobao";

	private String userName = "root";

	private String passWord = "123456";
	// DataSource 数据源名称DSN
	private String DatabaseConnStr = "jdbc:mysql://" + DatabaseUrl + "/" + DatabaseName;
		
	public MySQL(String ip,String port) {
		this.DatabaseUrl=ip+":"+port;
	}
	
	public void setUserPasswd(String user,String passwd) {
		this.userName=user;
		this.passWord=passwd;
	}
	
	public void setDB(String dbName) {
		this.DatabaseName=dbName;
	}
	
	public boolean getConnection() {
		this.DatabaseConnStr="jdbc:mysql://" + this.DatabaseUrl + "/" + this.DatabaseName+"?useUnicode=true&characterEncoding=UTF-8";
		boolean s=false;
		try {
			Class.forName(DatabaseDriver);
			conn = DriverManager.getConnection(DatabaseConnStr, userName, passWord);
			s=this.conn.isClosed();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		return !s;
	}
	
	public ResultSet excute(String sql) {
		Statement state=null;
		ResultSet rs=null;
		try {
			state=this.conn.createStatement();
			rs=state.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	public int update(String sql) {
		int state=0;
		try {
			state=this.conn.createStatement().executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return state;
	}
	
	public void updateLarge(String sql) {
		try {
			this.conn.createStatement().executeLargeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// 关闭数据库
	public void closeDB() {
		try {
			conn.close();
		} catch (Exception end) {
			System.err.println("执行关闭Connection对象有错误：" + end.getMessage());
			System.out.print("执行执行关闭Connection对象有错误：有错误:" + end.getMessage()); // 输出到客户端
		}
	}
	
	public static void main(String[] args) {
		MySQL ms=new MySQL("localhost","3306");
		ms.setUserPasswd("root", "123456");
		ms.setDB("taobao");
		System.out.println(ms.getConnection());
		ResultSet rs=ms.excute("show tables");
		try {
			while(rs.next())
				System.out.println(rs.getString(1));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
