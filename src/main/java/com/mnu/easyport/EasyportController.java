package com.mnu.easyport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EasyportController {

    @Autowired
    private SiteUserRepository userRepository;

    @GetMapping(path = "/")
    public String mains(Model model) {
        model.addAttribute("siteuser", new SiteUser());
        return "index";

    }

    @PostMapping(path = "/")
    public String signup(@ModelAttribute SiteUser user, Model model) {

        userRepository.save(user);
        model.addAttribute("name", user.getName());
        return "signup_done";
    }

}
