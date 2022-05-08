package io.github.sgbasaraner.cdevaldemo;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EvalController {
    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @GetMapping("/index")
    public String showUserList(Model model) {
        return "index";
    }
}
