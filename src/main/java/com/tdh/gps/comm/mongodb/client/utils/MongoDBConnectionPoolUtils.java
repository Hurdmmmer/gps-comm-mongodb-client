package com.tdh.gps.comm.mongodb.client.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * 
 * @ClassName: MongoDBConnectionPoolUtils
 * @Description: (MongoDB连接池工具类)
 * @author wxf
 * @date 2018年9月3日 下午5:09:02
 *
 */
@Component("mongoDBConnectionPoolUtils")
@PropertySource({ "classpath:dataSource.properties", "classpath:mongodb-client-config.properties" })
public class MongoDBConnectionPoolUtils {

	private static final Log LOG = LogFactory.getLog(MongoDBConnectionPoolUtils.class);
	/**
	 * mongodb客户端
	 */
	private MongoClient mongoClient = null;
	/**
	 * mongodb数据库实例Map集合缓存
	 */
	private final Map<String, MongoDatabase> mongoDatabaseMap = new ConcurrentHashMap<String, MongoDatabase>();
	
	/**
	 * 读写锁
	 */
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	@Value("${DB.IP}")
	private String IP;
	@Value("${DB.PORT}")
	private int PORT;
//	用户名
	@Value("${DB.USERNAME}")
	private String USERNAME;
//	密码
	@Value("${DB.PASSWORD}")
	private String PASSWORD;
//	数据库
	@Value("${DB.DATABASE}")
	private String DATABASE;
//	最大连接数
	@Value("${DB.MAXCONNS}")
	private int MAXCONNS;
//	连接超时时间
	@Value("${DB.CONNECTTIMEOUT}")
	private int CONNECTTIMEOUT;
//	线程获取连接最大等待时间
	@Value("${DB.MAXWAITTIME}")
	private int MAXWAITTIME;
//	空闲连接最大存活时间
	@Value("${DB.MAXCONNECTIONIDLETIME}")
	private int MAXCONNECTIONIDLETIME;
//	等待线程队列的倍数
	@Value("${DB.THREADSALLOWEDTOBLOCKFORCONNECTIONMULTIPLIER}")
	private int THREADSALLOWEDTOBLOCKFORCONNECTIONMULTIPLIER;
//  服务器socket连接超时
	@Value("${DB.SOCKETTIMEOUT}")
	private int SOCKETTIMEOUT;
//	初始最小连接数
	@Value("${DB.MINCONNS}")
	private int MINCONNS;
	/**
	 * 锁超时时间
	 */
	@Value("${timeOutTryLock}")
	private long timeoutTryLock;

	/**
	 * 
	 * @Title: getMongoClient  
	 * @Description: (获得mongodb数据库客户端)  
	 * @return MongoClient 返回类型 
	 * @throws
	 */
	public MongoClient getMongoClient() {
		createMongoClient();
		return mongoClient;
	}

	/**
	 * 
	 * @Title: getMongoDatabase  
	 * @Description: (获得mongodb数据库实例)  
	 * @param databaseName
	 * @return MongoDatabase 返回类型 
	 * @throws
	 */
	public MongoDatabase getMongoDatabase(String databaseName) {
		createMongoClient();
		MongoDatabase database = mongoDatabaseMap.get(databaseName);
		if (null == database) {
			database = mongoClient.getDatabase(databaseName);
			mongoDatabaseMap.put(databaseName, database);
		}
		return database;
	}

	/**
	 * 
	 * @Title: getCollection  
	 * @Description: (获得mongodb数据库集合实例)  
	 * @param databaseName
	 * @param collectionName
	 * @return MongoCollection<Document> 返回类型 
	 * @throws
	 */
	public MongoCollection<Document> getCollection(String databaseName, String collectionName) {
		createMongoClient();
		return getMongoDatabase(databaseName).getCollection(collectionName);
	}

	/**
	 * 
	 * @Title: createMongoClient  
	 * @Description: (创建mongodb数据库客户端实例) 
	 * @throws
	 */
	private void createMongoClient() {
		if (null != mongoClient)
			return;
		try {
			readWriteLock.writeLock().tryLock(timeoutTryLock, TimeUnit.MILLISECONDS);
			MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
			builder.connectionsPerHost(MAXCONNS);
			builder.connectTimeout(CONNECTTIMEOUT);
			builder.maxWaitTime(MAXWAITTIME);
			builder.maxConnectionIdleTime(MAXCONNECTIONIDLETIME);
			builder.threadsAllowedToBlockForConnectionMultiplier(THREADSALLOWEDTOBLOCKFORCONNECTIONMULTIPLIER);
			builder.socketTimeout(SOCKETTIMEOUT);
			builder.minConnectionsPerHost(MINCONNS);
			ServerAddress address = new ServerAddress(IP, PORT);
			MongoCredential credential = MongoCredential.createCredential(USERNAME, DATABASE, PASSWORD.toCharArray());
			mongoClient = new MongoClient(address, credential, builder.build());
		} catch (InterruptedException e) {
			LOG.error("创建mongodb数据库客户端实例异常", e);
		} finally {
			readWriteLock.writeLock().unlock();
		}

	}

}
