# service-discovery
This project has been created to demonstrate how we can do service discovery in CF. There are several ways of doing it. Each way will be modeled as a branch. The trunk/master will cover "Service discovery using User Provided Services".
In addition to service discovery, this project also demonstrate how we can run the same application in the cloud and locally without doing any code changes. Our sample application called gateway requires an Oracle Database. Cloud Foundry service connector supports Oracle as one of its services. However, Local Config service connector does not therefore we are going to demostrate how to add an OracleServiceInfoCreator. 

<h2>Service Discovery using User Provider Service mechanism</h2>
<h3>Overview</h3>
The goal is to connect or wire a consumer application (in this project is Gateway) and a producer application (RetailChannel) without using properties file to set the location of the producer application.
To do so we are going to use <a href="https://docs.run.pivotal.io/devguide/services/user-provided.html">User Provided Service</a> mechanism available in Cloud Foundry (and in any PaaS I would say). We could have implemented this solution in other programming language support by CF, or in Java without Spring Framework or in older versions of Spring Framework. In other words, if we are using Spring in our project we don't need the latest Spring (4.2.x) to use this mechanism.

The scenario is this:
<pre>
   Gateway(application) --(http/REST)-> RetailChannel(application)
        (the consumer)                    (the producer)
</pre>

The RetailChannel may be such application that exposes multiple services each one of them with its own context path. e.g
<pre>
 retailChannel.somedomain.com/account -> services account related functionality
 retailChannel.somedomain.com/funding -> services deposits related functionality
</pre>

First of all we deploy the RetailChannel application. The team responsible of this service are also who create a User Provided Service with the url of the RetailChannel application.

<pre>
cf cups accountService -p '{"uri":"retailchannel.somedomain.com/account", "tag":"WebService"}'
cf cups fundingService -p '{"uri":"retailchannel.somedomain.com/deposit", "tag":"WebService"}'
</pre>

The Gateway application will manifest (manifest.yml) that it needs one service whose name matches the User Provided Service we just created. PCF will assert that the service exists otherwise it won't less us deploy the Gateway application.
<pre>
---
applications:
- name: gateway
  memory: 1024M
  instances: 1
  path: target/gateway-0.0.1-SNAPSHOT.jar
  host: gateway
  domain: somedomain.com
  services:
  - accountService
  - fundingService
</pre>

The Gateway application starts up with its environment variable <a href="https://docs.run.pivotal.io/devguide/deploy-apps/environment-variable.html">VCAP_SERVICES</a> as follows:
<pre>
"VCAP_SERVICES": {
  "user-provided": [
   {
    "credentials": {
     "tag": "WebService",
     "uri": "https://retailchannel.somedomain.com/account"
    },
    "label": "user-provided",
    "name": "accountService",
    "syslog_drain_url": "",
    "tags": []
   },
   {
    "credentials": {
     "tag": "WebService",
     "uri": "https://retailchannel.somedomain.com/deposit"
    },
    "label": "user-provided",
    "name": "fundingService",
    "syslog_drain_url": "",
    "tags": []
   }
  ]
 }
 </pre>

There is a mechanism provided via the <a href="https://github.com/spring-cloud/spring-cloud-connectors/tree/master/spring-cloud-cloudfoundry-connector">spring-cloud-cloudfoundry-connector</a> project that allows an application to easily access this environment variable.

The Gateway application can easily access the url of these two REST endpoints as follows:

<pre>
@RestController
@RequestMapping("/gateway")
public class GatewayController {

	@Autowired @Qualifier("accountService") WebServiceInfo accountService;  
	@Autowired @Qualifier("fundingService") WebServiceInfo fundingService;  

  @RequestMapping("/{account}/statements")
	public List<AccountTransaction> accountStatement(@PathVariable("account") String account) {
		String url = accountService.get().getUri() + "/" + account;
    ...
	}
</pre>

This is possible thanks to some Java @Configuration:
<pre>
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
}
</pre>

WebServiceInfo class is declared in utility project, cloud-web-service-creator which is part of this solution. This project configures Cloud Foundry so that it can convert our user provided services tagged with the name "WebService" (look at how we created the user provided service above) into a WebServiceInfo class. 


<h3>Instructions how to use it</h3>
Build the projects
<pre>
cloud-web-service-creator\mvn install
gateway\mvn install
retailChannel\mvn install
</pre>

Deploy and create user provided services
<pre>
retailChannel\cf push
cf cups accountService -p '{"uri":"retailchannel.somedomain.com/account", "tag":"WebService"}'
cf cups fundingService -p '{"uri":"retailchannel.somedomain.com/deposit", "tag":"WebService"}'
gateway\cf push
</pre>

We request the account statements thru the gateway application:
<pre>
curl gateway.somedomain.com/gateway/34667/statements
</pre>

This request will in turn call retailChannel.somedomain.com/account/34667

<h3>Scaling RetailChannel application</h3>
It is worth noting that in the manifest of the RetailChannel application we specified 2 instances. This means that the calls from the Gateway application into the RetailChannel application are load balanced. To probe it tail the logs of the retailChannel and try a few request to the gateway application. 
<pre>
cf logs retailChannel
</pre>
In a separate terminal run
<pre>
retailChannel.somedomain.com/account/34667
retailChannel.somedomain.com/account/44566
</pre>
And the expected outcome in the logs. See that one request was handled by APP/0 and the other by APP/1.
<pre>
2015-12-04T17:15:27.80+0100 [APP/0]      OUT prepare accoutstatments for 34667
2015-12-04T17:15:27.80+0100 [APP/1]      OUT prepare accoutstatments for 44566
</pre>

<h3>Consuming a Restful web service</h3>
On this project, we have used Spring RestTemplate to access the RetailChannel's endpoints. In other branches we will explore more convenient mechanisms.
<pre>
@RestController
@RequestMapping("/gateway")
public class GatewayController {

	@Autowired @Qualifier("accountService") Optional<WebServiceInfo> accountService;  
	@Autowired RestTemplate restTemplate;
		
	@RequestMapping("/{account}/statements")
	public List<AccountTransaction> accountStatement(@PathVariable("account") String account) {
		String url = accountService.get().getUri() + "/" + account;
		ResponseEntity<List<AccountTransaction>> rateResponse = restTemplate.exchange(url,
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<AccountTransaction>>() {
            	});
		List<AccountTransaction> statements = rateResponse.getBody();
		return statements;
	}
</pre>


<h2>How to make the LocalConfig connector to support Oracle database</h2>

We proceed the same way we did it for the WebServiceInfoCreator class. In the project cloud-web-service-creator (we should consider renaming that project or create two distinct projects one for cloudFoundry and another for localconfig) we add a new package for localConfig where we are going to add our OracleServiceInfoCreator and WebServiceInfoCreator classes. We also a new file (META-INF/services/org.springframework.cloud.localconfig.LocalConfigServiceInfoCreator) under src/main/resources where we list our two service creators.
<pre>
io.pivotal.demo.cups.cloud.local.WebServiceInfoCreator
io.pivotal.demo.cups.cloud.local.OracleServiceInfoCreator
</pre>

That's it. Now, we can run our applications locally. See next section on how to run the application locally.

Note about OracleServiceInfoCreator: Oracle requires a connection url incompatible with the standard urls supported by databases like mongodb or mysql. It is for this reason that first of all, we use the same url we specified in the cloud, i.e jdbc:oracle ... And second, we shall only rely on the getJdbcUrl() method in the class RelationalServiceInfo in order to get the connection url. In other words, we shall not rely on the methods getUsername(), etc, because they will be null.

<h2>How to make the application run in local mode</h2>
There are different ways of doing it (full description of all options <a href="http://cloud.spring.io/spring-cloud-connectors/spring-cloud-connectors.html#_local_configuration_connector">here</a>). In the gateway project we have created a spring-cloud-bootstrap.properties which tells the local connector to look up for the service definitions in a file called spring-local-cloud.properties. In practice, this file should be outside this project and hence not in github because this file contains passwords. Instead, this file should be in the user.home directory. 
This is the content of the spring-cloud-bootstrap.properties:
<pre>
spring.cloud.propertiesFile=src/main/resources/spring-local-cloud.properties
</pre>

The LocalConfig is activated provided the spring-local-cloud.properties file contains a property called <b>spring.cloud.appId</b>. Additionally, we define our services' url and database's connection urls as follows:
<pre>
spring.cloud.appId=gateway
spring.cloud.accountService=http://myhost-account:90
spring.cloud.fundingService=http://myhost-funding:90

spring.cloud.oracle=jdbc:oracle:thin:userId/password@192.168.127.1:2000/DHKTLRDB
</pre>

Important: Spring Cloud will first check whether it is running under various cloud providers like Cloud Foundry before checking whether it is running locally. It is for this reason that you should not have in your local environment any VCAP_* properties declared. 




