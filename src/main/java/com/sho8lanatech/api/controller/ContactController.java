package com.sho8lanatech.api.controller;

import com.sho8lanatech.api.dto.ContactRequest;
import com.sho8lanatech.api.service.EmailService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    private final EmailService emailService;

    public ContactController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    public void send(@RequestBody ContactRequest request) {
        emailService.sendEmail(request);
    }
}
