package com.tdh.gps.comm.mongodb.client.content;

/**
 * 
 * @ClassName: CollectionName
 * @Description: (集合名称枚举类)
 * @author wxf
 * @date 2018年9月4日 下午3:44:23
 *
 */
public enum CollectionNames {
//集合主键信息集合
	GPS_COLLECTION_PRIMARY_KEY("gps_collection_primary_key", "_id");
//	集合名称
	private String name;
//	主键
	private String primaryKey;

	private CollectionNames(String name, String primaryKey) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

}
