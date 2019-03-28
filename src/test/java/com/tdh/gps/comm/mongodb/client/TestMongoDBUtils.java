package com.tdh.gps.comm.mongodb.client;

import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.tdh.gps.comm.mongodb.client.utils.MongoDBConnectionPoolUtils;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:mongoDB-client-application.xml" })
public class TestMongoDBUtils {

	@Autowired
	private MongoDBConnectionPoolUtils utils;
	private static final String DATABASENAME = "test";
	private static final String COLLECTIONNAME = "gps";

	@Test
	public void test() {
		MongoCollection<Document> collection = utils.getCollection(DATABASENAME, COLLECTIONNAME);

		BasicDBObject object = new BasicDBObject();
		object = object.parse("{'plate_no':'晋A3L4341839','position_time':{$gte:ISODate(\"2018-09-03T08:16:35.340Z\")}}");


//		Document document = new Document();
//		document.put("plate_no", "晋A3L4341839" );
//		document.put("position_time", new Date());
//		collection.insertOne(document);

		FindIterable<Document> document = collection.find(object);
		MongoCursor<Document> mongoCursor = document.iterator();

		while (mongoCursor.hasNext()) {
			Document doc = mongoCursor.next();
			System.out.println(doc.toJson());
		}

	}

}
