package OneToOneChat_practice.chat.controller;

import OneToOneChat_practice.chat.dto.ChatDto;
import OneToOneChat_practice.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final SimpMessagingTemplate template;

    private final ChatService service;

    @MessageMapping("/chat/enterUser") //클라이언트가 이 URL로 요청
    //@Payload - 클라이언트가 보낸 메세지를 메서드의 매개변수로 매핑, SimpMessageHeaderAccessor - WebSocket 메시지의 헤더 정보를 접근
    public void enterUser(@Payload ChatDto chat, SimpMessageHeaderAccessor headerAccessor) {

        //채팅방 유저 + 1
        service.plusUserCnt(chat.getRoomId());

        //채팅방에 유저 추가 및 UUID 반환
        String userUUID = service.addUser(chat.getRoomId(), chat.getSender());

        //WebSocket session에 사용자 UUID와 채팅방 ID 저장
        headerAccessor.getSessionAttributes().put("userUUID", userUUID);
        headerAccessor.getSessionAttributes().put("roomId", chat.getRoomId());

        chat.setMessage(chat.getMessage() + "님 입장.");

        //convertAndSend()를 통해 지정된 URL에다가 chat 내용을 전송한다.
        template.convertAndSend("/sub/chat/room/" + chat.getRoomId(), chat);
    }

    //클라이언트로부터 받은 채팅 메시지를 처리하고 해당 방에 전송
    @MessageMapping("/chat/sendMessage")
    public void sendMessage(@Payload ChatDto chat) {
        log.info("Chat {}", chat);
        chat.setMessage(chat.getMessage());
        template.convertAndSend("/sub/chat/room/" + chat.getRoomId(), chat);
    }

    //유저 퇴장 시 EventListener 을 통해서 유저 퇴장을 확인
    @EventListener //특정 이벤트 발생했을 때 메서드 호출 (세션 종료 이벤트 리스닝)
    public void webSocketDisconnectListener(SessionDisconnectEvent event) {
        log.info("DisConnEvent {}", event);

        //StompHeaderAccessor를 사용하여 WebSocket 헤더와 세션 속성에 접근
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        //stomp 세션에 있던 uuid와 roomId를 확인해서 채팅방 유저 리스트와 room에서 해당 유저를 삭제
        String userUUID = (String) headerAccessor.getSessionAttributes().get("userUUID");
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");

        log.info("headAccessor {}", headerAccessor);

        //채팅방 유저 -1
        service.minusUserCnt(roomId);

        //채팅방 유저 리스트에서 UUID 유저 닉네임 조회 및 리스트에서 유저 삭제
        String userName = service.getUserName(roomId, userUUID);
        service.deleteUser(roomId, userUUID);

        if (userName != null) {
            log.info("User Disconnected :" + userName);

            //ChatDto 객체를 설정하여 퇴장 메세지 설정
            //MessageType.LEAVE를 통해 메시지 타입을 설정하고, 퇴장한 유저의 이름과 함께 메시지를 생성.
            ChatDto chat = ChatDto.builder()
                    .type(ChatDto.MessageType.LEAVE)
                    .sender(userName)
                    .message(userName + " 님 입장 !!")
                    .build();

            //생성한 퇴장 메세지를 해당 채팅방을 구독중인 모든 클라이언트들에게 전송
            template.convertAndSend("/sub/chat/room/" + roomId, chat);
        }

    }

    //채팅방에 참여한 유저 리스트 반환
    @GetMapping("/chat/userlist")
    @ResponseBody //메서드 반환값이 HTTP 응답 바디에 직접 작성됨 즉, 메서드가 반환하는 ArrayList<String>이 JSON 형식으로 변환되어 클라이언트게 전달 됨
    public ArrayList<String> userList(String roomId) {
        return service.getUserList(roomId);
    }

    //채팅방에 참여한 유저 닉네임 중복 확인
    @GetMapping("/chat/duplicateName")
    @ResponseBody
    public String isDuplicateName(@RequestParam("roomId") String roomId,
                                  @RequestParam("username") String username) {

        //유저 이름 확인
        String userName = service.generateUniqueUsername(roomId, username);
        log.info("유저 이름을 중복확인합니다. 유저 이름 {}", userName);

        return userName;
    }

}
