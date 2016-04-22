package org.project.openbaton.nubomedia.api.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

/**
 * Created by Carlo on 19/04/2016.
 */
@Configuration
@EnableResourceServer
public class NubomediaResourceServer  extends ResourceServerConfigurerAdapter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void configure(HttpSecurity http) throws Exception{
        http.headers().frameOptions().disable();
        logger.debug("Enabling web security");
        http.authorizeRequests().regexMatchers(HttpMethod.POST, "/api/v1/nubomedia/paas").access("#oauth2.hasScope('write')").and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and().exceptionHandling();
        http.authorizeRequests().regexMatchers("/api/v1/nubomedia/user").access("#oauth2.hasScope('write')").and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and().exceptionHandling();
        http.authorizeRequests().regexMatchers("/api/v1/nubomedia/user").hasRole("ADMIN").and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and().exceptionHandling();
        http.authorizeRequests().antMatchers("/api/v1/nubomedia/paas").access("#oauth2.hasScope('write')").and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and().exceptionHandling();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId(OAuth2AuthorizationServerConfig.RESOURCE_ID);
    }

}
