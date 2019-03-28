package com.tdh.gps.comm.mongodb.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tdh.gps.comm.mongodb.client.utils.PrimaryKeyGeneratorUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:mongoDB-client-application.xml" })
public class TestPrimaryKeyGeneratorUtils {

	@Autowired
	private PrimaryKeyGeneratorUtils utils;

	@Test
	public void testGeneratePrimaryKey() {
		for (int i = 0; i < 1000; i++) {
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					utils.generatePrimaryKey("gps_truck");
				}
			});
			thread.setName("thread" + i);
			thread.start();
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
