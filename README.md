# Spring Cloud App Broker Demo

This repo contains a step-by-step tutorial on implementing a simple service broker that conforms to the [Open Service Broker API](https://www.openservicebrokerapi.org/) specification and deploys applications and backing services to Cloud Foundry using the [Spring Cloud App Broker](https://spring.io/projects/spring-cloud-app-broker).

See Git tags for step-by-step progress of configuring the broker using Spring Cloud App broker. Clone this repository and run the below from the root of the project folder:

```bash
$ git tag -ln

v1			Build a simple Service Broker
v2 			Configure app deployment properties through App Broker configuration
v3			Configure service instance/binding lifecycle using Workflows
v4			Configure Parameter Transformer	for backing application deployment
v5			Persiting Service Instance/Binding State 
v6			Configure custom Target locations for backing applications
```

## Introduction

Previously, in order to build a Spring Boot based service broker application, you would add the [Spring Cloud Open Service Broker](https://spring.io/projects/spring-cloud-open-service-broker) starter to the the project, specify the configuration and implement the required interfaces such as [ServiceInstanceService](https://github.com/cloudfoundry-community/spring-boot-cf-service-broker/blob/master/src/main/java/org/cloudfoundry/community/servicebroker/service/ServiceInstanceService.java) and [ServiceInstanceBindingService](https://github.com/cloudfoundry-community/spring-boot-cf-service-broker/blob/master/src/main/java/org/cloudfoundry/community/servicebroker/service/ServiceInstanceBindingService.java) interface. Spring Cloud Open Service broker is less opinionated about server broker implementation and leaves many of the decisions to the developer, requiring the developer to implement all of the broker application logic themselves like managing service instances, managing state, backing app deployment etc. A second project [Spring Cloud App Broker](https://spring.io/projects/spring-cloud-app-broker/) is available which is an abstraction on top of Spring Cloud Open Service broker and provides opinionated implementations of the corresponding interfaces and also deploys applications and backing services to a platform, such as Cloud Foundry or Kubernetes.

> Spring Cloud App Broker builds on Spring Cloud Open Service broker. You must provide Spring Cloud Open Service Broker configuration in order to use Spring Cloud App Broker.

The following are some features of Spring Cloud App Broker in comparison to the Spring Cloud Open Service Broker

### Configure App Deployment

With `spring-cloud-app-broker`, you can declare the details of services, including applications to deploy, application deployment details and their dependent backing services in the App Broker configuration (using properties under `spring.cloud.appbroker.services`). For instance, you can specify the number of service instances to be deployed, the memory and disk resource requirements , etc for specific service instance deployments. App broker will manage the deployment and provision of dependent apps and services and bind those services and apps where appropriate. Conversely when a request is received to delete a service instance, App Broker will unbind and delete dependent services and applications that were previously provisioned

`spring-cloud-app-broker` provides the [AppDeploymentCreateServiceInstanceWorkflow](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/workflow/instance/AppDeploymentCreateServiceInstanceWorkflow.java) / [AppDeploymentUpdateServiceInstanceWorkflow](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/workflow/instance/AppDeploymentUpdateServiceInstanceWorkflow.java) /[AppDeploymentDeleteServiceInstanceWorkflow](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/workflow/instance/AppDeploymentDeleteServiceInstanceWorkflow.java) for managing the deployment of configured backing applications and services.

You can specify default values for all application deployment( using properties under `spring.cloud.appbroker.deployer.cloudfoundry.*`) or override specific deployment ( using properties under `spring.cloud.appbroker.services.*`). App broker also provides interfaces to customize app deployment implementations dynamically.

> Currently, Spring Cloud App Broker supports only Cloud Foundry as a deployment platform.

### Auto configuration

`spring-cloud-app-broker` provides default implementations of most of the components needed to implement a service broker. In Spring Boot fashion, you can override the default behaviour by providing your own implementation of Spring beans, and `spring-boot-app-broker` will back away from its defaults. This reduces lot of boiler plate code and enables to quickly build your own service broker.

For instance, in service instance provisioning / de-provisioning, `spring-cloud-open-service-broker` required a Spring bean that implements the [ServiceInstanceService](https://github.com/cloudfoundry-community/spring-boot-cf-service-broker/blob/master/src/main/java/org/cloudfoundry/community/servicebroker/service/ServiceInstanceService.java) interface. `spring-cloud-app-broker` handles this by providing an implementation in `AppDeploymentCreateServiceInstanceWorkflow`. Additionally service broker authors can implement the [CreateServiceInstanceWorkflow](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/service/CreateServiceInstanceWorkflow.java) to further modify the deployment.

Similarly, For service instance binding/unbinding, `spring-cloud-open-service-broker` required a Spring bean that implements the [ServiceInstanceBindingService](https://github.com/cloudfoundry-community/spring-boot-cf-service-broker/blob/master/src/main/java/org/cloudfoundry/community/servicebroker/service/ServiceInstanceBindingService.java) interface. Spring Cloud App Broker provides the [CreateServiceInstanceAppBindingWorkflow](https://github.com/spring-cloud/spring-cloud-app-broker/blob/master/spring-cloud-app-broker-core/src/main/java/org/springframework/cloud/appbroker/service/CreateServiceInstanceAppBindingWorkflow.java) to manage the service instance bindings

## Repository Tags
The following are the tags to specific commits in the repository that showcase the various features of the Spring Cloud App Broker.

### v1: Build a simple Service Broker

Build a simple service broker application using [Spring Initializr](https://start.spring.io/) and include the `spring-boot` and `spring-cloud-starter-app-broker-cloudfoundry` dependency

-   Define service broker with Spring Boot externalised configuration supplied by `application.yml`
-   Specify the Spring Cloud Open Service Broker configuration( using properties under `spring.cloud.openservicebroker`) and Spring Cloud App Broker configuration(using properties under `spring.cloud.appbroker`)
-   Specify the details of the Cloud Platform deployment (using properties under `spring.cloud.appbroker.deployer`)

Follow the next section for step-by-step tutorial to deploy the service broker to Cloud Foundry

## Deploying the Service Broker to Cloud Foundry

This section will show you how to deploy the broker from this repository to a space in Cloud Foundry and make it available for use via the marketplace.

### Prerequisites

In order to complete the tutorial, please ensure you have:

-   Installed Cloud Foundry [CLI](https://docs.cloudfoundry.org/cf-cli/) in your local workstation to push apps and creating/binding service instances.
-   A Cloud Foundry account and a space to deploy apps. This can be public hosted Cloud Foundry or a private Cloud Foundry.

Before you begin, please be sure you are logged into a Cloud Foundry instance and targeted to an org and space.

## Step 1 - Build the project

This project requires Java 8 at a minimum.

This project is a multi-module Gradle project. The `sample-app-broker` module is a Spring Boot application and contains the code which implements the Spring Cloud App Broker. The `sample-app-service` module is also a Spring Boot application that acts as the service instance which is deployed by the `sample-app-broker`. It has a single endpoint at `/` for test purposes.

- Run the following command from the root of the project folder to compile the code and run tests

  ```bash
  $ ./gradlew clean build
  ```

- Copy the Spring Boot fat JAR from `sample-app-service` to the `/src/main/resources` folder in `sample-app-broker`

  ```bash
  $ cp sample-app-service/build/libs/sample-app-service.jar sample-app-broker/src/main/resources/
  ```
  The app broker configuration provided in `sample-app-broker` deploys the JAR provided in `src/main/resources`

## Step 2 - Deploy the service broker

- From the root of the repository use the supplied manifest to deploy the `sample-app-broker` application.

  ```bash
  $ cf push -f deploy/cloudfoundry/manifest.yml
  ```
  A sample output is shown below where the app is deployed to Cloud Foundy and is started succesfully.

  ```bash
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

    ```bash
    $ cf apps
    ```

    You should see output similar to:
    ```
    Getting apps in org sample / space apps as admin...
    OK

    name                requested state   instances   memory   disk   urls
    sample-app-broker   started           1/1         1G       1G     sample-app-broker.cfapps.haas-222.pez.pivotal.io
    ```

- Accessing the catalog

  Use `curl` or any other REST client to access the broker's catalog via the `/v2/catalog` endpoint. This is the same endpoint used to populate the marketplace

  ```bash
  $ curl http://sample-app-broker.cfapps.haas-222.pez.pivotal.io/v2/catalog
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
  As you can see above the broker exposes a single service called 'sample' which offers a single standard plan.

## Step 3 - Register the broker

Now that the application has been deployed and verified, it can be registered to the Cloud Foundry services marketplace

-   With administrator privileges

    If you have administrator privileges on Cloud Foundry, you can make the service broker available in all organisations and spaces.

    The Open Service Broker API endpoints in the service broker application are secured with a basic auth username and password. Register the service broker using the URL from above and the credentials:

    ```bash
    cf create-service-broker sample-broker admin admin http://sample-app-broker.cfapps.haas-222.pez.pivotal.io
    ```
    Make the service offerings from the service broker visible in the service marketplace

    ```
    $ cf enable-service-access sample-broker
    ```

-   Without administrator privileges

    If you do not have administrator privileges on Cloud Foundry, you can make the service broker available in a single organization and space that you have privileges in:

    ```
    $ cf create-service-broker sample-broker admin admin http://sample-app-broker.cfapps.haas-222.pez.pivotal.io --space-scoped
    ```
    You should see ouptut similar to:
    ```
    Creating service broker sample-broker in org sample / space apps as admin...
    OK
    ```
-  Check the registered Service broker
    ```
    $ cf service-brokers
    Getting service brokers as admin...

    name                      url
    sample-broker             http://sample-app-broker.cfapps.haas-222.pez.pivotal.io
   ```

## Step 4 - View Catalog and service plans

The new service `sample-service` should be now visible in the marketplace along with the other services

> If in the previous step you have registered the broker as space-scoped then the new service will only be show up in the marketplace in the space in which it is registered

- Use the below command to view the marketplace

  ```bash
  $ cf marketplace
  Getting services from marketplace in org sample / space apps as admin...
  OK

  service          plans           description                                           broker
  sample           standard        A sample service                                      sample-broker

  TIP: Use 'cf marketplace -s SERVICE' to view descriptions of individual plans of a given service.
  ```

## Step 5 - Create a service instance

-   Use `cf create-service` to create a service instance

    ```bash
    $ cf create-service sample standard my-sample
    Creating service instance my-sample in org sample / space apps as admin...
    OK

    Create in progress. Use 'cf services' or 'cf service my-sample' to check operation status.
    ```

    This will take some amount of time and in the background will initiate the deployment of the service instances and backing service if any.

- Use `cf services` to verify if the instance is created

  ```bash
  $ cf services
  Getting services in org sample / space apps as admin...

  name        service   plan       bound apps   last operation       broker          upgrade available
  my-sample   sample    standard                create succeeded   sample-broker
  ```

- Check if the service instance has been deployed
  ```bash
  $ cf apps
  Getting apps in org sample / space apps as admin...
  OK

  name                  requested state   instances   memory   disk   urls
  sample-app-broker     started           1/1         1G       1G     sample-app-broker.cfapps.haas-222.pez.pivotal.io
  sample-service-app1   started           1/1         1G       1G     sample-service-app1.cfapps.haas-222.pez.pivotal.io
  ```

  You should see a new app `sample-service-app1` deployed into the same org/space once the service has been succesfully created.

## Step 6 - Service binding

Normally you would bind apps to service instances for the purpose of generating credentials and delivering them to apps. In this case, there is no explicit configuration for service binding and we can directly interact with the service instance using a REST client as shown in the next step. 


## Step 7 - Access the service instance

Using the URI from the `cf apps` output you can access the service instance endpoint

```
# Check the endpoint
$ curl https://sample-service-app1.cfapps.haas-222.pez.pivotal.io/

Hello from brokered service...
```

## Step 8 - Delete the service instance

- Delete the service
  ```bash
  $ cf delete-service my-sample

  Really delete the service my-sample?> y
  Deleting service my-sample in org sample / space apps as admin...
  OK

  Delete in progress. Use 'cf services' or 'cf service my-sample' to check operation status.
  ```

  Deleting the service automatically deletes the service instance as well. 

- Check the apps

  ```bash
  $ cf apps
  Getting apps in org sample / space apps as admin...
  OK

  name                requested state   instances   memory   disk   urls
  sample-app-broker   started           1/1         1G       1G     sample-app-broker.cfapps.haas-222.pez.pivotal.io
  ```

## Step 9 - Delete the broker and application

After completing the above steps, you can clean up by deleting the service broker and the application

- Delete the service broker:

  ```bash
  $ cf delete-service-broker -f sample-app-broker
  ```
  After confirming the deletion, you should see output similar to:

  ```
  Deleting service broker sample-broker as admin...
  OK
  ```

  If you re-run `cf service-brokers` you will not see the `sample-broker` listed

- Delete the app broker application:

  ```bash
  $ cf delete -f sample-app-broker
  ```

