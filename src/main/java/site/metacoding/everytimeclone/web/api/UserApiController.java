package site.metacoding.everytimeclone.web.api;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import site.metacoding.everytimeclone.domain.user.User;
import site.metacoding.everytimeclone.service.UserService;
import site.metacoding.everytimeclone.util.Script;
import site.metacoding.everytimeclone.util.UtilValid;
import site.metacoding.everytimeclone.util.email.EmailUtil;
import site.metacoding.everytimeclone.web.api.dto.user.DelAccountReqDto;
import site.metacoding.everytimeclone.web.api.dto.user.EmailUpdateDto;
import site.metacoding.everytimeclone.web.api.dto.user.LoginDto;
import site.metacoding.everytimeclone.web.api.dto.user.NicknameUpdateDto;
import site.metacoding.everytimeclone.web.api.dto.user.PasswordUpdateDto;

@RequiredArgsConstructor
@RestController
public class UserApiController {

    private final UserService userService;
    private final HttpSession session;
    private final EmailUtil emailUtil;

    @GetMapping("/api/user/username-same-check")
    public ResponseEntity<?> usernameSameCheck(String username) {
        boolean isNotSame = userService.유저네임중복검사(username);
        return new ResponseEntity<>(isNotSame, HttpStatus.OK);
    }

    @GetMapping("/api/user/nickname-same-check")
    public ResponseEntity<?> nicknameSameCheck(String nickname) {
        boolean isNotSame = userService.닉네임중복검사(nickname);
        return new ResponseEntity<>(isNotSame, HttpStatus.OK);
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto, BindingResult bindingResult,
            HttpServletResponse response) {
        // 인증
        User userEntity = userService.로그인(loginDto);

        if (userEntity == null) {
            return new ResponseEntity<>(-1, HttpStatus.BAD_REQUEST);
        }

        session.setAttribute("principal", userEntity); // 세션에 저장

        // 쿠키에 저장
        if (loginDto.getRemember().equals("on")) {
            response.addHeader("Set-Cookie", "remember=" + loginDto.getUsername() + ";path=/");
        }

        return new ResponseEntity<>(userEntity, HttpStatus.OK);
    }

    @GetMapping("/user/find-username")
    public String findUsername(String email) {

        User userEntity = userService.유저네임보내주기(email);

        emailUtil.sendEmail(userEntity.getEmail(), "요청하신 이메일의 ID",
                "ID : " + userEntity.getUsername());

        return Script.href("/user/login-form", "안내 이메일을 발송하였습니다. 만약 메일이 오지 않는다면, 스팸 편지함을 확인해주세요.");
    }

    @PutMapping("/user/password-reset")
    public ResponseEntity<?> passwordReset(@RequestBody User user) {

        User userEntity = userService.패스워드초기화(user.getUsername(), user.getEmail());

        emailUtil.sendEmail(userEntity.getEmail(), "비밀번호가 초기화 되었습니다",
                "초기화된 비밀번호는 " + userEntity.getPassword() + " 입니다. 로그인 후 비밀번호를 재설정하십시오.");

        return new ResponseEntity<>(1, HttpStatus.OK);
    }

    @PutMapping("/s/api/user/{id}/email")
    public ResponseEntity<?> updateEmail(@PathVariable Integer id, @Valid @RequestBody EmailUpdateDto emailUpdateDto,
            BindingResult bindingResult) {
        UtilValid.요청에러처리(bindingResult);
        userService.이메일수정(id, emailUpdateDto);
        return new ResponseEntity<>(1, HttpStatus.OK);
    }

    @PutMapping("/s/api/user/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Integer id,
            @Valid @RequestBody PasswordUpdateDto passwordUpdateDto, BindingResult bindingResult) {
        UtilValid.요청에러처리(bindingResult);
        userService.비밀번호수정(id, passwordUpdateDto);
        return new ResponseEntity<>(1, HttpStatus.OK);
    }

    @PutMapping("/s/api/user/{id}/nickname")
    public ResponseEntity<?> updateNickname(@PathVariable Integer id,
            @Valid @RequestBody NicknameUpdateDto nicknameUpdateDto, BindingResult bindingResult) {
        UtilValid.요청에러처리(bindingResult);
        userService.닉네임수정(id, nicknameUpdateDto);
        return new ResponseEntity<>(1, HttpStatus.OK);
    }

    @DeleteMapping("/s/user/{id}/delete-account")
    public ResponseEntity<?> deleteAccount(@PathVariable Integer id) {
        userService.회원탈퇴(id);
        session.invalidate();
        return new ResponseEntity<>(1, HttpStatus.OK);
    }

    @PostMapping("/s/user/password-check")
    public ResponseEntity<?> passwordCheck(@RequestBody DelAccountReqDto delAccountReqDto) {
        User principal = (User) session.getAttribute("principal");
        boolean isPresent = userService.로그인(principal.getUsername(), delAccountReqDto.getPassword());
        if (isPresent == true) {
            return new ResponseEntity<>(1, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(-1, HttpStatus.OK);
        }

    }

}
