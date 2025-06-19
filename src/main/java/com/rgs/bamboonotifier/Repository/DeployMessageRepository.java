package com.rgs.bamboonotifier.Repository;

import com.rgs.bamboonotifier.Entity.DeployMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeployMessageRepository extends CrudRepository<DeployMessage, String> {

    DeployMessage findByDeployId(Long deployId);

    List<DeployMessage> findAllByEnvironmentIdOrderByCreatedAtDesc(String environmentId);
}
