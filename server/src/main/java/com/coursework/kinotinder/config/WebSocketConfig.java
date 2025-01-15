// WebSocketConfig.java
package com.coursework.kinotinder.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        logger.info("Registering STOMP endpoint /ws");
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000","http://localhost:8080", "https://ef-considerable-excellence-consoles.trycloudflare.com");
                // .withSockJS();
    }


     @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
         logger.info("Configuring WebSocket transport");
        registration.setMessageSizeLimit(1024 * 1024);
        registration.setSendTimeLimit(10 * 1000);
       registration.setSendBufferSizeLimit(1024 * 1024);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        logger.info("Configuring message broker");
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }
}