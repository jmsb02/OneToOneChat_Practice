package OneToOneChat_practice.chat.controller;

import OneToOneChat_practice.chat.entity.ChatRoom;
import OneToOneChat_practice.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 전반적인 채팅방 조회, 생성, 입장 관리하는 Controller
 */
@RestController
@Slf4j
public class ChatRoomController {

    @Autowired
    private ChatService chatService;

    /**
     * 모든 채팅방 조회
     * "/"로 들어올시 모든 채팅방 정보 반환
     */
    @GetMapping("/")
    public String goChatRoom(Model model) {
        model.addAttribute("list", chatService.findAllRoom());
        log.info("모든 채팅방을 가져옵니다 : {}", chatService.findAllRoom());
        return "roomlist";
    }

    /**
     * 채팅방 생성
     * 채팅방 생성 완료 후 redirect
     */
    @PostMapping("/chat/creatroom")
    public String createRoom(@RequestParam String name, RedirectAttributes rttr) {
        ChatRoom room = chatService.createChatRoom(name);

        log.info("채팅방을 생성합니다 채팅방 : {}", room);

        //room을 roomName 이름으로 플래시 속성에 추가(생성된 채팅방 정보를 다음 요청에서 사용할 수 있도록 임시로 저장)
        rttr.addFlashAttribute("roomName", room);

        return "redirect:/";
    }

    /**
     * 채팅방 입장
     * 넘어오는 roomId로 채팅방을 찾아서 클라이언트를 그 room에 보내줌
     */
    @GetMapping("/chat/room/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {

        log.info("roodId = {}", roomId);

        model.addAttribute("room", chatService.findRoomById(roomId));

        return "chatroom";
    }


}
