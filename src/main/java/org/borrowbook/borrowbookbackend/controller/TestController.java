package org.borrowbook.borrowbookbackend.controller;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {
    @GetMapping("/get")
    public String get(@AuthenticationPrincipal User user){
        return user.getUsername();
    }
}
