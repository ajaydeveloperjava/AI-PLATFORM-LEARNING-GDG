package chatbot.chatbot.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static class OtpEntry {
        String otp;
        long expiryTime;

        OtpEntry(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    public String generateOTP() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    public boolean sendOtp(String email) {
        String otp = generateOTP();
        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);
        otpStore.put(email, new OtpEntry(otp, expiryTime));

        String subject = "Your OTP Code";
        String content = "<p>Your OTP is: <b>" + otp + "</b></p><p>Expires in 5 minutes.</p>";

        return sendHtmlEmail(email, subject, content);
    }

    public boolean sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("ajaynathiya2005@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateOtp(String email, String enteredOtp) {
        OtpEntry otpEntry = otpStore.get(email);
        if (otpEntry == null) return false;

        if (System.currentTimeMillis() > otpEntry.expiryTime) {
            otpStore.remove(email);
            return false;
        }
        return otpEntry.otp.equals(enteredOtp);
    }

    @Scheduled(fixedRate = 600000)
    public void cleanupExpiredOtps() {
        otpStore.entrySet().removeIf(e -> System.currentTimeMillis() > e.getValue().expiryTime);
    }
}
