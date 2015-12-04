# service-discovery
This project has been created to demonstrate how we can do service discovery in CF. There are several ways of doing it. Each way will be modeled as a branch. The trunk/master will cover "Service discovery using User Provided Services".

<h1>Service Discovery using User Provider Service mechanism<h1>
<h2>Overview<h2>
The goal is to connect or wire a consumer application (in this project is Gateway) and a producer application (RetailChannel) without using properties file to set the location of the producer application.
To do so we are going to use User Provided Service mechanism available in Cloud Foundry (and in any PaaS I would say). This mechanism and the code used to illustrate this solution could have been done in any programming language, or in Java without spring or in older versions of Spring. In other words, if we are using Spring in our project we don't need the latest Spring (4.2.x) to use this mechanism.

The scenario is this:
   Gateway(application) --(http/REST)-> RetailChannel(application)
        (the consumer)                    (the producer)

The RetailChannel may be such application that exposes multiple services each one of them with its own context path. e.g
 retailChannel.somedomain.com/account -> services account related functionality
 retailChannel.somedomain.com/funding -> services deposits related functionality

First of all we deploy the RetailChannel application. And then we create a User Provided Service with the url of the RetailChannel application.

<code>
retailChannel\cf push
cf cups accountService -p '{"uri":"retailchannel.somedomain.com/account"}
cf cups fundingService -p '{"uri":"retailchannel.somedomain.com/deposit"}'
</code>

The Gateway application will manifest (manifest.yml) that it needs one service whose name matches the User Provided Service we just created. PCF will assert that the service exists otherwise it won't less us deploy the Gateway application.
<code>
...
services:
  - accountService
  - fundingService
</code>

The Gateway application starts up with its environment variable VCAP_SERVICES as follows:
<code>
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
 </code>

There is a mechanism provided via the spring-cloud-cloudfoundry-connector project that allows an application to easily access this environment variable.

The Gateway application can easily access the url of these two REST endpoints as follows:

<code>
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
</code>

This is possible thanks to some Java @Configuration:
<code>
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
</code>

And also to the utility project, cloud-web-service-creator. This project configures Cloud Foundry so that it can convert our user provided services tagged as "WebService" into a WebServiceInfo class which facilitates the url of the user-provided-service.


<h2>Instructions how to use it</h2>
Build the projects
<code>
cloud-web-service-creator\mvn install
gateway\mvn install
retailChannel\mvn install
</code>

Deploy and create user provided services
<code>
retailChannel\cf push
cf cups accountService -p '{"uri":"retailchannel.somedomain.com/account"}
cf cups fundingService -p '{"uri":"retailchannel.somedomain.com/deposit"}'
gateway\cf push
</code>

We request the account statements thru the gateway application:
<code>
curl gateway.somedomain.com/gateway/34667/statements
</code>

This request will in turn call retailChannel.somedomain.com/account/34667
