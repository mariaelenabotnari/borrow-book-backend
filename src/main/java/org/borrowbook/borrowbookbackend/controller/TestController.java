package org.borrowbook.borrowbookbackend.controller;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {
    @GetMapping("/get")
    public String get(@AuthenticationPrincipal User user){
        return user.getUsername();
    }

    @PostMapping("/post")
    public String post(@AuthenticationPrincipal User user){ return user.getUsername();}
}
