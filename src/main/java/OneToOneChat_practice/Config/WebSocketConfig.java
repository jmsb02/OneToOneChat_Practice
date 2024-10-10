package OneToOneChat_practice.Config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


/**
 * WebSocket 연결을 위한 STOMP 엔드포인트 등록, 클라이언트는 이 엔드포인트를 통해 WebSocket 서버에 연결할 수 있음
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * addEndpoint("/ws") - 클라이언트가 WebSocket 서버에 연결할 수 있는 엔드포인트 정의
     * setAllowedOrigins("*") - CORS 설정 정의, 모든 출처에서의 연결 요청 허용
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*");
    }

    /**
     * 메세지를 처리하고 전달하는 메세지 브로커 설정
     * enableSimpleBroker("/sub")는 클라이언트가 구독할 수 있는 경로를 정의
     * setApplicationDestinationPrefixes("/pub")는 클라이언트가 메시지를 발행할 때 사용할 경로의 접두사를 설정
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }
}
