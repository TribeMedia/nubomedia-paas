package org.project.openbaton.nubomedia.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**
 * Created by Carlo on 19/04/2016.
 */

@Configuration
@EnableAuthorizationServer
@EnableResourceServer
@ComponentScan ("org.project.openbaton.nubomedia.api")
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private NubomediaUserDetailService userDetailsService;

    private TokenStore tokenStore = new InMemoryTokenStore();

    public static final String RESOURCE_ID = "oauth2-server";

    @Override
    public void
    configure(AuthorizationServerEndpointsConfigurer endpoints)
            throws Exception {
        endpoints.tokenStore(this.tokenStore).authenticationManager(this.authenticationManager).userDetailsService(userDetailsService);
    }


    @Override
    public void configure(ClientDetailsServiceConfigurer client) throws Exception {
        client.inMemory().withClient("nubomediaOSClient")
                .secret("nubomedia")
                .authorizedGrantTypes("authorization_code", "refresh_token", "password")
                .scopes("read", "write")
                .resourceIds(RESOURCE_ID);
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setTokenStore(this.tokenStore);
        return tokenServices;
    }

}
