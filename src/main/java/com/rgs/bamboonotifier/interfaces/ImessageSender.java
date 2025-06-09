package com.rgs.bamboonotifier.interfaces;

import org.springframework.http.HttpEntity;

public interface ImessageSender {

    void sendMessage(String message);

}
