package com.example.addressservice.Controller;

import com.example.addressservice.Config.Success;
import com.example.addressservice.Dto.AddressDto;
import com.example.addressservice.Repository.AddressRepo;
import com.example.addressservice.Service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.ILoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
@Slf4j
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


    @GetMapping("/getdata")
    public ResponseEntity<String>getALldata(){
        while(true){

        }
//        return ResponseEntity.ok("Done");
    }


    @GetMapping("/getdataexception")
    public ResponseEntity<String>getdata(){
        throw new NullPointerException("Null");
//        return ResponseEntity.ok("Done");
    }

    @GetMapping("/timeout")
    public ResponseEntity<String> simulateDatabaseLock() {
        log.warn("Chaos trigger invoked: Simulating a 10-second database lock/timeout.");
        try {
            // Freezes the executing request thread for 10 seconds
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Chaos timeout interrupted", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Chaos test interrupted");
        }
        return ResponseEntity.ok("Executed 10-second delay. Check /actuator/metrics/http.server.requests for latency spikes.");
    }
}









