package com.rgs.bamboonotifier.Rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }
}