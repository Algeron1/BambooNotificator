package com.rgs.bamboonotifier.Repository;

import com.rgs.bamboonotifier.Entity.DeployBanMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeployBanRepository  extends CrudRepository<DeployBanMessage, String> {
}
