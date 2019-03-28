package com.tdh.gps.comm.mongodb.client;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.tdh.gps.comm.mongodb.client.base.CollectionPrimaryKeySupport;
import com.tdh.gps.comm.mongodb.client.model.CollectionPrimaryKey;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:mongoDB-client-application.xml" })
public class TestCollectionPrimaryKeySupport {
	private static final Log LOG = LogFactory.getLog(TestCollectionPrimaryKeySupport.class);
	@Autowired
	private CollectionPrimaryKeySupport support;

	@Test
	public void testUpdateCollectionPrimaryKey() {
		List<CollectionPrimaryKey> primaryKeys = support.find(Filters.eq("collectionName", "gps_truck"),
				CollectionPrimaryKey.class);
		CollectionPrimaryKey primaryKey = primaryKeys.get(0);
		primaryKey.setCreateDate(new Date());
		primaryKey.setUpdateDate(new Date());
		long currentValue = primaryKey.getCurrentValue() + 1;
		primaryKey.setCurrentValue(currentValue);
		Gson gson = new Gson();
		LOG.info(Thread.currentThread().getName());
		LOG.info(gson.toJson(primaryKey));
		LOG.info(support.updateBy_id(primaryKey));
//		LOG.info(Thread.currentThread().getName() + "执行结束");

	}

}
