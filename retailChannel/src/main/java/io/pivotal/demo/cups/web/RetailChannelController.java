package io.pivotal.demo.cups.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RetailChannelController {

	
	@RequestMapping("/account/{account}")
	public List<AccountTransaction> accountStatement(@PathVariable("account") String account) {
		System.out.printf("prepare accoutstatments for %s \n", account);
		return loadStatements(account);
	}
	@RequestMapping("/deposit/{account}")
	public List<Deposit> deposits(@PathVariable("account") String account) {
		return Collections.emptyList();
	}
		
	List<AccountTransaction> loadStatements(String account) {
		List<AccountTransaction> statements = new ArrayList<>();
		statements.add(new AccountTransaction(account, System.currentTimeMillis(), 1000, "deposit"));
		statements.add(new AccountTransaction(account, System.currentTimeMillis(), -500, "withdrawal"));
		return statements;
	}
	
	public static class Deposit {
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}
	
}
