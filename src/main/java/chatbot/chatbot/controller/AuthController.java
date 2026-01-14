// ======================= AuthController.java (FULLY UPDATED) =======================
package chatbot.chatbot.controller;

import chatbot.chatbot.dto.LoginRequest;
import chatbot.chatbot.model.User;
import chatbot.chatbot.service.EmailService;
import chatbot.chatbot.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    /* ================= SIGNUP ================= */

    @GetMapping("/signup")
    public String showSignupPage(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute User user,
                         Model model,
                         HttpSession session) {

        if (userService.isEmailRegistered(user.getEmail())) {
            model.addAttribute("error", "Email is already registered.");
            return "signup";
        }

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            model.addAttribute("error", "Password and Confirm Password do not match.");
            return "signup";
        }

        userService.signup(user);

        emailService.sendOtp(user.getEmail());
        session.setAttribute("otpEmail", user.getEmail());

        // ✅ MUST REDIRECT
        return "redirect:/auth/ver";
    }

    /* ================= OTP ================= */

    @GetMapping("/ver")
    public String otpPage(HttpSession session, Model model) {

        String email = (String) session.getAttribute("otpEmail");
        if (email == null) {
            return "redirect:/auth/signup";
        }

        model.addAttribute("email", email);
        return "verifyOtp";
    }

    @PostMapping("/ver")
    public String verifyOtp(@RequestParam("otp") String otp,
                            HttpSession session,
                            Model model) {

        String email = (String) session.getAttribute("otpEmail");

        if (email == null) {
            return "redirect:/auth/signup";
        }

        if (emailService.validateOtp(email, otp)) {
            session.removeAttribute("otpEmail");
            // ✅ REDIRECT AFTER OTP SUCCESS
            return "redirect:/auth/login";
        }

        model.addAttribute("email", email);
        model.addAttribute("message", "Invalid OTP");
        return "verifyOtp";
    }

    /* ================= LOGIN ================= */

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest,
                        HttpSession session,
                        Model model) {

        String result = userService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        if ("Login successful".equals(result)) {

            // ✅ STORE LOGIN SESSION
            session.setAttribute("loggedInUser", loginRequest.getEmail());

            // ✅ ALWAYS REDIRECT
            return "redirect:/auth/home";
        }

        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    /* ================= HOME ================= */

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {

        String email = (String) session.getAttribute("loggedInUser");

        if (email == null) {
            return "redirect:/auth/login";
        }

        User user = userService.getUserByEmail(email);
        if (user == null) {
            session.invalidate();
            return "redirect:/auth/login";
        }

        model.addAttribute("user", user);
        return "home";
    }

    /* ================= PROFILE ================= */

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {

        String email = (String) session.getAttribute("loggedInUser");

        if (email == null) {
            return "redirect:/auth/login";
        }

        User user = userService.getUserByEmail(email);
        if (user == null) {
            session.invalidate();
            return "redirect:/auth/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }

    /* ================= LOGOUT ================= */

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
