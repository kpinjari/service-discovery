package com.example;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.service.common.RelationalServiceInfo;

@SpringBootApplication
public class DatasourceDemoApplication {

	@Autowired List<RelationalServiceInfo> dbs = new ArrayList<>();
	
	@PostConstruct 
	public void displayDbs() {
		dbs.forEach(c -> System.out.println(c));
	}
    public static void main(String[] args) {
        SpringApplication.run(DatasourceDemoApplication.class, args);
    }
}
