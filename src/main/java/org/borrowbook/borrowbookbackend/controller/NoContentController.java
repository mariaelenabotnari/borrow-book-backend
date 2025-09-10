package org.borrowbook.borrowbookbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoContentController {
    @GetMapping("/favicon.ico")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void favicon() {}

    @GetMapping("/.well-known/**")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void wellKnown() {}
}
