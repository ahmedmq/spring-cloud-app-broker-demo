package org.springframework.cloud.appbroker.samples.broker.target;

import org.springframework.cloud.appbroker.deployer.DeploymentProperties;
import org.springframework.cloud.appbroker.extensions.targets.ArtifactDetails;
import org.springframework.cloud.appbroker.extensions.targets.Target;
import org.springframework.cloud.appbroker.extensions.targets.TargetFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CustomSpaceTarget extends TargetFactory<CustomSpaceTarget.Config> {

    public CustomSpaceTarget() {
        super(Config.class);
    }

    @Override
    public Target create(Config config) {
        return this::apply;
    }

    private ArtifactDetails apply(Map<String, String> properties, String name, String serviceInstanceId) {
        String space = "my-custom-space";
        properties.put(DeploymentProperties.TARGET_PROPERTY_KEY, space);

        return ArtifactDetails.builder()
                .name(name)
                .properties(properties)
                .build();
    }

    public static class Config {
    }
}
