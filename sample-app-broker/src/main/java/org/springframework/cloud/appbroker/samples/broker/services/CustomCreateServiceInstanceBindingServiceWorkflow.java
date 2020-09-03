package org.springframework.cloud.appbroker.samples.broker.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.appbroker.service.CreateServiceInstanceAppBindingWorkflow;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse.CreateServiceInstanceAppBindingResponseBuilder;
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomCreateServiceInstanceBindingServiceWorkflow implements CreateServiceInstanceAppBindingWorkflow {

    private static final Logger LOG = LoggerFactory.getLogger(CustomCreateServiceInstanceBindingServiceWorkflow.class);

    @Value("${spring.cloud.openservicebroker.catalog.services[0].id}")
    private String backingServiceId;

    @Value("${spring.cloud.appbroker.services[0].apps[0].properties.host}")
    private String host;

    @Value("${spring.cloud.appbroker.services[0].apps[0].properties.domain}")
    private String domain;

    private static final String URI_KEY = "uri";

    @Override
    public Mono<Void> create(CreateServiceInstanceBindingRequest request, CreateServiceInstanceAppBindingResponse response) {
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> accept(CreateServiceInstanceBindingRequest request) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Got request to create service binding: " + request);
        }
        return Mono.just(request.getServiceDefinitionId().equals(backingServiceId));
    }

    @Override
    public Mono<CreateServiceInstanceAppBindingResponseBuilder> buildResponse(CreateServiceInstanceBindingRequest request,
                                                                              CreateServiceInstanceAppBindingResponseBuilder responseBuilder) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Got request to create service binding: " + request);
        }
        return Mono.just(responseBuilder.credentials(buildCredentials()));
    }

    private Map<String, Object> buildCredentials() {
        Map<String, Object> credentials = new HashMap<>();
        credentials.put(URI_KEY, host+"."+domain);
        return credentials;
    }



}
