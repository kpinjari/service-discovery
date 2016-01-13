package com.example;

import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.common.RelationalServiceInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudConfig {
	
	@Bean
	public CloudFactory cloudFactory() {
		return new CloudFactory();
	}
	
	@Bean
	public Cloud cloud(CloudFactory cloudFactory) {
		return cloudFactory.getCloud();
	}
	
	@Bean RelationalServiceInfo sqlserver(Cloud cloud) {
		return (RelationalServiceInfo)cloud.getServiceInfo("sqlserver");
	}
	@Bean RelationalServiceInfo db2(Cloud cloud) {
		return (RelationalServiceInfo)cloud.getServiceInfo("db2");
	}
	

}