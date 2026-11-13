package com.rotafolego.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Rota simples para conferir se a API esta no ar (GET /). */
@RestController
public class StatusController {

    @GetMapping("/")
    public Map<String, String> status() {
        return Map.of("api", "Rota do Folego", "status", "no ar");
    }
}
