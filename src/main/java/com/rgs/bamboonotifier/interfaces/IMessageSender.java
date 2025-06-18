package com.rgs.bamboonotifier.interfaces;

import org.springframework.http.ResponseEntity;

public interface IMessageSender<T> {

    ResponseEntity<T> sendMessage(String message, String messageId);
}
