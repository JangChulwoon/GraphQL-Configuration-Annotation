package com.demo.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class URLRepository {

	@Autowired
	private RequestMappingHandlerMapping requestMapping;

	private Map<String, Map<String, Object>> urlRepository;

	@PostConstruct
	public void init() {
		urlRepository = new HashMap<>();
		Map<RequestMappingInfo, HandlerMethod> handlerMethods = this.requestMapping.getHandlerMethods();
		for (Entry<RequestMappingInfo, HandlerMethod> item : handlerMethods.entrySet()) {
			RequestMappingInfo mapping = item.getKey();
			HandlerMethod method = item.getValue();
			for (String urlPattern : mapping.getPatternsCondition().getPatterns()) {
				Map<String, Object> map = new HashMap<>();
				map.put("class", method.getBeanType().getName());
				map.put("method", method.getMethod().getName());
				map.put("params", method.getMethod().getParameterTypes());
				urlRepository.put(urlPattern, map);
			}
		}
	}
	
	@Bean(name="urlRepositroy")
	public Map<String,Map<String, Object>> getUrlReopository() {
		return urlRepository;
	}

}
