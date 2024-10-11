package OneToOneChat_practice.chat.chatService;


import OneToOneChat_practice.chat.entity.ChatRoom;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class ChatService {

    //chatRoomMap - Map<고유 식별자, 채팅방의 정보>
    private Map<String, ChatRoom> chatRoomMap;

    //의존 관계 주입 후 초기화하도록 함 - chatRoomMap은 ChatService가 사용되기 전에 초기화
    @PostConstruct
    private void init() {
        chatRoomMap = new LinkedHashMap<>();
    }

    /**
     * 전체 채팅방 조회
     */
    public List<ChatRoom> findAllRoom() {

        //chatRoomMap.values() -> 현재 저장된 모든 ChatRoom 객체의 값을 가져옴
        List<ChatRoom> rooms = new ArrayList<>(chatRoomMap.values());

        //최신순으로 정렬
        Collections.reverse(rooms);

        return rooms;
    }

    /**
     * roomId -> 채팅방 조회
     */
    public ChatRoom findRoomById(String roomId) {
        return chatRoomMap.get(roomId);
    }

    /**
     * 채팅방 userName 조회
     */
    public String getUserName(String roomId, String userId) {
        ChatRoom room = chatRoomMap.get(roomId);
        return room.getUserList().get(userId);
    }

    /**
     * 채팅방 userList 조회
     */
    public ArrayList<String> getUserList(String roomId) {

        ArrayList<String> userList = new ArrayList<>();

        ChatRoom room = chatRoomMap.get(roomId);

        //ex. key - userId, value - username
        room.getUserList().forEach((key, value) -> userList.add(value));

        return userList;
    }


    /**
     * roomName으로 채팅방 생성
     */
    public ChatRoom createChatRoom(String roomName) {
        ChatRoom room = new ChatRoom(roomName);

        chatRoomMap.put(room.getRoomId(), room);

        return room;
    }

    /**
     * 채팅방 유저 리스트에 유저 추가
     */
    public String addUser(String roomId, String userName) {
        ChatRoom room = chatRoomMap.get(roomId);
        String userUUID = UUID.randomUUID().toString();

        room.getUserList().put(userUUID, userName);
        return userUUID;
    }

    /**
     * 채팅방 인원 + 1
     */
    public void plusUserCnt(String roomId) {

        ChatRoom room = chatRoomMap.get(roomId);
        if (room != null) {
            room.setUserCount(room.getUserCount() + 1);
        }
    }

    /**
     * 채팅방 인원 -1
     */
    public void minusUserCnt(String roomId) {

        ChatRoom room = chatRoomMap.get(roomId);
        if (room != null) {
            if (room.getUserCount() > 0) {
                room.setUserCount(room.getUserCount() - 1);
            }
        }
    }

    /**
     * 채팅방 이름 중복 확인
     */
    public String generateUniqueUsername(String roomId, String username) {
        ChatRoom room = chatRoomMap.get(roomId);
        String uniqueUserName = username;

        while (room.getUserList().containsValue(uniqueUserName)) {
            int ranNum = (int) (Math.random() * 100) + 1;
            uniqueUserName = username + ranNum;
        }

        return uniqueUserName;
    }

    /**
     * 채팅방 유저 리스트 삭제
     */
    public void deleteUserList(String roomId, String userUUID) {
        ChatRoom room = chatRoomMap.get(roomId);
        room.getUserList().remove(userUUID);
    }


}
