package server.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeckGeneratorController {

    @RequestMapping("/generate")
    public String index() {
        return "todo";
    }

}