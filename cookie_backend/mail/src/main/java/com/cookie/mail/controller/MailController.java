package com.cookie.mail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/mail", produces = { MediaType.APPLICATION_JSON_VALUE })
@RestController
public class MailController {

    @Operation(summary = "Hello")
    @ApiResponse(responseCode = "200", description = "Hello",
            content = { @Content(mediaType = "application/json") })
//    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public String hello() {
        return "hello";
    }
}