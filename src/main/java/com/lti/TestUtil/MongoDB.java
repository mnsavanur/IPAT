package com.lti.TestUtil;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.lti.base.Base;
import com.lti.base.Config;
import com.lti.dataProviders.DataProviders;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDB extends Base {
	static Logger log = Logger.getLogger(MongoDB.class.getName());
	public static Logger mlog = Logger.getLogger("org.mongodb.driver");
	public static MongoClient mongoClient=null;
	public static MongoCollection webCollection;
	
	
	public static void createConnection() {
		
		if(Config.mongoConnectionString != null && Config.mongoDBName !=null && Config.mongoResultTable !=null) {
			if(mongoClient==null) {
				try {
					mongoClient = MongoClients.create(Config.mongoConnectionString);
					MongoDatabase db = mongoClient.getDatabase(Config.mongoDBName);
					webCollection = db.getCollection(Config.mongoResultTable);
					log.info("Connected to MongoDB server successfully");
				}catch(Exception e) {
					log.error(e.getMessage());
				}
				
		
			}
		}else {
			log.info("Mongo DB instance details not found in Config file. Results will not be stored in database");
		}
		

	}
	
	public static void closeConnection() {
		if(Config.mongoConnectionString != null && Config.mongoDBName !=null && Config.mongoResultTable !=null) {
			mongoClient.close();
		}
		
	}
	
	public static void addDocument(String TCName, String tcStatus, List<DBObject> testSteps, String appName, String Env, long duration) {
		if(Config.mongoConnectionString != null && Config.mongoDBName !=null && Config.mongoResultTable !=null) {
			Date d =new Date();
			mongoDoc.append("TestCase", TCName)
			.append("TestCaseStatus", tcStatus)
			.append("TestSteps", testSteps)
			.append("Application", appName)
			.append("Environment", Env)
			.append("Date",d)
			.append("Duration", duration)
			.append("UserID", (System.getProperty("user.name")).toString());
			webCollection.insertOne(mongoDoc);
			ObjectId id = mongoDoc.getObjectId("_id");
			log.info("Added record to MongoDB. Reference ID: "+id);
		}
		
		
	}
	
/*	public static void addTcName(String TCName) {
		resultDoc.append("TestCaseName", TCName);
	}
	
	public static void addTimeStamp() {
		Date date = new Date();
		resultDoc.append("Execution Date", date);
	}
	
	public static void addTcStatus(String tcStatus) {
		resultDoc.append("Status", tcStatus);
	}
	
	public static void addDocument(Document doc) {
		docList.add(doc);
		MongoDB.webCollection.insertMany(docList);
	}
	
	public static void addEnvDetails() {
		resultDoc.append("User ID", (System.getProperty("user.name")).toString());
	}*/
	
	public static void addTestStepLog(String message, String status) {
		//resultLog.append("Test Step Status", status)
		//.append("Log Message", message);
		//dbLog.add(resultLog);
		if(Config.mongoConnectionString != null && Config.mongoDBName !=null && Config.mongoResultTable !=null) {
			BasicDBObject logObject =new BasicDBObject();
			logObject.put("TestStepLog",message);
			logObject.put("Status",status);
			dataLog.add(logObject);
		}
		
		//dataLog.add(new BasicDBObject("Test Step Log",message));
		
		//dataLog.add(new BasicDBObject("Status",status));
		
	}
	
	/*public static void appendTestStepLog(List dbLog, Document mainLog) {
		mainLog.append("Test Step Results", dbLog);
	}*/

}

