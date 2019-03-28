package com.tdh.gps.comm.mongodb.client.base;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.tdh.gps.comm.mongodb.client.utils.MongoDBConnectionPoolUtils;
import com.tdh.gps.comm.mongodb.client.utils.ObjectTransformUtils;

/**
 * @author wxf
 * @ClassName: MongoDBBaseSupport
 * @Description: (MongoDB基础支持类)
 * @date 2018年9月3日 下午4:42:29
 */
@PropertySource("classpath:dataSource.properties")
public abstract class MongoDBBaseSupport<T> {
    private static final Log LOG = LogFactory.getLog(MongoDBBaseSupport.class);
    /**
     * @Description: 集合名称
     */
    protected String collectionName;
    /**
     * @Description: 主键名称
     */
    protected String primaryKeyName;

    /**
     * @Description: mongodb自动生成主键名称
     */
    protected static String _ID = "_id";

    /**
     * @Description: 数据库名称
     */
    @Value("${DB.GPS.DATABASE}")
    private String databaseName;

    /**
     * @Description: MongoDB连接池工具类
     */
    @Autowired
    private MongoDBConnectionPoolUtils connectionPoolUtils;

    /**
     * @Description: 对象转换工具类
     */
    @Autowired
    private ObjectTransformUtils<T> objectTransformUtils;

    /**
     * @throws
     * @Title: init
     * @Description: (对象初始化方法)
     */
    public abstract void init();

    /**
     * @param collectionName void 返回类型
     * @throws
     * @Title: setCollectionName
     * @Description: (设置集合)
     */
    public abstract void setCollectionName(String collectionName);

    /**
     * @param primaryKeyName void 返回类型
     * @throws
     * @Title: setPrimaryKeyName
     * @Description: (设置集合主键名称)
     */
    public abstract void setPrimaryKeyName(String primaryKeyName);

    /**
     * @return MongoCollection<Document> 返回类型
     * @throws
     * @Title: getCollection
     * @Description: (用于给子类获取连接操作数据库)
     */
    public MongoCollection<Document> getCollection() {
        return connectionPoolUtils.getCollection(databaseName, collectionName);
    }

    /**
     * @return ObjectTransformUtils<T> 返回类型
     * @throws
     * @Title: getObjectTransformUtils
     * @Description: (用于给子类 转换对象)
     */
    public ObjectTransformUtils<T> getObjectTransformUtils() {
        return objectTransformUtils;
    }

    /**
     * @param object
     * @throws
     * @Title: add
     * @Description: (新增单个)
     */
    public void add(T object) {
        MongoCollection<Document> collection = connectionPoolUtils.getCollection(databaseName, collectionName);
//		Document document = objectTransformUtils.objectToDocument(object, false);
        //null值字段不插入
        Document document = objectTransformUtils.objectToDocument(object, false);
        document.remove(_ID);
        collection.insertOne(document);
    }

    /**
     * @param objects
     * @throws
     * @Title: addList
     * @Description: (新增集合)
     */
    public void addList(List<? extends T> objects) {
        MongoCollection<Document> collection = connectionPoolUtils.getCollection(databaseName, collectionName);
//		List<Document> documents = objectTransformUtils.listToDocuments(objects, false);
        //null值字段不插入
        List<Document> documents = objectTransformUtils.listToDocuments(objects, false);
        for (Document document : documents) {
            document.remove(_ID);
        }
        collection.insertMany(documents);
    }

    /**
     * @param object
     * @return long 返回类型
     * @throws
     * @Title: updateById
     * @Description: (根据主键更新)
     */
    public long updateById(T object) {
        return updateBy_id(object);
    }

    /**
     * @param id
     * @return long 返回类型
     * @throws
     * @Title: deleteById
     * @Description: (根据主键删除)
     */
    public long deleteById(String id, boolean isLogicDelete) {
        MongoCollection<Document> collection = connectionPoolUtils.getCollection(databaseName, collectionName);
        if (isLogicDelete) {
            UpdateResult result = collection.updateMany(Filters.eq(_ID, new ObjectId(id)),
                    new Document("$set", new Document("deleted", 1)));
            return result.getModifiedCount();
        } else {
            DeleteResult deleteResult = collection.deleteOne(Filters.eq(_ID, new ObjectId(id)));
            return deleteResult.getDeletedCount();
        }
    }

    /**
     * @param filter
     * @param clazz
     * @return List<T> 返回类型
     * @throws
     * @Title: find
     * @Description: (查询)
     */
    public List<T> find(Bson filter, Class<? extends Object> clazz) {
        MongoCollection<Document> collection = connectionPoolUtils.getCollection(databaseName, collectionName);
        FindIterable<Document> iterable = collection.find(filter);
        List<T> objects = objectTransformUtils.listToObjects(iterable, clazz);
        return objects;
    }

    /**
     * @param clazz
     * @return List<T> 返回类型
     * @throws
     * @Title: findAll
     * @Description: (查找所有)
     */
    public List<T> findAll(Class<? extends Object> clazz) {
        MongoCollection<Document> collection = connectionPoolUtils.getCollection(databaseName, collectionName);
        FindIterable<Document> documents = collection.find();
        return objectTransformUtils.listToObjects(documents, clazz);
    }

    /**
     * @param filter
     * @param clazz
     * @param page
     * @param pageSize
     * @return List<T> 返回类型
     * @throws
     * @Title: findPage
     * @Description: (根据分页查询)
     */
    public List<T> findPage(Bson filter, Class<T> clazz, Integer page, Integer pageSize) {
        MongoCollection<Document> collection = connectionPoolUtils.getCollection(databaseName, collectionName);
        FindIterable<Document> iterable = null;
        if (filter == null) {
            iterable = collection.find().skip(page == null ? 0 : page).limit(pageSize == null ? 0 : pageSize);
        } else {
            iterable = collection.find(filter).skip(page == null ? 0 : page).limit(pageSize == null ? 0 : pageSize);
        }
        return objectTransformUtils.listToObjects(iterable, clazz);
    }

    /**
     * @param id
     * @param clazz
     * @return T 返回类型
     * @throws
     * @Title: findById
     * @Description: (根据主键查询单个)
     */
    public T findById(String id, Class<T> clazz) {
        MongoCollection<Document> collection = connectionPoolUtils.getCollection(databaseName, collectionName);
        FindIterable<Document> doc = collection.find(Filters.eq(_ID, new ObjectId(id)));
        MongoCursor<Document> iterator = doc.iterator();
        if (iterator.hasNext()) {
            Document document = iterator.next();
            T result = objectTransformUtils.documentToObject(document, clazz);
            iterator.close();
            return result;
        }
        iterator.close();
        return null;
    }

    /**
     * @param object
     * @return long 返回类型
     * @throws
     * @Title: updateBy_id
     * @Description: (根据主键_id更新)
     */
    public long updateBy_id(T object) {
        MongoCollection<Document> collection = connectionPoolUtils.getCollection(databaseName, collectionName);
        Document document = objectTransformUtils.objectToDocument(object, true);
        ObjectId id = new ObjectId(document.get(_ID).toString());
        document.remove(_ID);
        UpdateResult result = collection.updateMany(Filters.eq(_ID, id), new Document("$set", document));
        return result.getModifiedCount();
    }

    /**
     * @param record
     * @return List<T> 返回类型
     * @throws
     * @Title: findBySelective
     * @Description: (根据实例对象组合查询条件)
     */
    public List<T> findBySelective(T record) {
        Field[] fields = record.getClass().getDeclaredFields();
        List<Bson> conditions = new ArrayList<>();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = objectTransformUtils.getFieldValue(record, field.getName());
                if (value == null) {
                    continue;
                }
                conditions.add(Filters.eq(field.getName(), value));
            } catch (Exception e) {
                LOG.error("根据实例对象组合查询条件异常", e);
                return null;
            }
        }
        // 2018年9月7日 添加如果不存在条件则查找所有
        if (conditions.size() == 0) {
            return this.findAll(record.getClass());
        }
        return find(Filters.and(conditions), record.getClass());
    }

    /**
     * @param fieldName
     * @param conditions
     * @return long 返回类型
     * @throws
     * @Title: deleteBy
     * @Description: (根据字段和传入的集合逻辑删除文档)
     */
    public long deleteBy(String fieldName, List<?> conditions) {
        MongoCollection<Document> collection = connectionPoolUtils.getCollection(databaseName, collectionName);
        UpdateResult deleted = collection.updateMany(Filters.in(fieldName, conditions),
                new Document("$set", new Document("deleted", 1)));
        return deleted.getModifiedCount();
    }

}
