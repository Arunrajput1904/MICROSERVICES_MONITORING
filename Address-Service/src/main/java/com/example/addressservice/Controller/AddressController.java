package com.example.addressservice.Controller;

import com.example.addressservice.Config.Success;
import com.example.addressservice.Dto.AddressDto;
import com.example.addressservice.Repository.AddressRepo;
import com.example.addressservice.Service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {


    private final AddressService addressService;

    
    @GetMapping("/get/{id}")
    public ResponseEntity<AddressDto> getAddress(@PathVariable long id) {
        AddressDto addressDto=addressService.getAddress(id);
        return new  ResponseEntity<>(addressDto, HttpStatus.OK);
    }

    @PostMapping("/post")
    public ResponseEntity<AddressDto> postAddress(@RequestBody AddressDto addressDto) {

        return new ResponseEntity<>(addressService.addAddress(addressDto), HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<AddressDto>> getAllAddress(){
        List<AddressDto> list=addressService.findalladdress();

        return new ResponseEntity<>(list, HttpStatus.OK);

    }

}
