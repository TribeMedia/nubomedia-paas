package org.project.openbaton.nubomedia.api.core;

import org.project.openbaton.nubomedia.api.messages.NubomediaUserCreate;
import org.project.openbaton.nubomedia.api.persistence.User;
import org.project.openbaton.nubomedia.api.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

/**
 * Created by Carlo on 19/04/2016.
 */
@RestController
@RequestMapping("/api/v1/nubomedia/user")
public class NubomediaUserManager {

    @Autowired private UserRepository repository;
    @Autowired private OpenshiftManager osmanager;
    private Logger logger;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @RequestMapping(method = RequestMethod.POST)
    public String createUser(@RequestBody  NubomediaUserCreate create){

        if(repository.findFirstByUsername(create.getUsername()) != null){
            return "User " + create.getUsername() + " already exist";
        }
        else{
            User newUser = new User(create.getUsername(),create.getPassword(),create.getMail(),false);
            repository.save(newUser);
            return "User " + create.getUsername() + " successfully created";
        }
    }

    @RequestMapping(value = "/{username}" , method = RequestMethod.GET)
    public User getUserDetails (@PathVariable("username") String username){
        return repository.findFirstByUsername(username);
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.DELETE)
    public String deleteUser (@PathVariable("username") String username){
        User deleteUser = repository.findFirstByUsername(username);
        repository.delete(deleteUser);
        return "User " + username + " successfully deleted";
    }
    //user must be created by an administrator, it also must be assigned to a project
}
