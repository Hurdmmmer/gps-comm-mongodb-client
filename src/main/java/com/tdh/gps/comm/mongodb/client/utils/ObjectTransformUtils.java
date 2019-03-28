package com.tdh.gps.comm.mongodb.client.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.stereotype.Component;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

/**
 * @author wxf
 * @ClassName: ObjectTransformUtils
 * @Description: (对象转换工具类)
 * @date 2018年9月3日 下午5:43:48
 */
@Component("objectTransformUtils")
public class ObjectTransformUtils<T> {

    private static final Log LOG = LogFactory.getLog(ObjectTransformUtils.class);
    //    private static final String SERIALVERSIONUID = "serialVersionUID";
    private static final String ID = "id";
    private static final String _ID = "_id";
    private static final String OBJECT_ALL_PATH = "java.lang.Object";//Object类对象全路径

    /**
     * @param object
     * @return Document 返回类型
     * @throws
     * @Title: objectToDocument
     * @Description: (VO对象转换成MongoDB集合文档对象)
     */
    public Document objectToDocument(T object, Boolean isUpdate) {
        return this.getDocument(object, isUpdate);
    }

    /**
     * @param objects
     * @return List<Document> 返回类型
     * @throws
     * @Title: listToDocuments
     * @Description: (VO对象集合转换成MongoDB集合文档对象集合)
     */
    public List<Document> listToDocuments(List<? extends T> objects, Boolean isUpdate) {
        List<Document> documents = new ArrayList<Document>();
        for (T t : objects) {
            Document document = getDocument(t, isUpdate);
            document.remove(_ID);
            documents.add(document);
        }
        return documents;
    }

    /**
     * @param document
     * @param clazz
     * @return T 返回类型
     * @throws
     * @Title: documentToObject
     * @Description: (MongoDB集合文档对象转换成VO对象)
     */
    public T documentToObject(Document document, Class<T> clazz) {
        return getObject(document, clazz);

    }

    /**
     * @param documents
     * @param clazz
     * @return List<T> 返回类型
     * @throws
     * @Title: listToObjects
     * @Description: (MongoDB集合文档对象集合转换成VO对象集合)
     */
    @SuppressWarnings("unchecked")
    public List<T> listToObjects(FindIterable<Document> documents, Class<? extends Object> clazz) {
        List<T> objects = new ArrayList<T>();
        MongoCursor<Document> mongoCursor = documents.iterator();
        while (mongoCursor.hasNext()) {
            Document document = mongoCursor.next();
            T object = this.getObject(document, clazz);
            if (object != null) {
                objects.add(object);
            }
        }
        mongoCursor.close();
        return objects;
    }

    /**
     * @param object
     * @param fieldName
     * @return Object 返回类型
     * @throws
     * @Title: getFieldValue
     * @Description: (获取字段值)
     */
    public Object getFieldValue(T object, String fieldName) {
        Object result = null;
        boolean flag = false;
        try {
            List<String> fieldNameList = getObjectFieldNameList(object.getClass());
            for (String string : fieldNameList) {
                if (fieldName.equals(string)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                String methodName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                Method method = object.getClass().getDeclaredMethod("get" + methodName);
                result = method.invoke(object);
            }
        } catch (Exception e) {
            LOG.error("获取字段值异常", e);
        }
        return result;
    }

    /**
     * @param object
     * @return Document 返回类型
     * @throws
     * @Title: getDocument
     * @Description: (获取MongoDB集合文档对象)
     */
    private Document getDocument(T object, Boolean isUpdate) {
        Document document = new Document();
        List<String> fieldNameList = getObjectFieldNameList(object.getClass());
        boolean hasLng = false;
        boolean hasLat = false;
        String lng = null;
        String lat = null;
        for (String attributeName : fieldNameList) {
            Field field = null;
            Object attributeValue = null;
            try {
                field = object.getClass().getDeclaredField(attributeName);
                String methodName = attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
                Method getMethod = object.getClass().getDeclaredMethod("get" + methodName);
                attributeValue = getMethod.invoke(object);
                if (attributeValue == null && isUpdate) {
                    // 如果该属性没有值， 则不存放在当前条件中
                    continue;
                }
                if (ID.equals(attributeName)) {
                    if (attributeValue != null) {
                        document.put(ID, attributeValue);
                    } // 添加业务id
//					document.put(_ID, attributeValue);
                } else if ("lat".equalsIgnoreCase(attributeName)
                        && field.getType().getName().equalsIgnoreCase("java.math.BigDecimal")
                        && attributeValue != null) {
                    hasLat = true;
                    lat = getDecimal128Value(attributeValue.toString());

                } else if ("lng".equalsIgnoreCase(attributeName)
                        && field.getType().getName().equalsIgnoreCase("java.math.BigDecimal")
                        && attributeValue != null) {
                    hasLng = true;
                    lng = getDecimal128Value(attributeValue.toString());

                } else if ("alt".equalsIgnoreCase(attributeName)
                        && field.getType().getName().equalsIgnoreCase("java.math.BigDecimal")
                        && attributeValue != null) {
                    document.put(field.getName(), Double.parseDouble(getDecimal128Value(attributeValue.toString())));
                } else if (field.getType().getName().equalsIgnoreCase("java.math.BigDecimal")
                        && attributeValue != null) {
                    document.put(attributeName, getDecimal128Value(attributeValue.toString()));
                } else {
                    document.put(attributeName, attributeValue);
                }
            } catch (Exception e) {
                LOG.error("VO对象转换成MongoDB集合文档对象异常，字段名称：" + attributeName + "|字段值：" + attributeValue, e);
//				try {
//					attributeValue = field.get(object);
//				} catch (IllegalArgumentException | IllegalAccessException e1) {
//					LOG.error("VO对象转换成MongoDB集合文档对象异常", e1);
//				}
            }

        }
        if (hasLng && hasLat && lat != null && lng != null) {
            String jsonStr = "{ type: \"Point\", coordinates:[" + lng + "," + lat + "]}";
            Document locationDoc = new Document();
            locationDoc = locationDoc.parse(jsonStr);
            document.put("location", locationDoc);
        }
        return document;
    }

    /**
     * 获取BigDecimal数值字符串（保留12位精度）
     *
     * @param bigDecimalValue
     * @return
     */
    private String getDecimal128Value(String bigDecimalValue) {
        String strValue = bigDecimalValue;
        String[] split = strValue.split("\\.");
        String intVale = split[0];
        if (split.length == 2) {
            String floatValue = split[1];
            floatValue = floatValue.length() > 13 ? floatValue.substring(0, 12) : floatValue;
            strValue = intVale + "." + floatValue;
        } else {
            strValue = intVale;
        }
        return strValue;
    }

    /**
     * @param document
     * @param clazz
     * @return T 返回类型
     * @throws
     * @Title: getObject
     * @Description: (获得VO对象)
     */
    @SuppressWarnings("unchecked")
    private T getObject(Document document, Class<? extends Object> clazz) {
        List<String> fieldNameList = getObjectFieldNameList(clazz);
        boolean hasLng = false;
        boolean hasLat = false;
        T object = null;
        try {
            object = (T) clazz.newInstance();
        } catch (Exception e) {
            LOG.error("Mongodb 实例化对象失败：" + clazz.getName(), e);
        }
        if (object == null) {
            return null;
        }
        for (String attributeName : fieldNameList) {
            Field field = null;
            Object value = null;
            try {
                field = clazz.getDeclaredField(attributeName);
                Class type = field.getType();
                String methodName = attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
                Method setMethod = clazz.getDeclaredMethod("set" + methodName, type);
//				if (ID.equals(attributeName)) {
////                    obj = document.get(_ID);
//					// 如果文档中有业务字段id,讲业务字段赋值给对象，否则将objectId赋值给对象
//					if (document.get(ID) != null) {
//						value = document.get(ID);
//					} else {
////                        obj = document.get(_ID);
//					}
//				} else 
                if ("lng".equalsIgnoreCase(attributeName)) {
                    hasLng = true;
                    continue;
                } else if ("lat".equalsIgnoreCase(attributeName)) {
                    hasLat = true;
                    continue;
                } else {
                    value = document.get(attributeName);
                }
                if (value == null) {
                    continue;
                }
                TypeTransformUtils.transform(setMethod, object, value, type, attributeName);
            } catch (Exception e) {
                LOG.error("MongoDB集合文档对象转换成VO对象异常，字段名称：" + attributeName + "|字段值：" + value, e);
//				try {
//					field.set(object, document.get(attributeName));
//				} catch (IllegalArgumentException | IllegalAccessException e1) {
//					LOG.error("MongoDB集合文档对象转换成VO对象异常", e1);
//				}
            }
        }
        // 如果文档中有坐标，将坐标转换成属性lng,lat属性值
        if (document.get("location") != null && hasLng && hasLat) {
            Document locValue = (Document) document.get("location");
            List coordinateList = (List) locValue.get("coordinates");
            Double lng = Double.valueOf(coordinateList.get(0).toString());
            Double lat = Double.valueOf(coordinateList.get(1).toString());
            try {
                Method lngSetMethod = clazz.getMethod("setLng", BigDecimal.class);
                Method latSetMethod = clazz.getMethod("setLat", java.math.BigDecimal.class);
                lngSetMethod.invoke(object, new BigDecimal(lng.toString()));
                latSetMethod.invoke(object, new BigDecimal(lat.toString()));
            } catch (Exception e) {
                LOG.error("MongoDB集合文档loc对象转换成lng,lat属性异常", e);
            }
        }
        return object;
    }

    /**
     * @param object
     * @return List<String> 返回类型
     * @throws
     * @Title: getObjectFieldNameList
     * @Description: (获取object对象字段名称列表)
     */
    private List<String> getObjectFieldNameList(Class<? extends Object> object) {
        List<Method> methods = new ArrayList<Method>();
        List<Field> fields = new ArrayList<Field>();
        List<String> result = new ArrayList<String>();
        Class<? extends Object> temp = object;
        while (null != temp) {
            if (OBJECT_ALL_PATH.equals(temp.getName()))
                break;
            methods.addAll(Arrays.asList(temp.getDeclaredMethods()));
            fields.addAll(Arrays.asList(temp.getDeclaredFields()));
            temp = temp.getSuperclass();
        }
        Map<String, String> methodNameMap = new HashMap<String, String>();
        for (Method method : methods) {
            methodNameMap.put(method.getName(), method.getName());
        }
        for (Field field : fields) {
            String attributeName = field.getName();
            String methodName = attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
            if (null != methodNameMap.get("set" + methodName)) {
                result.add(attributeName);
            }
        }
        return result;
    }


}
