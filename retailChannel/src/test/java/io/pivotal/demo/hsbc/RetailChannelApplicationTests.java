package io.pivotal.demo.hsbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;

import io.pivotal.demo.cups.RetailChannelApplication;

import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = RetailChannelApplication.class)
@WebAppConfiguration
public class RetailChannelApplicationTests {

	@Test
	public void contextLoads() {
	}

}
