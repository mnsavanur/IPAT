package com.lti.TestUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.lti.base.Config;
import com.lti.webDriver.Assertions;
/*
 * Date - 12/13/2021
 * Author - Sheetal Jadhav
 * Description - Database connection and query
*/
public class DatabaseHandling {
	static Logger log = Logger.getLogger(DatabaseHandling.class.getName());
	public static Connection connectToDB(String dbType, String host, String port, String user, String pwd)throws Exception{
		Connection conn = null;
		try {
			if(dbType.equalsIgnoreCase("SQLServer")) {
				//String dbURL="jdbc:sqlserver://"+host +"\\sqlexpress;portNumber="+port+";user="+ user +";password=" + pwd;
				//conn = DriverManager.getConnection(dbURL);
				
				String dbURL="jdbc:sqlserver://"+host +"\\sqlexpress";
				Properties properties=new Properties();
				properties.put("portNumber", port);
				properties.put("user", user);
				properties.put("password", pwd);
				
				conn = DriverManager.getConnection(dbURL, properties);
			}
			return conn;
		}catch(Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			throw new Exception(e);
		}
	}
	public static ResultSet executeQuery(Connection con, String query)throws Exception{
		Statement st = null;
		ResultSet rs;
		st = con.createStatement();
		rs = st.executeQuery(query);
		return rs;
	}
	public static void main(String args[])throws Exception{
		Connection conn=null;
		try {
			conn= connectToDB("SQLServer","172.17.22.11","29433","SheetalJ","Automation@123");
			String q="select * from Sys.servers";
			ResultSet rs=executeQuery(conn,q);
			//rs.next();
			while (rs.next()) {
				System.out.println(rs.getString("name"));
			}
		}catch(Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}finally {
			try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (Exception e) {
            	e.printStackTrace();
    			log.error(e.getMessage());
            }
		}
	}
	public static HashMap<String,String> retrieveDBParameters(String dbName)throws Exception{
		
		HashMap<String,String> dbParams=new HashMap<String, String>();
		for(int i=0;i<Config.listOfDatabse.size();i++) {
			if(Config.listOfDatabse.get(i).get(0).trim().replace("dbName=","").equalsIgnoreCase(dbName)){
				for(int j=1;j<Config.listOfDatabse.get(i).size();j++) {
					if(Config.listOfDatabse.get(i).get(j).contains("dbType"))
						dbParams.put("dbType", Config.listOfDatabse.get(i).get(j).trim().replace("dbType=",""));
					
					if(Config.listOfDatabse.get(i).get(j).contains("dbHost"))
						dbParams.put("dbHost", Config.listOfDatabse.get(i).get(j).trim().replace("dbHost=",""));
					
					if(Config.listOfDatabse.get(i).get(j).contains("dbPort"))
						dbParams.put("dbPort", Config.listOfDatabse.get(i).get(j).trim().replace("dbPort=",""));
					
					if(Config.listOfDatabse.get(i).get(j).contains("dbUser"))
						dbParams.put("dbUser", Config.listOfDatabse.get(i).get(j).trim().replace("dbUser=",""));
					
					if(Config.listOfDatabse.get(i).get(j).contains("dbPassword"))
						dbParams.put("dbPassword", Config.listOfDatabse.get(i).get(j).trim().replace("dbPassword=",""));
				}
			}else
				continue;
		}
		return dbParams;
	}
	
	public static String singleResultQuery(String dbName, String query)throws Exception{
		Connection conn=null;
		try {
			HashMap<String,String> dbParams=retrieveDBParameters(dbName);
			conn = connectToDB(dbParams.get("dbType"),dbParams.get("dbHost"),dbParams.get("dbPort"),dbParams.get("dbUser"),dbParams.get("dbPassword"));
			ResultSet rsActual = executeQuery(conn, query);
			rsActual.next();
			String actualQResult=String.valueOf(rsActual.getString(1));
			if(rsActual.next() != false) {
				throw new Exception("MoreThanOneResult");
			}
			return actualQResult;
		}catch(Exception e) {
			throw e;
		}finally {
			try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (Exception e) {
            	e.printStackTrace();
    			log.error(e.getMessage());
            }
		}
	}
}
