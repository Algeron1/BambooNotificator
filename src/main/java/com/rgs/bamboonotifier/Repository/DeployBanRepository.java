package com.rgs.bamboonotifier.Repository;

import com.rgs.bamboonotifier.Entity.DeployBan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeployBanRepository  extends CrudRepository<DeployBan, String> {
}
