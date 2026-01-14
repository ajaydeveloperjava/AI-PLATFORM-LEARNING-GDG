package chatbot.chatbot.service;

import chatbot.chatbot.model.User;
import chatbot.chatbot.repo.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.findByEmail(email) != null;
    }

    public void signup(User user) {
        userRepository.save(user);
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return "Login successful";
        }
        return "Invalid email or password";
    }

    // ➕ added for profile
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
