# Spring Cloud App Broker Demo

This repo contains a step-by-step tutorial on implementing a simple service broker that conforms to the [Open Service Broker API](https://www.openservicebrokerapi.org/) specification and deploys applications and backing services to Cloud Foundry using the [Spring Cloud App Broker](https://spring.io/projects/spring-cloud-app-broker).

See Git tags for step-by-step progress of configuring the broker using Spring Cloud App broker. Clone this repository and run the below command from the root of the project folder:

```text
$ git tag -ln

v1			Build a simple Service Broker
v2 			Configure app deployment properties through App Broker configuration
v3			Configure service instance/binding lifecycle using Workflows
v4			Configure Parameter Transformer	for backing application deployment
v5			Persisting Service Instance/Binding State 
v6			Configure custom Target locations for backing applications
```

## Introduction

Previously, in order to build a Spring Boot based service broker application, you would add the [Spring Cloud Open Service Broker](https://spring.io/projects/spring-cloud-open-service-broker) starter to the the project, specify the configuration and implement the required interfaces such as [`ServiceInstanceService`](https://github.com/cloudfoundry-community/spring-boot-cf-service-broker/blob/master/src/main/java/org/cloudfoundry/community/servicebroker/service/ServiceInstanceService.java) and [`ServiceInstanceBindingService`](https://github.com/cloudfoundry-community/spring-boot-cf-service-broker/blob/master/src/main/java/org/cloudfoundry/community/servicebroker/service/ServiceInstanceBindingService.java). Spring Cloud Open Service broker is less opinionated about server broker implementation and leaves many of the decisions to the developer, requiring the developer to implement all of the broker application logic themselves like managing service instances, managing state, backing app deployment etc. A second project [Spring Cloud App Broker](https://spring.io/projects/spring-cloud-app-broker/) is available which is an abstraction on top of Spring Cloud Open Service broker and provides opinionated implementations of the corresponding interfaces and also deploys applications and backing services to a platform, such as Cloud Foundry or Kubernetes.

> Spring Cloud App Broker builds on Spring Cloud Open Service broker. You must provide Spring Cloud Open Service Broker configuration in order to use Spring Cloud App Broker.

The following are some features of Spring Cloud App Broker in comparison to the Spring Cloud Open Service Broker:

### Configure App Deployment

With `spring-cloud-app-broker`, you can declare the details of services, including applications to deploy, application deployment details and their dependent backing services in the App Broker configuration (using properties under `spring.cloud.appbroker.services`). For instance, you can specify the number of service instances to be deployed, the memory and disk resource requirements , etc for specific service instance deployments. App broker will manage the deployment and provision of dependent apps and services and bind those services and apps where appropriate. Conversely when a request is received to delete a service instance, App Broker will unbind and delete dependent services and applications that were previously provisioned

`spring-cloud-app-broker` provides the [`AppDeploymentCreateServiceInstanceWorkflow`](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/workflow/instance/AppDeploymentCreateServiceInstanceWorkflow.java) which handles deploying the configured backing applications and services. Similarly there is [`AppDeploymentUpdateServiceInstanceWorkflow`](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/workflow/instance/AppDeploymentUpdateServiceInstanceWorkflow.java) and [`AppDeploymentDeleteServiceInstanceWorkflow`](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/workflow/instance/AppDeploymentDeleteServiceInstanceWorkflow.java) for updating and deleting the deployment of configured backing applications and services respectively.

You can specify default values for all application deployment( using properties under `spring.cloud.appbroker.deployer.cloudfoundry.*`) or override specific deployment ( using properties under `spring.cloud.appbroker.services.*`). 

> Currently, Spring Cloud App Broker supports only Cloud Foundry as a deployment platform.

### Workflows
App broker allows you to create multiple workflows for the various stages of creating, updating and deleting service instances. For instance, app broker authors can implement
[`CreateServiceInstanceWorkflow`](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/service/CreateServiceInstanceWorkflow.java) and configure it as a Spring bean within the application to hook additional functionality into the request to create a service instance. The [`WorkflowServiceInstanceService`](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/service/WorkflowServiceInstanceService.java) will then pick up this implementation and execute it as part of the service instance creation process. Multiple workflows can be annotated with `@Order` so as to process the workflows in a specific order.

Similarly workflows can be configured for creating and deleting service instance bindings. 

### Auto configuration

`spring-cloud-app-broker` provides default implementations of most of the components needed to implement a service broker. In Spring Boot fashion, you can override the default behaviour by providing your own implementation of Spring beans, and `spring-boot-app-broker` will back away from its defaults. This reduces lot of boiler plate code and enables to quickly build your own service broker.

For instance, in service instance provisioning / de-provisioning, `spring-cloud-open-service-broker` required a Spring bean that implements the [`ServiceInstanceService`](https://github.com/cloudfoundry-community/spring-boot-cf-service-broker/blob/master/src/main/java/org/cloudfoundry/community/servicebroker/service/ServiceInstanceService.java) interface. `spring-cloud-app-broker` handles this by auto configuring an implementation in `AppDeploymentCreateServiceInstanceWorkflow`. 

Similarly if no implementation is provided for persisting service instance/service binding, then the app broker provides an [`InMemoryServiceInstanceStateRepository`](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/state/InMemoryServiceInstanceStateRepository.java) which provides an in memory `Map` to save state and offers an easy getting-started experience. 

> The `InMemoryServiceInstanceStateRepository` is provided for demonstration and testing purpose only. It is not suitable for production grade applications!

There are additional auto configuration performed by App Broker. Please look at [`AppBrokerAutoConfiguration`](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-autoconfigure/src/main/java/org/springframework/cloud/appbroker/autoconfigure/AppBrokerAutoConfiguration.java) class for further details.

## Repository Tags
The following are the tags to specific commits in the repository that showcase the various features of the Spring Cloud App Broker.

### v1: Build a simple Service Broker

Build a simple service broker application using [Spring Initializr](https://start.spring.io/) and include the `spring-boot-starter` and `spring-cloud-starter-app-broker-cloudfoundry` dependency

-   Define service broker with Spring Boot externalised configuration supplied by `application.yml`
-   Specify the Spring Cloud Open Service Broker configuration( using properties under `spring.cloud.openservicebroker`) and Spring Cloud App Broker configuration(using properties under `spring.cloud.appbroker`)
-   Specify the details of the Cloud Platform deployment (using properties under `spring.cloud.appbroker.deployer`)


### v2: Configure app deployment properties through App Broker configuration

Update the Spring Cloud App Broker configuration(using properties under `spring.cloud.appbroker`):

- Configure the memory requirements for the app instance using the property `spring.cloud.appbroker.services[0].apps.properties.memory`
- Configure the number of service instances to be deployed using the property `spring.cloud.appbroker.services[0].apps.properties.count`

> The above configuration of `count` will result in two instances of the application to be deployed into CloudFoundry

For a complete list of properties please see [Properties Configuration](https://docs.spring.io/spring-cloud-app-broker/docs/1.1.1.RELEASE/reference/#properties-configuration)


### v3: Configure service instance/binding lifecycle using Workflows

Add workflows to configure perform actions before or after create, update, delete, bind and unbind. 

- Create `CustomCreateServiceInstanceBindingServiceWorkflow` class by implementing `CreateServiceInstanceAppBindingWorkflow` interface to generate the connection URI of the deployed service instance. Binding the service to an app will add the URI of the deployed instance to `VCAP_SERVICES` environment variable of the app. The URI is generated using the app broker configuration properties `spring.cloud.appbroker.services[0].apps[0].properties.host` and `spring.cloud.appbroker.services[0].apps[0].properties.domain`. These two configuration properties are also used to map routes for the deployed application.
- Create `ServiceInstanceParametersValidatorWorkflow` by implementing `CreateServiceInstanceWorkflow` interface to validate the parameters before the service instance is created. The class `ServiceInstanceServiceOrder` is used to specify the order of the workflows.

### v4:	Configure Parameter Transformer	for backing application deployment

Update the Spring Cloud App Broker [`Parameters Transformers`](https://docs.spring.io/spring-cloud-app-broker/docs/1.1.1.RELEASE/reference/#service-instance-parameters) configuration(using properties under `spring.cloud.appbroker.services.apps.parameters-transformers`):

- Configure `PropertyMapping` parameter transformer for the service instance using the property `spring.cloud.appbroker.services[0].apps.parameters-transformers.name` and include the `count` and `memory` properties to set deployment properties of the backing application from parameters provided when a service instance is created or updated.

- Configure `EnvironmentMapping` parameter transformer for the app instance using the property `spring.cloud.appbroker.services[0].apps.parameters-transformers.name` and include the `count`,`memory` and `lang` properties to populate environment variables on the backing application from parameters provided when a service instance is created or updated

- Implement a custom `ParameterTransformer` to include business logic to handle user input parameters and make it available to the service instance. Create a `RequestTimeoutParameterTransformer` where we are going to map a parameter from `request-timeout-ms` to an environment variable `sample-app.httpclient.connect-timeout`.

### v5: Persisting Service Instance/Binding State 

- Persisting Service Instance State

  - Create a model class `ServiceInstance` to store the service instance data.
  - Create a repository interface definition by using [Spring Data's](https://spring.io/projects/spring-data) `CrudRepository`. In this example we have used the `ReactiveCrudRepository` implementation by using [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc) project.
  - Finally register `Bean` object `DefaultServiceInstanceStateRepository` that implements the [`ServiceInstanceStateRepository`](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/state/ServiceInstanceStateRepository.java) to persists the service instance state using the above created repository.

- Persisting Service Instance Binding State
  - Create a model class `ServiceInstanceBinding` to store the service instance data.
  - Create a repository interface definition `ServiceInstanceBindingRepository` by using [Spring Data's](https://spring.io/projects/spring-data) `CrudRepository`. In this example we have used the `ReactiveCrudRepository` implementation by using [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc) project.
  - Finally register `Bean` object `DefaultServiceInstanceBindingStateRepository` that implements the [`ServiceInstanceBindingStateRepository`](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/state/ServiceInstanceBindingStateRepository.java) to persists the service instance binding using the above created repository.

- Create required configuration classes for MySQL connection using the [r2dbc-mysql](https://github.com/mirromutth/r2dbc-mysql) library. Parse the Cloud Foundry environment variable `VCAP_SERVICES` to get the connection parameters. The folder 'src/main/resources' includes `schema.sql` which specifies the table definition and is executed during the initialization process. 

> You will need to create a MYSQL database instance in Cloud Foundry prior to using `cf push`. You will need to use the same MYSQL instance name provided in the manifest file when creating the instance using `cf create-service..`. 

### v6: Configure custom Target locations for backing applications

- Specify a new Target for deploying the backing applications
  
  - Create a `CustomSpaceTarget` that extends [TargetFactory](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/extensions/targets/TargetFactory.java)
  - Specify the custom target in the app broker configuration 
  
By default, for Cloud Foundry the backing application is deployed to the org named by `spring.cloud.appbroker.deployer.cloudfoundry.default-org` and the space named by `spring.cloud.appbroker.deployer.cloudfoundry.default-space`. However, in the above case the application is deployed to `custom-space` space in Cloud Foundry.

Follow the next section for step-by-step tutorial to deploy the service broker to Cloud Foundry

## Deploying the Service Broker to Cloud Foundry

This section will show you how to deploy the broker from this repository to a space in Cloud Foundry and make it available for use via the marketplace.

### Prerequisites

In order to complete the tutorial, please ensure you have:

-   Installed Cloud Foundry [CLI](https://docs.cloudfoundry.org/cf-cli/) in your local workstation to push apps and creating/binding service instances.
-   A Cloud Foundry account and a space to deploy apps. This can be public hosted Cloud Foundry or a private Cloud Foundry.

Before you begin, please be sure you are logged into a Cloud Foundry instance and targeted to an org and space.

Update the `application.yml` in `src/main/resources` of `sample-app-broker` to specify the connection details for your cloudfoundry instance.
Also, need to update the `domain` property under `appbroker.services[0].apps.properties` to provide the domain name of your cloud foundry instance.

For the steps below, replace the correct domain of your cloud foundry instance when executing the command. The steps below use `example.io` as a domain.

## Step 1 - Build the project

This project requires Java 8 at a minimum.

This project is a multi-module Gradle project. 

`sample-app-broker` module is a Spring Boot application and contains the code which implements the Spring Cloud App Broker. 

`sample-app-service` module is a Spring Boot application that acts as the service instance which is deployed by the `sample-app-broker`. It has a single endpoint at '`/`' which returns back a sample string response.

- Run the following command from the root of the project folder to compile the code and run tests

  ```text
  $ ./gradlew clean build
  ```
The gradle build for `sample-app-broker` configures the `bootJar` task to copies the JAR of `sample-app-service` into its classpath.   The app broker configuration provided in `sample-app-broker` YAML file deploys the `sample-app-service` JAR as a service instance.

## Step 2 - Deploy the service broker

- Prior to deploying the broker create a mysql instance to store the service instance and binding state:
  ```
  $ cf create-service p.mysql db-small mysql

  Creating service instance mysql in org sample / space apps as admin...
  OK

  Create in progress. Use 'cf services' or 'cf service mysql' to check operation status.
  ```

- After the mysql instance is created, from the root of the repository use the supplied manifest to deploy the `sample-app-broker` application.

  ```text
  $ cf push -f deploy/cloudfoundry/manifest.yml
  ```
  A sample output is shown below where the app is deployed to Cloud Foundry and is started successfully.

  ```text
  name:                sample-app-broker
  requested state:     started
  isolation segment:   iso-01
  routes:              sample-app-broker.apps.pcfone.io
  last uploaded:       Thu 27 Aug 20:58:36 IST 2020
  stack:               cflinuxfs3
  buildpacks:          client-certificate-mapper=1.11.0_RELEASE container-security-provider=1.18.0_RELEASE
                      java-buildpack=v4.31.1-offline-<https://github.com/cloudfoundry/java-buildpack.git#03e8eec>
                      java-main java-opts java-security jvmkill-agent... (no decorators apply)

  type:            web
  instances:       1/1
  memory usage:    1024M
  start command:   JAVA_OPTS="-agentpath:$PWD/.java-buildpack/open_jdk_jre/bin/jvmkill-1.16.0_RELEASE=printHeapHistogram=1
                  -Djava.io.tmpdir=$TMPDIR -XX:ActiveProcessorCount=$(nproc)
                  -Djava.ext.dirs=$PWD/.java-buildpack/container_security_provider:$PWD/.java-buildpack/open_jdk_jre/lib/ext
                  -Djava.security.properties=$PWD/.java-buildpack/java_security/java.security $JAVA_OPTS" &&
                  CALCULATED_MEMORY=$($PWD/.java-buildpack/open_jdk_jre/bin/java-buildpack-memory-calculator-3.13.0_RELEASE
                  -totMemory=$MEMORY_LIMIT -loadedClasses=17441 -poolType=metaspace -stackThreads=250 -vmOptions="$JAVA_OPTS")
                  && echo JVM Memory Configuration: $CALCULATED_MEMORY && JAVA_OPTS="$JAVA_OPTS $CALCULATED_MEMORY" &&
                  MALLOC_ARENA_MAX=2 SERVER_PORT=$PORT eval exec $PWD/.java-buildpack/open_jdk_jre/bin/java $JAVA_OPTS -cp
                  $PWD/. org.springframework.boot.loader.JarLauncher
      state     since                  cpu    memory         disk           details
  #0   running   2020-08-27T15:28:55Z   0.0%   154.1M of 1G   162.9M of 1G

  ```

-   Check the status of the application

    ```text
    $ cf apps
    ```

    You should see output similar to:
    ```
    Getting apps in org sample / space apps as admin...
    OK

    name                requested state   instances   memory   disk   urls
    sample-app-broker   started           1/1         1G       1G     sample-app-broker.example.io
    ```

- Accessing the catalog

  Use `curl` or any other REST client to access the `sample-app-broker` catalog via the `/v2/catalog` endpoint. This is the same endpoint used to populate the marketplace. Use the url of the broker application from previous step.

  ```text
  $ curl http://sample-app-broker.example.io/v2/catalog
  {
    "services": [
      {
        "id": "3101b971-1044-4816-a7ac-9ded2e028079",
        "name": "sample",
        "description": "A sample service",
        "bindable": true,
        "plans": [
          {
            "id": "2451fa22-df16-4c10-ba6e-1f682d3dcdc9",
            "name": "standard",
            "description": "A sample plan",
            "free": true,
            "bindable": true
          }
        ],
        "tags": [
          "sample"
        ],
        "metadata": {
          
        },
        "requires": [
          
        ]
      }
    ]
  }
  ```
  As you can see above the broker exposes a service called 'sample' which offers a single standard plan.

## Step 3 - Register the broker

Now that the application has been deployed and verified, it can be registered to the Cloud Foundry services marketplace. 

-   With administrator privileges

    If you have administrator privileges on Cloud Foundry, you can make the service broker available in all organisations and spaces.

    The Open Service Broker API endpoints in the service broker application are secured with a basic auth username and password. Register the service broker using the URL from above and the credentials:

    ```text
    cf create-service-broker sample-broker admin admin http://sample-app-broker.example.io
    ```
    Make the service offerings from the service broker visible in the service marketplace

    ```
    $ cf enable-service-access sample-broker
    ```

-   Without administrator privileges

    If you do not have administrator privileges on Cloud Foundry, you can make the service broker available in a single organization and space that you have privileges in:

    ```
    $ cf create-service-broker sample-broker admin admin http://sample-app-broker.example.io --space-scoped
    ```
    You should see output similar to:
    ```
    Creating service broker sample-broker in org sample / space apps as admin...
    OK
    ```
-  Check the registered Service broker
    ```
    $ cf service-brokers
    Getting service brokers as admin...

    name                      url
    sample-broker             http://sample-app-broker.example.io
   ```

## Step 4 - View Catalog and service plans

The new service `sample-service` should be now visible in the marketplace along with the other services

> If in the previous step you have registered the broker as space-scoped then the new service will only be show up in the marketplace in the space in which it is registered

- Use the below command to view the marketplace

  ```text
  $ cf marketplace
  Getting services from marketplace in org sample / space apps as admin...
  OK

  service          plans           description                                           broker
  sample           standard        A sample service                                      sample-broker

  TIP: Use 'cf marketplace -s SERVICE' to view descriptions of individual plans of a given service.
  ```

## Step 5 - Create a service instance

-   Use `cf create-service` to create a service instance and pass additional configuration parameters as configured above in V4 tag:

    ```text
    $ cf create-service sample standard my-sample -c '{"count":1,"memory":"1G","lang":"en","request-timeout-ms":60}'
    Creating service instance my-sample in org sample / space apps as admin...
    OK

    Create in progress. Use 'cf services' or 'cf service my-sample' to check operation status.
    ```

    This will take some amount of time and in the background will initiate the deployment of the service instance (`sample-app-service`).

- Use `cf services` to verify if the instance is created:

    ```text
    $ cf services
    Getting services in org sample / space apps as admin...

    name        service   plan       bound apps   last operation       broker          upgrade available
    my-sample   sample    standard                create succeeded   sample-broker
    ```

- Check if the service instance has been deployed:
  ```text
  $ cf target -s my-custom-space
  $ cf apps
  Getting apps in org sample / space my-custom-space as admin...
  OK

  name                  requested state   instances   memory   disk   urls
  sample-service-app1   started           1/1         1G       1G     sample-service-app1.example.io
  ```

  You should see a new app `sample-service-app1` deployed into the same `my-custom-space` space once the service has been successfully created. Notice the number of instances is 1, and the memory of 1G has been allocated overriding the static configuration provided in the YAML file.

- Check the environment of the service instance
  ```text
  $ cf env sample-service-app1
  Getting env variables for app sample-service-app1 in org sample / space apps as admin...
  OK
  ...
  User-Provided:
  SPRING_APPLICATION_JSON: {"count":"1","memory":"1G","lang":"en","sample-app.httpclient.connect-timeout":60,"spring.cloud.appbroker.service-instance-id":"ca203ddf-591d-44e4-9e02-56cba3847797"}
  ```

  The environment parameter transformer configuration is applied and the `count`, `memory` `lang` properties are available in the environment. Environment variable `sample-app.httpclient.connect-timeout` is also set by the custom Parameter Transformer implementation.

- Verify if the service instance state is stored in the database. You can either use `cf mysql` to remotely `ssh` into the Cloud Foundry mysql instance or follow instructions [here](https://docs.cloudfoundry.org/devguide/deploy-apps/ssh-services.html). Below we have used `cf mysql`

  ```
  $ cf mysql mysql

  mysql> select * from service_instance;
  +----+--------------------------------------+-----------------------------------+-----------------+--------------+
  | id | service_instance_id                  | description                       | operation_state | last_updated |
  +----+--------------------------------------+-----------------------------------+-----------------+--------------+
  |  1 | f966780f-2027-435c-b099-2f464937bab0 | create service instance completed | SUCCEEDED       | NULL         |
  +----+--------------------------------------+-----------------------------------+-----------------+--------------+
  1 row in set (1.00 sec)

  ```

## Step 6 - Create Service binding

Let us know try to bind the service created above. We do not intend to use the service and so for the sake of simplicity let us bind the service to the the `sample-app-broker` app. Ideally, we would have a separate application that will bind to our new service

- Use `cf bind-service` to bind the instance. Change to the correct cloud foundry space before running the below command:
  ```
  $ cf target -s apps
  $ cf bind-service sample-app-broker my-sample
  ```

- You can then see the URI returned by inspecting the `VCAP_SERVICES` environment variable
  ```
  $ cf env sample-app-broker
  Getting env variables for app sample-app-broker in org sample / space apps as admin...
  OK

  System-Provided:
  {
  "VCAP_SERVICES": {
    "sample": [
    {
      "binding_name": null,
      "credentials": {
      "uri": "sample-service-demo.example.io"
      },
      "instance_name": "my-sample",
      "label": "sample",
      "name": "my-sample",
      "plan": "standard",
      "provider": null,
      "syslog_drain_url": null,
      "tags": [
      "sample"
      ],
      "volume_mounts": []
    }
    ]
  }
  }
  ```
- Verify if the service instance binding state is stored in the database. You can either use `cf mysql` to remotely `ssh` into the Cloud Foundry mysql instance or follow instructions [here](https://docs.cloudfoundry.org/devguide/deploy-apps/ssh-services.html). Below we have used `cf mysql`

  ```
  $ cf mysql mysql

  mysql> select * from service_instance_binding;
  +----+--------------------------------------+--------------------------------------+-------------------------------------------+-----------------+--------------+
  | id | service_instance_id                  | binding_id                           | description                               | operation_state | last_updated |
  +----+--------------------------------------+--------------------------------------+-------------------------------------------+-----------------+--------------+
  |  1 | f966780f-2027-435c-b099-2f464937bab0 | ae217d8e-a97a-401f-87bd-ff9f1d70c0ea | create service instance binding completed | SUCCEEDED       | NULL         |
  +----+--------------------------------------+--------------------------------------+-------------------------------------------+-----------------+--------------+
  1 row in set (0.29 sec)

  ```

## Step 7 - Access the service instance

- Using the URI from the `cf apps` output you can access the service instance endpoint. Change to correct space in Cloud Foundry first

  ```
  # Check the endpoint
  $ cf target -s my-custom-space
  $ curl http://sample-service-app1.example.io/

  Hello from brokered service...
  ```

## Step 8 - Delete the service binding

- Unbind the service instance from the app
  ```
  $ cf target -s apps
  $ cf unbind-service sample-app-broker my-sample
  Unbinding app sample-app-broker from service my-sample in org sample / space apps as admin...
  OK
  ```

## Step 9 - Delete the service instance

We can now begin to clean up the environment after completing the previous step.

- Delete the service:
  ```text
  $ cf delete-service my-sample

  Really delete the service my-sample?> y
  Deleting service my-sample in org sample / space apps as admin...
  OK

  Delete in progress. Use 'cf services' or 'cf service my-sample' to check operation status.
  ```

  This should take some minutes to complete. Deleting the service automatically deletes the service instance as well. 

- Check the apps

  ```text
  $ cf apps
  Getting apps in org sample / space apps as admin...
  OK

  name                requested state   instances   memory   disk   urls
  sample-app-broker   started           1/1         1G       1G     sample-app-broker.example.io

## Step 10 - Delete the broker and application

After completing the above steps, you can clean up by deleting the service broker and the application

- Delete the service broker:

  ```text
  $ cf delete-service-broker -f sample-broker
  ```
  After confirming the deletion, you should see output similar to:

  ```
  Deleting service broker sample-broker as admin...
  OK
  ```

  If you re-run `cf service-brokers` you will not see the `sample-broker` listed

- Delete the app broker application:

  ```text
  $ cf delete -f sample-app-broker
  ```
  
- Delete the mysql instance

  Use the below command to delete the service keys associated with the database:

  ```
  $ cf delete-service-key mysql cf-mysql
  ```
  Finally, delete the mysql instance using: 
  
  ```
  $ cf delete-service mysql
  ```


