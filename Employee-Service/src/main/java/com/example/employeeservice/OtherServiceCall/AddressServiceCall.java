package com.example.employeeservice.OtherServiceCall;


import com.example.employeeservice.Dto.AddressDto;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "address-service",path = "/address")
public interface AddressServiceCall {

    @GetMapping("/get/{id}")
    public ResponseEntity<AddressDto> getAddress(@PathVariable long id);

    @PostMapping("/post")
    public ResponseEntity<AddressDto> postAddress(@RequestBody AddressDto addressDto);

    @GetMapping("/getAll")
    public ResponseEntity<List<AddressDto>> getAllAddress();
}
