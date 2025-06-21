package com.convofy.convofy.utils;

import com.convofy.convofy.Entity.OtpStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    // Generate 6-digit OTP
    public String generateOTP() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    // Send OTP to Email and store it
    public void sendOtp(String email) {
        String otp = generateOTP();
        OtpStore.otpMap.put(email, otp); // Store OTP
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);
    }

    // Validate the OTP
    public boolean validateOtp(String email, String otp) {
        String storedOtp = OtpStore.otpMap.get(email);
        return otp.equals(storedOtp);
    }
}

