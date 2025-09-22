package org.borrowbook.borrowbookbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/util")
public class UtilController {

    @GetMapping("/health")
    public void health() { }

    @GetMapping("/ping")
    public void ping() { }
}