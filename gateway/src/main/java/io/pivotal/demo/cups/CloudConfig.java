package io.pivotal.demo.cups;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.common.RelationalServiceInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.pivotal.demo.cups.cloud.WebServiceInfo;

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
	
	@Bean WebServiceInfo accountService(Cloud cloud) {
		return (WebServiceInfo)cloud.getServiceInfo("accountService");
	}
	@Bean WebServiceInfo fundingService(Cloud cloud) {
		return (WebServiceInfo)cloud.getServiceInfo("fundingService");
	}
	@Bean RelationalServiceInfo oracle(Cloud cloud) {
		return (RelationalServiceInfo)cloud.getServiceInfo("oracle");
	}
	

}