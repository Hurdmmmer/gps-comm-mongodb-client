package com.tdh.gps.comm.mongodb.client.utils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.mongodb.client.model.Filters;
import com.tdh.gps.comm.mongodb.client.base.CollectionPrimaryKeySupport;
import com.tdh.gps.comm.mongodb.client.model.CollectionPrimaryKey;

/**
 * 
 * @ClassName: PrimaryKeyGenerator
 * @Description: (主键生成器工具类)
 * @author wxf
 * @date 2018年9月4日 上午10:41:06
 *
 */
@Component("primaryKeyGeneratorUtils")
@PropertySource("classpath:mongodb-client-config.properties")
public class PrimaryKeyGeneratorUtils {
	private static final Log LOG = LogFactory.getLog(PrimaryKeyGeneratorUtils.class);
	/**
	 * 锁超时时间
	 */
	@Value("${timeOutTryLock}")
	private long timeoutTryLock;
	@Autowired
	private CollectionPrimaryKeySupport support; 

	/**
	 * 读写锁
	 */
	private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	
	/**
	 * 
	 * @Title: generatePrimaryKey  
	 * @Description: (生成主键)  
	 * @param collectionName
	 * @return long 返回类型 
	 * @throws
	 */
	public long generatePrimaryKey(String collectionName) {
		long primaryKey = 0;
		try {
			readWriteLock.writeLock().tryLock(timeoutTryLock, TimeUnit.MILLISECONDS);
			List<CollectionPrimaryKey> list = support.find(Filters.eq("collectionName", collectionName),
					CollectionPrimaryKey.class);
			if (!CollectionUtils.isEmpty(list)) {
				CollectionPrimaryKey key = list.get(0);
				primaryKey = key.getCurrentValue() + 1;
				key.setCurrentValue(primaryKey);
				support.updateBy_id(key);
			}
		} catch (InterruptedException e) {
			LOG.error("生成主键异常", e);
		} finally {
			readWriteLock.writeLock().unlock();
		}
		return primaryKey;
	}

}
