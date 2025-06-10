package com.rgs.bamboonotifier.interfaces;

import org.springframework.http.ResponseEntity;

public interface ImessageSender<T> {

    ResponseEntity<T> sendMessage(String message, String messageId);

}
