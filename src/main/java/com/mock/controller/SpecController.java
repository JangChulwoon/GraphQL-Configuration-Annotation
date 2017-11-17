package com.mock.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.annotation.Processor;

import lombok.extern.slf4j.Slf4j;

@RestController
public class SpecController {

	@Autowired
	Processor processor;
	
	@Autowired
	@Qualifier("urlRepositroy")
	Map<String,Map<String,Object>> urlReopository;
	
	@GetMapping("/spec")
	public Object buildSpecification(@RequestParam("url") String url) throws NoSuchMethodException, ClassNotFoundException {
		url = replaceRESTful(url);
		Map<String, Object> map = urlReopository.get(url);
		Class<?> cls = Class.forName((String)map.get("class"));
		return processor.process(cls.getDeclaredMethod((String)map.get("method"),(Class[]) map.get("params")));
	}
	
	private String replaceRESTful(String url) {
		return url.replace("[", "{").replaceAll("]", "}");
	}
}
