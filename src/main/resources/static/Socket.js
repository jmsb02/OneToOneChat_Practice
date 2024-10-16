'use strict';

document.write("<script src=\"https://code.jquery.com/jquery-3.6.1.min.js\" integrity=\"sha256-o88AwQnZB+VDvE9tvIXrMQaPlFFSUTR+nldQm1LuPXQ=\" crossorigin=\"anonymous\"></script>");

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

const url = new URL(location.href).searchParams;
const roomId = url.get('roomId');

/**
 * 사용자가 입력한 사용자의 이름을 받아 WebSocket 서버에 연결
 */
function connect(event) {
    username = document.querySelector('#name').value.trim();

    // username 중복 확인 후 연결
    isDuplicateName().then(isUnique => {
        if (isUnique) {
            usernamePage.classList.add('hidden');
            chatPage.classList.remove('hidden');

            //연결하고자 하는 Socket의 endPoint
            var socket = new SockJS('/ws-stomp');
            stompClient = Stomp.over(socket);

            stompClient.connect({}, onConnected, onError);
        } else {
            alert("사용자 이름이 중복되었습니다. 다른 이름을 입력하세요.");
        }
    });

    event.preventDefault();
}

/**
 * WebSocket 연결이 성공적으로 이루어졌을 때 호출되는 메서드
 */
function onConnected() {
    stompClient.subscribe('/sub/chat/room/' + roomId, onMessageReceived);
    stompClient.send("/pub/chat/enterUser", {}, JSON.stringify({
        "roomId": roomId,
        sender: username,
        type: 'ENTER'
    }));
    connectingElement.classList.add('hidden');
}

/**
 * 입력된 사용자의 이름이 이미 사용중인지 확인
 */
function isDuplicateName() {
    return new Promise((resolve) => {
        $.ajax({
            type: "GET",
            url: "/chat/duplicateName",
            data: {
                "username": username,
                "roomId": roomId
            },
            success: function (data) {
                username = data; // 중복 확인 후 username 업데이트
                resolve(true); // 중복이 아닐 경우
            },
            error: function () {
                resolve(false); // 중복인 경우
            }
        });
    });
}

/**
 * 현재 채팅방에 있는 사용자 목록을 가져오는 메서드
 */
function getUserList() {
    const $list = $("#list");

    $.ajax({
        type: "GET",
        url: "/chat/userlist",
        data: {
            "roomId": roomId
        },
        success: function (data) {
            var users = "";
            for (let i = 0; i < data.length; i++) {
                users += "<li class='dropdown-item'>" + data[i] + "</li>";
            }
            $list.html(users);
        },
        error: function () {
            console.error("유저 리스트를 가져오는 데 실패했습니다.");
        }
    });
}

/**
 * WebSocket 연결이 실패했을 때 호출되는 메서드
 */
function onError(error) {
    connectingElement.textContent = 'WebSocket 서버에 연결할 수 없습니다. 페이지를 새로 고쳐 다시 시도하세요!';
    connectingElement.style.color = 'red';
}

/**
 * 사용자가 입력한 메세지를 채팅방에 전송하는 메서드
 */
function sendMessage(event) {
    var messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            "roomId": roomId,
            sender: username,
            message: messageContent,
            type: 'TALK'
        };

        stompClient.send("/pub/chat/sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
        // 메시지 전송 알림 (예: "메시지가 전송되었습니다.")
    }
    event.preventDefault();
}

/**
 * 서버에서 수신한 메시지를 처리하는 메서드
 */
function onMessageReceived(payload) {
    var chat = JSON.parse(payload.body);
    var messageElement = document.createElement('li');

    if (chat.type === 'ENTER' || chat.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        chat.content = chat.sender + chat.message;
        getUserList();

    } else { //chat.type === 'TALK'
        messageElement.classList.add('chat-message');
        var avatarElement = document.createElement('i');
        avatarElement.appendChild(document.createTextNode(chat.sender[0]));
        avatarElement.style['background-color'] = getAvatarColor(chat.sender);

        messageElement.appendChild(avatarElement);
        var usernameElement = document.createElement('span');
        usernameElement.appendChild(document.createTextNode(chat.sender));
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    textElement.appendChild(document.createTextNode(chat.message));
    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

/**
 * 메시지를 보낸 사용자의 이름에 따라 아바타 색상을 결정하는 메서드
 */
function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
