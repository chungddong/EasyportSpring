package com.mnu.easyport;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class EasyportController {

    @Autowired
    private SiteUserRepository userRepository;

    @Autowired
    private AzureBlobService azureBlobService;

    @GetMapping(path = "/")
    public String mains(Model model) {
        model.addAttribute("siteuser", new SiteUser());
        return "index";

    }

    @PostMapping(path = "/signup")
    public String signup(@ModelAttribute SiteUser user, Model model) {

        userRepository.save(user);
        model.addAttribute("name", user.getName());
        return "signup_done";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam(name = "email") String email, 
        @RequestParam(name = "passwd") String passwd, HttpSession session, RedirectAttributes rd) {
        
        SiteUser user = userRepository.findByEmail(email);
        if(user != null) {
            if(passwd.equals(user.getPasswd())) {
                session.setAttribute("email", email);
                return "myroom"; //로그인 성공시 myroom 으로 이동
            }
        }

        rd.addFlashAttribute("reason", "wrong password");
        return "redirect:/error";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "index";
    }



    
    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadBlob(@RequestParam("container") String containerName,
            @RequestParam("file") MultipartFile file) throws IOException {
        String blobName = file.getOriginalFilename();
        azureBlobService.uploadBlobFromFile(containerName, blobName, file.getInputStream());
        return "redirect:/upload?success";
    }

    

}
