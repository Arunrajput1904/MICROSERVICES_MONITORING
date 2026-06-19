package com.example.emailservice;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class Controller {



    @KafkaListener(topics = "new-user-email1",groupId = "email1-service")
    public String sendEmail1(EmailObject emailObject) {
        System.out.println(" email is send by one user+.....................................................");
        return " Email sent to "+emailObject.getName();

    }





}
