package org.project.openbaton.nubomedia.api.security;

import org.project.openbaton.nubomedia.api.persistence.User;
import org.project.openbaton.nubomedia.api.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Carlo on 19/04/2016.
 */
@Component
public class NubomediaUserDetailService implements UserDetailsService{

    @Autowired private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User authorizedUser = null;

        for (User user : userRepository.findAll()){
            if (user.getUsername().equals(username)){
                authorizedUser = user;
                break;
            }
        }

        if (authorizedUser == null){
            throw new UsernameNotFoundException("Username " + username + " not found");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (authorizedUser.isAdmin()){
            authorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN");
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(authorizedUser.getUsername(),authorizedUser.getPassword(),authorities);

        return userDetails;
    }
}
