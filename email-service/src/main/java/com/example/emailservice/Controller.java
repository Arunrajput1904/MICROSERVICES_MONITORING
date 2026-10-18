package com.example.emailservice;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Consumer;

@RestController
@RequestMapping("/email")
@Configuration
public class Controller {


    @Bean
    public Consumer<String> consumer(){
  return      s-> System.out.println(s);
    }


    @Bean
    public Consumer<Message<String>> message(){
        return s-> System.out.println(s.getPayload()+ " send email ................................................");
    }


    @Bean
    public Consumer<Message<String>> emailConsumer(){
        return s-> System.out.println(s.getPayload()+ " send email ................................................");
    }







}
