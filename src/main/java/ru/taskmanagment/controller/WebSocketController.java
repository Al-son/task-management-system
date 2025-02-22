package ru.taskmanagment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.taskmanagment.service.ValidateFileService;


@RequiredArgsConstructor
@RestController
public class WebSocketController extends TextWebSocketHandler {
    private final ValidateFileService validateFileService;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String response = "Message reçu: " + message.getPayload();
        session.sendMessage(new TextMessage(response));
    }

//    public void startValidationProcess(WebSocketSession session, MultipartFile file) throws Exception {
//        List<String> logs = validateFileService.validateZipFile(file);
//        for (String log : logs) {
//            session.sendMessage(new TextMessage(log));
//        }
//    }
}
