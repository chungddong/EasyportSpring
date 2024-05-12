package com.mnu.easyport;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import org.apache.el.stream.Optional;
import org.hibernate.mapping.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.persistence.Entity;
import jakarta.servlet.http.HttpSession;

import java.util.Random;

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

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @GetMapping(path = "/")
    public String mains(Model model) {
        model.addAttribute("siteuser", new SiteUser());
        return "index";

    }

    // 로그인, 회원가입 관련
    // ===============================================================================================================================

    @PostMapping(path = "/signup")
    public String signup(@ModelAttribute("siteuser") SiteUser user, Model model) {

        userRepository.save(user);

        Profile profile = new Profile();
        profile.setUserid(user.getUserid());
        profile.setEmail(user.getEmail());
        profile.setIntroduce("");
        profile.setCanview(false);

        String rankey = generateRandomKey();
        profile.setViewkey(rankey);
        profileRepository.save(profile);

        String userName = user.getName();

        azureBlobService.createContainer(userName);

        model.addAttribute("name", userName);
        return "signup_done";
    }

    // 랜덤키 생성 코드
    public static String generateRandomKey() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    // 로그인 요청 시
    @PostMapping("/myboard")
    public String loginUser(@RequestParam(name = "userid") String userid,
            @RequestParam(name = "passwd") String passwd, HttpSession session, RedirectAttributes rd) {

        SiteUser user = userRepository.findByUserid(userid);
        if (user != null) {
            if (passwd.equals(user.getPasswd())) {
                session.setAttribute("userid", userid);
                return "redirect:/myboard"; // 로그인 성공시 myboard 으로 이동
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

    @PostMapping("/uploadProfileImg")
    public String uploadProfileImg(HttpSession session, @RequestParam("file") MultipartFile file) throws IOException {

        String userid = (String) session.getAttribute("userid");

        String containerName = userid;
        azureBlobService.uploadBlobFromFile(containerName, userid + "_profile.png", file.getInputStream());

        return "redirect:/updateProfile";
    }

    @GetMapping("/myboard")
    public String showHomePage(HttpSession session, Model model) {
        String userid = (String) session.getAttribute("userid");
        if (userid == null) {
            return "redirect:/"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        List<Post> posts = postRepository.findByUserid(userid);
        model.addAttribute("posts", posts);

        // System.out.println(posts.get(0).getTitle());

        Profile profile = profileRepository.findByUserid(userid);
        model.addAttribute("profile", profile);

        String imgurl = "https://easyportstorage.blob.core.windows.net/" + userid + "/" + userid + "_profile.png";

        model.addAttribute("imgurl", imgurl);

        return "myboard";
    }

    @GetMapping("/header")
    public String headerPage(Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "header"; // home.html을 렌더링
    }

    @GetMapping("/editport") // 포트폴리오 작성 페이지
    public String showEditPortPage(HttpSession session, Model model) {
        String userid = (String) session.getAttribute("userid");
        if (userid == null) {
            return "redirect:/"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }
        Post post = new Post();
        post.setUserid(userid); // 작성자 id 지정
        model.addAttribute("post", post);
        return "editport";
    }

    // 글 저장 로직
    @PostMapping("/savePost")
    public String savePost(@ModelAttribute("post") Post post) {
        postRepository.save(post);
        return "redirect:/myboard";
    }

    @PostMapping("/updatePost")
    public String postMethodName(HttpSession session, @ModelAttribute("post") Post post) {
        String userid = (String) session.getAttribute("userid");
        if (userid == null) {
            return "redirect:/"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        long postid = post.getId();
        int postnum = (int) postid;

        List<Post> posts = postRepository.findByUserid(userid);
        Post findPosts = posts.get(postnum);

        Long id = findPosts.getId();

        post.setId(id);
        post.setUserid(userid);
        postRepository.save(post);

        return "redirect:/myboard";
    }

    // 프로필 업데이트 창 이동
    @GetMapping("/updateProfile")
    public String profilePage(HttpSession session, Model model) {
        String userid = (String) session.getAttribute("userid");
        if (userid == null) {
            return "redirect:/"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        Profile profile = profileRepository.findByUserid(userid);

        model.addAttribute("profile", profile);

        String imgurl = "https://easyportstorage.blob.core.windows.net/" + userid + "/" + userid + "_profile.png";

        model.addAttribute("imgurl", imgurl);

        return "updateProfile"; // home.html을 렌더링
        // ㄴㅇㄹㄴㅇㄹ
    }

    // 프로필 수정 로직
    @PostMapping("/updateProfile")
    public String updateProfile(HttpSession session, @ModelAttribute("profile") Profile profile) {
        String userid = (String) session.getAttribute("userid");

        System.err.println("출력해야할거 : " + profile.getCanview());
        Long id = profileRepository.findByUserid(userid).getId();
        profile.setId(id);
        profile.setUserid(userid);
        profileRepository.save(profile);
        return "redirect:/myboard";
    }

    @GetMapping("/myboard/view")
    public String showItemDetails(HttpSession session, @RequestParam("no") int itemIndex, Model model) {

        String userid = (String) session.getAttribute("userid");
        if (userid == null) {
            return "redirect:/"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        List<Post> posts = postRepository.findByUserid(userid);
        Post findPosts = posts.get(itemIndex);

        model.addAttribute("posts", findPosts);
        model.addAttribute("postNum", itemIndex);

        return "postview"; // 해당 데이터를 보여줄 뷰의 이름 리턴
    }

    @GetMapping("/publicView")
    public String publicboardView(@RequestParam("code") String code, Model model) {

        Profile publicuser = profileRepository.findByViewkey(code);

        if (publicuser.getCanview() == true) {
            String userid = publicuser.getUserid();

            List<Post> posts = postRepository.findByUserid(userid);
            model.addAttribute("posts", posts);

            // System.out.println(posts.get(0).getTitle());

            Profile profile = profileRepository.findByUserid(userid);
            model.addAttribute("profile", profile);

            String imgurl = "https://easyportstorage.blob.core.windows.net/" + userid + "/" + userid + "_profile.png";

            model.addAttribute("imgurl", imgurl);

            return "publicboard";
        } else {
            return "공개되어 있지 않습니다";
        }

    }

    @GetMapping("/publicpost")
    public String showpublicItemDetails(@RequestParam("code") String code,
    @RequestParam("no") int itemIndex,
    Model model) {

        System.out.println("받은거 확인 : " + code + "," + itemIndex);

        Profile publicuser = profileRepository.findByViewkey(code);
        String userid = publicuser.getUserid();

        List<Post> posts = postRepository.findByUserid(userid);
        model.addAttribute("posts", posts);
        Post findPosts = posts.get(itemIndex);

        model.addAttribute("posts", findPosts);
        model.addAttribute("postNum", itemIndex);

        return "publicpostview"; // 해당 데이터를 보여줄 뷰의 이름 리턴
    }

    @GetMapping("/myboard/change")
    public String showChangePost(HttpSession session, @RequestParam("no") int itemIndex, Model model) {

        String userid = (String) session.getAttribute("userid");
        if (userid == null) {
            return "redirect:/"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        List<Post> post = postRepository.findByUserid(userid);
        Post findPosts = post.get(itemIndex);

        Long index = (long) itemIndex;

        findPosts.setId(index);
        model.addAttribute("post", findPosts);

        return "changeport"; // 해당 데이터를 보여줄 뷰의 이름 리턴
    }

    @GetMapping("/userDetail") // 유저 버튼 클릭시
    public String showUserDetail(HttpSession session, Model model) {

        String userid = (String) session.getAttribute("userid");
        if (userid == null) {
            return "redirect:/"; // 로그인 정보가 없으면 로그인 화면으로 이동
        }

        SiteUser user = userRepository.findByUserid(userid);
        model.addAttribute("user", user);

        return "userDetail";

    }

    @PostMapping("/deleteUser")
    public String deleteUser(HttpSession session, @ModelAttribute("profile") Profile profile) {
        String userid = (String) session.getAttribute("userid");
        // TODO : suerid 로 post 및 다른 기타정보 삭제먼저하게 처리
        Profile currentProfile = profileRepository.findByUserid(userid);
        profileRepository.deleteById(currentProfile.getId());

        return "redirect:/";
    }

    @PostMapping("/changeUserpwd")
    public String changeUserpwd(HttpSession session,
            @ModelAttribute("prepasswd") String curpwd,
            @ModelAttribute("newpasswd") String newpasswd) {

        String userid = (String) session.getAttribute("userid");
        SiteUser currentuser = userRepository.findByUserid(userid);
        String realpwd = currentuser.getPasswd();

        System.out.println("입력받은 현재 비밀번호 : " + curpwd);
        System.out.println("진짜 현재 비밀번호  : " + realpwd);

        if (curpwd == realpwd) {
            currentuser.setPasswd(newpasswd);
            // userRepository.save(currentuser);
            // TODO : user레포지터리 jpa 로 바꾸기 위에거 아마 버그 있을듯
        }

        // TODO : 현재비번과 변경 비번 받아오는 코드 추가

        return "redirect:/userDetail";
    }

}
