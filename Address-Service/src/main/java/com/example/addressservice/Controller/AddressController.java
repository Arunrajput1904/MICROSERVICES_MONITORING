package com.example.addressservice.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/address")
public  class AddressController{


    @GetMapping("/getdataexception")
    ResponseEntity<String> getData(){
        throw new NullPointerException("Null error exception");
    }


}