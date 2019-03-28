package com.tdh.gps.comm.mongodb.client.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bson.types.Binary;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

/**
 * 
 * @ClassName: TypeHandler
 * @Description: (类型转换工具类)
 * @author wxf
 * @date 2018年11月1日 上午10:31:47
 *
 */
public final class TypeTransformUtils {
	
	/**
	 * 日期转换工具类
	 */
	private static final ThreadLocal<SimpleDateFormat> formatLocal = new ThreadLocal<SimpleDateFormat>();
	/**
	 * 日期格式
	 */
	private static final String YYYY_MM_DD_HH_MM_SS="yyyy-MM-dd hh:mm:ss";
	/**
	 * Object类对象全路径
	 */
	 private static final String OBJECT_ALL_PATH="java.lang.Object";
	
	/**
	 * 
	 * @Title: transform  
	 * @Description: (类型转换)  
	 * @param setMethod
	 * @param target
	 * @param object
	 * @param type
	 * @param fieldName
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws ParseException
	 * @throws
	 */
	public static void transform(Method setMethod, Object target, Object object, Class<?> type, String fieldName)
			throws InvocationTargetException, IllegalAccessException, ParseException {
		if (null == object) {
			return;
		}
		String fieldTypeName = type.getName();
		if (fieldName.equalsIgnoreCase("waybillStatus")) {
			return;
		}
		if (object instanceof ObjectId) {
			object = object.toString();
			setMethod.invoke(target, object);
			return;
		}
		if ("java.lang.String".equalsIgnoreCase(fieldTypeName)) {
			String value = object.toString();
			if ("".equals(value)) {
				return;
			}
			setMethod.invoke(target, value);
			return;
		} else if ("java.lang.Integer".equalsIgnoreCase(fieldTypeName) || "int".equalsIgnoreCase(fieldTypeName)) {
			Integer value = Integer.valueOf(object.toString());
			if (value == null) {
				return;
			}
			setMethod.invoke(target, value);
			return;
		} else if ("java.lang.Long".equalsIgnoreCase(fieldTypeName) || "long".equalsIgnoreCase(fieldTypeName)) {
			Long value = Long.valueOf(object.toString());
			if (value == null) {
				return;
			}
			setMethod.invoke(target, value);
			return;
		} else if ("java.lang.Double".equalsIgnoreCase(fieldTypeName) || "double".equalsIgnoreCase(fieldTypeName)) {
			Double value = Double.valueOf(object.toString());
			if (value == null) {
				return;
			}
			setMethod.invoke(target, value);
			return;
		} else if ("java.lang.Boolean".equalsIgnoreCase(fieldTypeName) || "boolean".equalsIgnoreCase(fieldTypeName)) {
			Boolean value = Boolean.valueOf(object.toString());
			if (value == null) {
				return;
			}
			setMethod.invoke(target, value);
			return;
		} else if ("java.sql.Timestamp".equalsIgnoreCase(fieldTypeName)) {
			Date date = (Date) (object instanceof java.util.Date ? object
					: getSimpleDateFormat().parse(object.toString()));

			setMethod.invoke(target, new java.sql.Timestamp(date.getTime()));
			return;
		} else if ("java.sql.Date".equalsIgnoreCase(fieldTypeName)) {
			Date date = (Date) (object instanceof java.util.Date ? object
					: getSimpleDateFormat().parse(object.toString()));
			setMethod.invoke(target, new java.sql.Date(date.getTime()));
			return;
		} else if ("java.util.Date".equalsIgnoreCase(fieldTypeName)) {
			Date date = (Date) (object instanceof java.util.Date ? object
					: getSimpleDateFormat().parse(object.toString()));
			setMethod.invoke(target, date);
			return;
		}
		// 如果是后添加的 业务集合则不进行反序列化
		if (fieldTypeName.equalsIgnoreCase(List.class.getName())) {
			return;
		}
		if (fieldTypeName.equalsIgnoreCase(Byte.class.getName())) {
			// 有些数据是字节类型， 需要强制转换
			object = Byte.parseByte(object.toString());
			setMethod.invoke(target, object);
			return;
		}
		if (fieldTypeName.equalsIgnoreCase(byte[].class.getName())) {
			// 如果是字节数组则需要强转成 mongodb 对应的 Binary 对象获取字节数组的值
			object = ((Binary) object).getData();
			setMethod.invoke(target, object);
			return;
		}
		if (fieldTypeName.equalsIgnoreCase(BigDecimal.class.getName())) {
			// 转换成 BigDecimal 类型
			object = object instanceof Decimal128 ? ((Decimal128) object).bigDecimalValue()
					: new BigDecimal(object.toString());
			setMethod.invoke(target, object);
			return;
		}
		setMethod.invoke(target, object);
	}

	/**
	 * 
	 * @Title: checkSetMethod  
	 * @Description: (检查类对象的字段是否有Setter方法)  
	 * @param object
	 * @param fieldName
	 * @return boolean 返回类型 
	 * @throws
	 */
	public static boolean checkSetMethod(Class<? extends Object> object, String fieldName) {
		boolean flag = false;
		Method[] methods = object.getDeclaredMethods();
		StringBuilder methodNameBuilde = new StringBuilder();
		methodNameBuilde.append("set").append(fieldName.substring(0, 1).toUpperCase()).append(fieldName.substring(1));
		String methodName = methodNameBuilde.toString();
		for (Method method : methods) {
			if (methodName.equals(method.getName())) {
				flag = true;
				break;
			}
		}
		return flag;
	}
	/**
	 * 
	 * @Title: checkGetMethod  
	 * @Description: (检查类对象的字段是否有Getter方法)  
	 * @param object
	 * @param fieldName
	 * @return boolean 返回类型 
	 * @throws
	 */
	public static boolean checkGetMethod(Class<? extends Object> object, String fieldName) {
		boolean flag = false;
		Method[] methods = object.getDeclaredMethods();
		StringBuilder methodNameBuilde = new StringBuilder();
		methodNameBuilde.append("get").append(fieldName.substring(0, 1).toUpperCase()).append(fieldName.substring(1));
		String methodName = methodNameBuilde.toString();
		for (Method method : methods) {
			if (methodName.equals(method.getName())) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	/**
	 * 
	 * @Title: getSimpleDateFormat  
	 * @Description: (获取日期转换工具类)  
	 * @return SimpleDateFormat 返回类型 
	 * @throws
	 */
	private static SimpleDateFormat getSimpleDateFormat() {
		SimpleDateFormat format= formatLocal.get();
		if(null==format) {
			format =new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
			formatLocal.set(format);
		}
		return format;
	}
}
