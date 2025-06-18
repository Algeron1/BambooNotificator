package com.rgs.bamboonotifier.Repository;

import com.rgs.bamboonotifier.Entity.DeployMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeployMessageRepository extends CrudRepository<DeployMessage, String> {

    DeployMessage findByEnvironmentId(String envorinmentId);

    DeployMessage findByDeployId(Long deployId);

}
