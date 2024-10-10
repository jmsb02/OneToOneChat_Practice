package OneToOneChat_practice.chat.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.UUID;

/**
 * Stomp를 통해 pub/sub를 사용하면 구독자 관리가 알아서 됨
 * 따라서 세션 관리 코드 필요 x, 메세지를 보내는 로직 작성도 하지 않아도 된다!
 */

@Getter
public class ChatRoom {
    private String roomId;
    private String roomName;
    private Long userCount;

    private HashMap<String, String> userList = new HashMap<String, String>(); //ex. userId, username 형식

    public ChatRoom create(String roomName) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = UUID.randomUUID().toString();
        chatRoom.roomName = roomName;

        return chatRoom;
    }
}
