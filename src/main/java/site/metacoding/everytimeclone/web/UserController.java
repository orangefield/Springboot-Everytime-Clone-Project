package site.metacoding.everytimeclone.web;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import site.metacoding.everytimeclone.domain.course.Course;
import site.metacoding.everytimeclone.domain.post.Post;
import site.metacoding.everytimeclone.domain.professor.Professor;
import site.metacoding.everytimeclone.domain.user.User;
import site.metacoding.everytimeclone.service.CourseService;
import site.metacoding.everytimeclone.service.PostService;
import site.metacoding.everytimeclone.service.ProfessorService;
import site.metacoding.everytimeclone.service.UserService;
import site.metacoding.everytimeclone.util.Script;
import site.metacoding.everytimeclone.util.UtilValid;
import site.metacoding.everytimeclone.web.api.dto.user.JoinDto;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final CourseService courseService;
    private final ProfessorService professorService;
    private final HttpSession session;

    // admin 로그인 시 관리자 페이지 이동
    @GetMapping("/s/admin")
    public String adminMain(Model model) {
        List<Course> courses = courseService.강의목록();
        List<Professor> professors = professorService.교수목록();
        model.addAttribute("professors", professors);
        model.addAttribute("courses", courses);
        return "/admin/index";
    }

    @GetMapping("/user/login-form")
    public String loginForm() {
        return "/user/loginForm";
    }

    @GetMapping("/user/join-form")
    public String joinForm(JoinDto joinDto) {
        return "/user/joinForm";
    }

    @PostMapping("/join")
    public String join(@Valid JoinDto joinDto, BindingResult bindingResult) {
        // 핵심로직
        UtilValid.요청에러처리(bindingResult);
        User userEntity = joinDto.toEntity();
        if (userEntity.getUsername().equals("admin")) {
            userEntity.setRole("admin");
        }
        userService.회원가입(userEntity);
        return "redirect:/user/login-form";
    }

    @GetMapping("/user/find-username-form")
    public String findUsernameForm() {
        return "/user/findUsernameForm";
    }

    @GetMapping("/user/password-reset-form")
    public String passwordResetForm() {
        return "/user/findPasswordForm";
    }

    @GetMapping("/s/user/{id}")
    public String detail(@PathVariable Integer id) {
        return "/user/detail";
    }

    @GetMapping("/s/user/{id}/password-form")
    public String passwordUpdateForm(@PathVariable Integer id) {
        return "/user/passwordUpdateForm";
    }

    @GetMapping("/s/user/{id}/email-form")
    public String emailUpdateForm(@PathVariable Integer id, Model model) {
        User principal = (User) session.getAttribute("principal");

        model.addAttribute("userEntity", principal);
        return "/user/emailUpdateForm";
    }

    @GetMapping("/s/user/{id}/nickname-form")
    public String nicknameUpdateForm(@PathVariable Integer id) {
        return "/user/nicknameUpdateForm";
    }

    @GetMapping("/logout")
    public @ResponseBody String logout() {
        session.invalidate();
        return Script.href("/", "로그아웃 되었습니다.");
    }

    @GetMapping("/s/user/{id}/post")
    public String myPostList(@PathVariable Integer id, Model model) {
        List<Post> orderByLikeCountPosts = postService.실시간인기글();
        model.addAttribute("hotPosts", orderByLikeCountPosts);

        List<Post> posts = postService.내가쓴글(id);
        model.addAttribute("posts", posts);
        return "/user/myPostList";
    }

    @GetMapping("/s/user/delete-account")
    public String deleteAccount() {
        return "/user/deleteAccountForm";
    }

}
