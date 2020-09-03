package org.springframework.cloud.appbroker.samples.broker;

final class ServiceInstanceServiceOrder {

    private static final int CREATE_SI_WORKFLOW_ORDER = 0;

    // Use a lower order value to give a high precedence
    public static final int VALIDATE_CREATE_PARAMETERS = CREATE_SI_WORKFLOW_ORDER - 400;

    private ServiceInstanceServiceOrder() {
    }

}