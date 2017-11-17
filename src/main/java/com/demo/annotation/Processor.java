package com.demo.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Processor {

	public Object process(Method method) throws NoSuchMethodException, SecurityException {
		// cls.getDeclaredMethod(methodName);
		Map<String, Object> map = new HashMap<>();
		if (method.getAnnotation(GraphQLConfiguration.class) != null) {
			GraphQLConfiguration configuration = method.getAnnotation(GraphQLConfiguration.class);
			map.put("Type", processType(configuration.type()));
			map.put("Query", processQuery(method, configuration));
		}
		return map;
	}

	private Object processType(Class<?>... classes) {
		List<Object> types = new ArrayList<>();
		Arrays.stream(classes).forEach((cls) -> {
			Map<String, Object> type = new HashMap<>();
			type.put("name", cls.getSimpleName());
			type.put("property", buildProperty(cls.getDeclaredFields()));
			types.add(type);
		});
		return types;
	}

	// 이부분 정리할 수 있나 .? 
	private List<Object> buildProperty(Field[] fields) {
		List<Object> propertys = new ArrayList<>();
		for (Field field : fields) {
			Map<String, Object> property = new HashMap<>();
			Class<?> fieldType = field.getType();
			String typeString = fieldType.getSimpleName();
			String format = "object";
			if (Iterable.class.isAssignableFrom(fieldType)) {
				int depth = 1;
				ParameterizedType returnType = (ParameterizedType) field.getGenericType();
				Type elementType = returnType.getActualTypeArguments()[0];
				while(elementType instanceof ParameterizedType) {
					depth++;
					elementType = ((ParameterizedType) elementType).getActualTypeArguments()[0];
				}
				Class<?> propertyType = (Class<?>)elementType;
				fieldType = propertyType;
				typeString = propertyType.getSimpleName();
				format = "list";
				property.put("depth", depth);
			}
			if (fieldType.isEnum()) {
				typeString = "string";
			}
			property.put("name", field.getName());
			property.put("type", typeString);
			property.put("format", format);
			propertys.add(property);
		}
		return propertys;
	}

	
	private Object processQuery(Method method, GraphQLConfiguration configuration) {
		List<Object> types = new ArrayList<>();
		Map<String, Object> query = new HashMap<>();
		query.put("returnType", configuration.returnType());
		query.put("argument", buildParameter(method));
		query.put("format", configuration.format());
		types.add(query);
		return types;
	}

	private List<Object> buildParameter(Method method) {
		List<Object> arguments = new ArrayList<>();
		for (Parameter params : method.getParameters()) {
			if (params.getAnnotation(GraphQLExcludedParam.class) == null) {
				Map<String, Object> argument = new HashMap<>();
				argument.put("name", params.getName());
				argument.put("argType", params.getType().getSimpleName());
				argument.put("paramType", getParamType(params));
				argument.put("isRequired", "1"); // 1 = true
				arguments.add(argument);
			}
		}
		return arguments;
	}

	private String getParamType(Parameter params) {
		if (params.getAnnotation(RequestParam.class) != null) {
			return "QUERY";
		}
		return "REST";
	}

}
