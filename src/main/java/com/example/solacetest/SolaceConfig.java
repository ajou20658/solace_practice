package com.example.solacetest;

import com.solacesystems.jcsmp.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolaceConfig {
    @Value("${spring.solaceUsername}")
    private String username;
    @Value("${spring.solacePassword}")
    private String password;
    @Value("${spring.solaceHost}")
    private String host;
    @Value("${spring.queueName}")
    private String queueName;
    @Value("${spring.vpnName}")
    private String vpn;
    @Bean
    public JCSMPSession session() throws JCSMPException {
        JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST,host);
        properties.setProperty(JCSMPProperties.USERNAME,username);
        properties.setProperty(JCSMPProperties.PASSWORD,password);
        properties.setProperty(JCSMPProperties.VPN_NAME,vpn);
        JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();
        System.out.println("Session Connected : "+session);
        if(session.isCapable(CapabilityType.PUB_GUARANTEED)&&
                session.isCapable(CapabilityType.SUB_FLOW_GUARANTEED)&&
                session.isCapable(CapabilityType.ENDPOINT_MANAGEMENT)&&
                session.isCapable(CapabilityType.QUEUE_SUBSCRIPTIONS)){
            System.out.println("All required capabilites supported!");
        }else {
            System.out.println("Capabilities not met!");
            System.exit(1);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(session::closeSession));
        return session;
    }
    @Bean Queue queue(){
        return JCSMPFactory.onlyInstance().createQueue(queueName);
    }
    @Bean
    public XMLMessageProducer producer(JCSMPStreamingPublishCorrelatingEventHandler eventHandler) throws JCSMPException {
        return session().getMessageProducer(eventHandler);
    }
    @Bean
    public JCSMPStreamingPublishCorrelatingEventHandler eventHandler(){
        return new JCSMPStreamingPublishCorrelatingEventHandler() {
            @Override
            public void responseReceivedEx(Object o) {
                System.out.println("Producer received response for msg: " + o);
            }

            @Override
            public void handleErrorEx(Object o, JCSMPException e, long l) {
                System.out.printf("Producer received error for msg: %s@%s - %s%n", o, l, e);
            }
        };
    }
    @Bean
    public XMLMessageListener listener() {
        return new XMLMessageListener(){
            @Override
            public void onReceive(BytesXMLMessage bytesXMLMessage){
                if(bytesXMLMessage instanceof TextMessage textMessage){
                    System.out.println("Received message : "+ textMessage.getText());
                }
            }

            @Override
            public void onException(JCSMPException e) {
                System.out.println(e.getExtraInfo());
            }
        };
    }

    @Bean
    public Consumer consumer(XMLMessageListener listener, Queue queue, JCSMPSession session) throws JCSMPException {
        ConsumerFlowProperties flowProperties = new ConsumerFlowProperties();
        flowProperties.setEndpoint(queue);
//        XMLMessageConsumer consumer = session().getMessageConsumer(listener);
        Consumer consumer = session.createFlow(listener,flowProperties);
        consumer.start();
        return consumer;
    }
    @Bean
    public DisposableBean shutdownHook(XMLMessageProducer producer, Consumer consumer,JCSMPSession session){
        return () -> {
            if( producer!=null){
                producer.close();
                System.out.println("Producer disconnected successfully.");
            }
            if (consumer!=null){
                consumer.close();
                System.out.println("Consumer disconnected successfully.");
            }
            if (session!=null){
                session.closeSession();
                System.out.println("Session disconnected successfully.");
            }
        };
    }

    @Bean
    public TextMessage textMessage(){
        TextMessage msg =  JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        msg.setDeliveryMode(DeliveryMode.PERSISTENT);
        return msg;
    }

    @Bean
    public Topic topic(){
        Topic topic = JCSMPFactory.onlyInstance().createTopic("gwangbu/a/b/b");
        return topic;
    }
}
