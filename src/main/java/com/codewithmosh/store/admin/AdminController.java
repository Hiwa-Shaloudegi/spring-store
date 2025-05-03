package com.codewithmosh.store.admin;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello Admin";
    }
}
