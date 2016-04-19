package org.project.openbaton.nubomedia.api.persistence;

import org.springframework.data.repository.CrudRepository;

/**
 * Created by Carlo on 19/04/2016.
 */
public interface UserRepository extends CrudRepository<User, String> {

    User findFirstById(String Id);
    User findFirstByUsername(String username);

}
