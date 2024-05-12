package com.example.solacetest;

import com.solacesystems.jcsmp.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Controller {
    private final XMLMessageProducer producer;
    private final Topic topic;
    @GetMapping("/hello")
    public void sendHello(String message) throws JCSMPException {

        TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        msg.setDeliveryMode(DeliveryMode.PERSISTENT);
        msg.setText(message);
        producer.send(msg,topic);
    }
}
