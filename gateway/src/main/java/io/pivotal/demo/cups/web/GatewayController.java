package io.pivotal.demo.cups.web;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.service.common.RelationalServiceInfo;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.pivotal.demo.cups.cloud.WebServiceInfo;

@RestController
@RequestMapping("/gateway")
public class GatewayController {
	
	@Autowired @Qualifier("accountService") Optional<WebServiceInfo> accountService;  
	@Autowired @Qualifier("fundingService") Optional<WebServiceInfo> fundingService;  
	@Autowired @Qualifier("oracle") Optional<RelationalServiceInfo> oracle;
	@Autowired @Qualifier("db2") Optional<RelationalServiceInfo> db2;
	
	@Autowired RestTemplate restTemplate;
	
	@PostConstruct
	public void printCloudSettings () {
		System.out.println("AccountService: " + (accountService.isPresent() ? accountService.get() : " not present"));
		System.out.println("FundingService: " + (fundingService.isPresent() ? fundingService.get() : " not present"));
		System.out.println("oracle: " + (oracle.isPresent() ? oracle.get().getJdbcUrl() : " not present"));
		System.out.println("db2: " + (db2.isPresent() ? db2.get() : " not present"));
		
	}
	
	// note: in prod, the account id will actually come from the securityContext and not
	// from a parameter
	@RequestMapping("/{account}/statements")
	public List<AccountTransaction> accountStatement(@PathVariable("account") String account) {
		String url = accountService.get().getUri() + "/" + account;
		ResponseEntity<List<AccountTransaction>> rateResponse =
        restTemplate.exchange(url,
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<AccountTransaction>>() {
            });
		List<AccountTransaction> statements = rateResponse.getBody();
		return statements;
	}
	
}
