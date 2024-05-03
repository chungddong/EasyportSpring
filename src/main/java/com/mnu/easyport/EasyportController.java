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

    @Autowired
    private ProfileRepository profileRepository;

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
        Profile profile = new Profile();
        profile.setUserid(user.getUserid());
        profile.setContent("");
        profileRepository.save(profile);
        
        model.addAttribute("name", user.getName());
        return "signup_done";
    }

    //로그인 요청 시
    @PostMapping("/myhome")
    public String loginUser(@RequestParam(name = "userid") String userid,
            @RequestParam(name = "passwd") String passwd, HttpSession session, RedirectAttributes rd) {

        SiteUser user = userRepository.findByUserid(userid);
        if (user != null) {
            if (passwd.equals(user.getPasswd())) {
                session.setAttribute("userid", userid);
                return "redirect:/myhome"; // 로그인 성공시 myhome 으로 이동
            }
        }

        rd.addFlashAttribute("reason", "wrong password");
        return "redirect:/error"; // TODO: 에러 페이지 만들어야함
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

    @GetMapping("/myhome")
    public String showHomePage(HttpSession session, Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        String userid = (String) session.getAttribute("userid");
        if (userid == null) {
            return "redirect:/"; //로그인 정보가 없으면 로그인 화면으로 이동
        }

        Profile profile = profileRepository.findByUserid(userid);
        model.addAttribute("profile", profile);
        return "myhome";
    }

    @GetMapping("/header")
    public String headerPage(Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "header"; // home.html을 렌더링
    }

    @GetMapping("/editport") //포트폴리오 작성 페이지
    public String showEditPortPage(HttpSession session, Model model) {
        
        Post post = new Post();
        //post.setAuthor(userEmail.toString()); // 작성자 정보를 현재 로그인한 사용자로 설정
        model.addAttribute("post", post);
        return "editport";
    }

    // 글 저장 로직
    @PostMapping("/savePost")
    public String savePost(@ModelAttribute("post") Post post) {
        postRepository.save(post);
        return "redirect:/myhome";
    }

}
