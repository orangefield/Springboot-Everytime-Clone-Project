package site.metacoding.everytimeclone.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.everytimeclone.domain.user.User;
import site.metacoding.everytimeclone.domain.user.UserRepository;
import site.metacoding.everytimeclone.handler.ex.CustomApiException;
import site.metacoding.everytimeclone.handler.ex.CustomException;
import site.metacoding.everytimeclone.web.api.dto.user.EmailUpdateDto;
import site.metacoding.everytimeclone.web.api.dto.user.LoginDto;
import site.metacoding.everytimeclone.web.api.dto.user.NicknameUpdateDto;
import site.metacoding.everytimeclone.web.api.dto.user.PasswordUpdateDto;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public boolean 유저네임중복검사(String username) {
        Optional<User> userOp = userRepository.findByUsername(username);

        if (userOp.isPresent()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean 닉네임중복검사(String nickname) {
        Optional<User> userOp = userRepository.findByUsername(nickname);

        if (userOp.isPresent()) {
            return false;
        } else {
            return true;
        }
    }

    @Transactional
    public void 회원가입(User user) {
        userRepository.save(user);
    }

    public User 로그인(LoginDto loginDto) {
        Optional<User> userOp = userRepository.mLogin(loginDto.getUsername(), loginDto.getPassword());
        if (userOp.isPresent()) {
            return userOp.get();
        } else {
            throw new CustomApiException("없는사용자입니다.");
        }
    }

    public Boolean 로그인(String username, String password) {
        Optional<User> userOp = userRepository.mLogin(username, password);
        if (userOp.isPresent()) {
            return true;
        } else {
            throw new CustomApiException("없는사용자입니다.");
        }
    }

    public User 유저네임보내주기(String email) { // 유저네임 찾기

        Optional<User> userOp = userRepository.findByEmail(email);

        if (userOp.isPresent()) {
            return userOp.get();
        } else {
            throw new CustomApiException("해당 이메일이 존재하지 않습니다.");
        }
    }

    @Transactional
    public User 패스워드초기화(String username, String email) {
        Optional<User> userOp = userRepository.findByUsernameAndEmail(username, email);

        if (userOp.isPresent()) {
            User userEntity = userOp.get();

            // 6자의 난수 생성 후 비밀번호 지정
            Integer randomNum = (int) (Math.random() * (999999 - 100000 + 1) + 100000);
            String randomPassword = randomNum.toString();

            userEntity.setPassword(randomPassword);
            return userEntity;
        } else {
            throw new CustomApiException("일치하는 정보가 존재하지 않습니다.");
        }
    }

    @Transactional
    public void 이메일수정(Integer id, EmailUpdateDto emailUpdateDto) {
        Optional<User> userOp = userRepository.findById(id);

        if (userOp.isPresent()) {
            User userEntity = userOp.get();

            // 비밀번호 비교
            if (userEntity.getPassword().equals(emailUpdateDto.getPassword())) {
                // 기존 이메일과 비교
                if (userEntity.getEmail().equals(emailUpdateDto.getEmail())) {
                    throw new RuntimeException("현재 사용하시는 이메일과 동일합니다.");
                }
                userEntity.setEmail(emailUpdateDto.getEmail());
            } else {
                throw new CustomApiException("비밀번호가 일치하지 않습니다.");
            }
        } else {
            throw new CustomApiException("존재하지 않는 사용자입니다.");
        }
    }

    @Transactional
    public void 비밀번호수정(Integer id, PasswordUpdateDto passwordUpdateDto) {
        Optional<User> userOp = userRepository.findById(id);

        if (userOp.isPresent()) {
            User userEntity = userOp.get();

            if (userEntity.getPassword().equals(passwordUpdateDto.getCurrentPassword())) {
                userEntity.setPassword(passwordUpdateDto.getPassword()); // 비밀번호 변경
            } else {
                throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
            }
        } else {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }
    }

    @Transactional
    public void 닉네임수정(Integer id, NicknameUpdateDto nicknameUpdateDto) {
        Optional<User> userOp = userRepository.findById(id);

        if (userOp.isPresent()) {
            User userEntity = userOp.get();

            if (userEntity.getNickname().equals(nicknameUpdateDto.getNickname())) {
                throw new CustomException("현재 사용하시는 닉네임과 동일합니다.");
            } else {
                userEntity.setNickname(nicknameUpdateDto.getNickname()); // 닉네임 변경
            }
        } else {
            throw new RuntimeException("존재하지 않는 사용자입니다.");
        }
    }

    @Transactional
    public void 회원탈퇴(Integer id) {
        userRepository.deleteById(id);
    }

}