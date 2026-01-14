package chatbot.chatbot.service;

import chatbot.chatbot.model.ChatHistory;
import chatbot.chatbot.repo.ChatHistoryRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatHistoryRepository repository;
    private final WebClient webClient;

    public ChatService(ChatHistoryRepository repository) {
        this.repository = repository;
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // ===============================
    // MAIN CHAT METHOD
    // ===============================
    public String chat(String sessionId, String userMessage) {

        // 1️⃣ Save user message
        saveMessage(sessionId, "USER", userMessage);

        // 2️⃣ Load previous conversation
        List<ChatHistory> history =
                repository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        // 3️⃣ Build conversation prompt
        String conversation = history.stream()
                .map(h -> h.getRole() + ": " + h.getMessage())
                .collect(Collectors.joining("\n"));

        String finalPrompt =
                "Answer in very simple English.\n" +
                        "Use easy words.\n" +
                        "Give answer only in bullet points.\n\n" +
                        conversation;

        // 4️⃣ Send to Ollama
        Map<String, Object> body = Map.of(
                "model", "gemma:2b",
                "prompt", finalPrompt,
                "stream", false
        );

        Map response = webClient.post()
                .uri("/api/generate")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String botReply = response.get("response").toString();

        // 5️⃣ Save bot reply
        saveMessage(sessionId, "BOT", botReply);

        return botReply;
    }

    // ===============================
    // SAVE MESSAGE
    // ===============================
    private void saveMessage(String sessionId, String role, String message) {
        ChatHistory chat = new ChatHistory();
        chat.setSessionId(sessionId);
        chat.setRole(role);
        chat.setMessage(message);
        chat.setCreatedAt(LocalDateTime.now());
        repository.save(chat);
    }

    // ===============================
    // GET CHAT HISTORY
    // ===============================
    public List<ChatHistory> getChatHistory(String sessionId) {
        return repository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    // ===============================
    // CLEAR CHAT HISTORY
    // ===============================
    public void clearHistory(String sessionId) {
        List<ChatHistory> chats =
                repository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        repository.deleteAll(chats);
    }
    // FILE UPLOAD (TXT / PDF BASIC)
    public String handleFileUpload(String sessionId, MultipartFile file) throws Exception {

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        String prompt =
                "Summarize the following content in simple English.\n" +
                        "Give points only.\n\n" +
                        content;

        String summary = askOllama(prompt);

        saveMessage(sessionId, "USER", "Uploaded file: " + file.getOriginalFilename());
        saveMessage(sessionId, "BOT", summary);

        return summary;
    }

    // IMAGE UPLOAD (BASIC EXPLANATION)
    public String handleImageUpload(String sessionId, MultipartFile image) {

        String prompt =
                "Explain what this image might contain in simple English.\n" +
                        "Give points only.";

        String explanation = askOllama(prompt);

        saveMessage(sessionId, "USER", "Uploaded image: " + image.getOriginalFilename());
        saveMessage(sessionId, "BOT", explanation);

        return explanation;
    }

    // COMMON OLLAMA CALL
    private String askOllama(String prompt) {

        Map<String, Object> body = Map.of(
                "model", "gemma:2b",
                "prompt", prompt,
                "stream", false
        );

        Map response = webClient.post()
                .uri("/api/generate")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return response.get("response").toString();
    }

}
