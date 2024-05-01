package com.mnu.easyport;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import javax.sql.rowset.serial.SerialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

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

    // 파일 업로드
    @PostMapping(path = "/upload")
    public String upload(@RequestParam MultipartFile file,
            Model model) throws IllegalStateException, IOException {

        if (!file.isEmpty()) {
            String newName = file.getOriginalFilename();
            newName = newName.replace(' ', '_');
            FileDto dto = new FileDto(newName, file.getContentType());
            File upfile = new File(dto.getFileName());
            file.transferTo(upfile);
            model.addAttribute("file", dto);

        }

        return "result";

    }

    

}
