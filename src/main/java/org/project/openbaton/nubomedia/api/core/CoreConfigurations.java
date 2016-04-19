package org.project.openbaton.nubomedia.api.core;


import org.project.openbaton.nubomedia.api.openbaton.OpenbatonCreateServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Carlo on 19/04/2016.
 */
@Configuration
public class CoreConfigurations {

    @Bean
    public Map<String, OpenbatonCreateServer> getDeploymentMap(){

        return new HashMap<>();

    }

}
