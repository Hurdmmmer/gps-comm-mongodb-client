package com.tdh.gps.comm.mongodb.client.base;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.tdh.gps.comm.mongodb.client.content.CollectionNames;
import com.tdh.gps.comm.mongodb.client.model.CollectionPrimaryKey;

/**
 * 
 * @ClassName: CollectionPrimaryKeySupport
 * @Description: (集合主键支持类)
 * @author wxf
 * @date 2018年9月4日 下午3:32:24
 *
 */
@Component("collectionPrimaryKeySupport")
public class CollectionPrimaryKeySupport extends MongoDBBaseSupport<CollectionPrimaryKey> {

	@Override
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	@Override
	public void setPrimaryKeyName(String primaryKeyName) {
		this.primaryKeyName = primaryKeyName;

	}

	@PostConstruct
	@Override
	public void init() {
		this.setCollectionName(CollectionNames.GPS_COLLECTION_PRIMARY_KEY.getName());
		this.setPrimaryKeyName(_ID);

	}

}
