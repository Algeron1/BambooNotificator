package com.rgs.bamboonotifier.Repository;

import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AnnouncementMessageRepository extends ListCrudRepository<AnnouncementMessage, String> {
}
