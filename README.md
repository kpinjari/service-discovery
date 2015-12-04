# service-discovery
This project has been created to demonstrate how we can do service discovery in CF. There are several ways of doing it. Each way will be modeled as a branch. The trunk/master will cover "Service discovery using User Provided Services".

<h2>Service Discovery using User Provider Service mechanism</h2>
<h3>Overview</h3>
The goal is to connect or wire a consumer application (in this project is Gateway) and a producer application (RetailChannel) without using properties file to set the location of the producer application.
To do so we are going to use <a href="https://docs.run.pivotal.io/devguide/services/user-provided.html">User Provided Service</a> mechanism available in Cloud Foundry (and in any PaaS I would say). This mechanism and the code used to illustrate this solution could have been done in any programming language, or in Java without spring or in older versions of Spring. In other words, if we are using Spring in our project we don't need the latest Spring (4.2.x) to use this mechanism.

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

First of all we deploy the RetailChannel application. And then we create a User Provided Service with the url of the RetailChannel application.

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
  domain: sysvs.mevansam.org
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
cf cups accountService -p '{"uri":"retailchannel.somedomain.com/account"}
cf cups fundingService -p '{"uri":"retailchannel.somedomain.com/deposit"}'
gateway\cf push
</pre>

We request the account statements thru the gateway application:
<pre>
curl gateway.somedomain.com/gateway/34667/statements
</pre>

This request will in turn call retailChannel.somedomain.com/account/34667
