package com.mnu.easyport;

import java.io.IOException;
import java.util.List;

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
    private PostRepository postRepository;

    @Autowired
    private AzureBlobService azureBlobService;

    @GetMapping(path = "/")
    public String mains(Model model) {
        model.addAttribute("siteuser", new SiteUser());
        return "index";

    }

    // 로그인, 회원가입 관련
    // ===============================================================================================================================

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
        if (user != null) {
            if (passwd.equals(user.getPasswd())) {
                session.setAttribute("email", email);
                return "home"; // 로그인 성공시 home 으로 이동
            }
        }

        rd.addFlashAttribute("reason", "wrong password");
        return "redirect:/error"; // TODO: 에러 페이지 만들어야함
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

    // ===============================================================================================================================

    // 파일 업로드 관련 [이미지]
    // ===============================================================================================================================

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

    @GetMapping("/home")
    public String showHomePage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/"; //로그인 정보가 없으면 로그인 화면으로 이동
        }
        // 데이터 가져오기 및 모델에 추가
        model.addAttribute("posts", postRepository.findAll());
        return "home";
    }

    @GetMapping("/header")
    public String headerPage(Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "header"; // home.html을 렌더링
    }

    @GetMapping("/editport")
    public String showEditPortPage(Model model) {
        model.addAttribute("post", new Post());
        return "editport";
    }

    // 글 저장 로직
    @PostMapping("/savePost")
    public String savePost(@ModelAttribute("post") Post post) {
        postRepository.save(post);
        return "redirect:/home";
    }

}
