package chatbot.chatbot.controller;

import chatbot.chatbot.service.ChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // ===============================
    // HOME PAGE (FIXES WHITELABEL)
    // ===============================
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String sessionId = session.getId();

        model.addAttribute("currentChat",
                chatService.getChatHistory(sessionId));
        model.addAttribute("showHistory", false);

        return "chat"; // chat.html
    }

    // ===============================
    // CHAT PAGE
    // ===============================
    @GetMapping("/chat")
    public String chatPage(HttpSession session, Model model) {
        return home(session, model);
    }

    // ===============================
    // SEND MESSAGE (FORM SUBMIT)
    // ===============================
    @PostMapping("/chat")
    public String chat(@RequestParam String message,
                       HttpSession session,
                       Model model) {

        String sessionId = session.getId();
        chatService.chat(sessionId, message);

        model.addAttribute("currentChat",
                chatService.getChatHistory(sessionId));
        model.addAttribute("showHistory", false);

        return "chat";
    }

    // ===============================
    // CHAT VIA AJAX
    // ===============================
    @PostMapping("/chat-ajax")
    @ResponseBody
    public String chatAjax(@RequestParam String message,
                           HttpSession session) {

        String sessionId = session.getId();
        return chatService.chat(sessionId, message);
    }

    // ===============================
    // VIEW HISTORY
    // ===============================
    @GetMapping("/history")
    public String viewHistory(HttpSession session, Model model) {

        String sessionId = session.getId();

        model.addAttribute("currentChat",
                chatService.getChatHistory(sessionId));
        model.addAttribute("history",
                chatService.getChatHistory(sessionId));
        model.addAttribute("showHistory", true);

        return "chat";
    }

    // ===============================
    // CLEAR CHAT HISTORY
    // ===============================
    @PostMapping("/clear")
    public String clearHistory(HttpSession session) {

        String sessionId = session.getId();
        chatService.clearHistory(sessionId);

        return "redirect:/"; // WORKS NOW
    }

    // ===============================
    // FILE UPLOAD
    // ===============================
    @PostMapping("/upload-file")
    @ResponseBody
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             HttpSession session) throws Exception {

        String sessionId = session.getId();
        return chatService.handleFileUpload(sessionId, file);
    }

    // ===============================
    // IMAGE UPLOAD
    // ===============================
    @PostMapping("/upload-image")
    @ResponseBody
    public String uploadImage(@RequestParam("image") MultipartFile image,
                              HttpSession session) throws Exception {

        String sessionId = session.getId();
        return chatService.handleImageUpload(sessionId, image);
    }
}
