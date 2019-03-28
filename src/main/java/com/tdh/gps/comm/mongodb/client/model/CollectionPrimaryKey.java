package com.tdh.gps.comm.mongodb.client.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @ClassName: CollectionPrimaryKey
 * @Description: (集合主键)
 * @author wxf
 * @date 2018年9月4日 下午1:43:33
 *
 */
public class CollectionPrimaryKey implements Serializable {

	/**
	 * @Fields serialVersionUID : (自动生成的序列化版本号)
	 */
	private static final long serialVersionUID = -5987406149451780429L;

//	主键
	private String _id;
//	集合名称
	private String collectionName;
//	当前值
	private long currentValue;
//	创建时间
	private Date createDate;
//	更新时间
	private Date updateDate;
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getCollectionName() {
		return collectionName;
	}
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}
	public long getCurrentValue() {
		return currentValue;
	}
	public void setCurrentValue(long currentValue) {
		this.currentValue = currentValue;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	

}
