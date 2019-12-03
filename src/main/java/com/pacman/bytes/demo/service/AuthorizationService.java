package com.pacman.bytes.demo.service;

import com.pacman.bytes.demo.dto.AccountDto;
import com.pacman.bytes.demo.dto.LoginRequest;
import com.pacman.bytes.demo.dto.LoginResponse;
import com.pacman.bytes.demo.entity.User;
import com.pacman.bytes.demo.repo.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthorizationService {

    @Autowired
    IUserRepository userRepository;

    public LoginResponse login(LoginRequest request) {

        Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(request.getUsername());

        if (!userOptional.isPresent()) {
            return LoginResponse.builder().isLoginSuccessful(false).build();
        }

        User user = userOptional.get();

        if (user.getIsLocked() || !user.getPassword().equals(request.getPassword()))  {
            int failedLoginCount = user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(failedLoginCount);
            if (!user.getIsLocked() && failedLoginCount > 2) {
                user.setIsLocked(true);
            }
            userRepository.save(user);
            return LoginResponse.builder().isLoginSuccessful(false).isAccountLocked(user.getIsLocked()).build();
        }


        boolean isSecQuestionsSet = (user.getSecurityAnswer1() != null && user.getSecurityAnswer2() != null && user.getSecurityAnswer3() != null );

        if (user.getIsPasswordTemporary()) {
            return LoginResponse.builder().isLoginSuccessful(true).isAccountLocked(false).isPasswordTemporary(true).isResetQuestionsAvailable(isSecQuestionsSet).build();
        }

        return LoginResponse.builder().isAccountLocked(false).isLoginSuccessful(true).isPasswordTemporary(false).isResetQuestionsAvailable(isSecQuestionsSet).build();

    }

    public void updateAccount(AccountDto accountDto) {

        Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(accountDto.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setIsLocked(accountDto.getIsLocked());
            user.setSafeWord(accountDto.getSafeWord());
            user.setSecurityAnswer1(accountDto.getSecurityAnswer1());
            user.setSecurityAnswer2(accountDto.getSecurityAnswer2());
            user.setSecurityAnswer3(accountDto.getSecurityAnswer3());
            user.setTelephoneNumber(accountDto.getTelephoneNumber());
            userRepository.save(user);
;
        }


    }

    public boolean changePassword (String username, String password) {

        Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(password);
            user.setIsPasswordTemporary(false);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Optional<AccountDto> getAccount(String username) {

        Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(username);

        if (!userOptional.isPresent()) {
            return Optional.empty();
        }

        User user = userOptional.get();

        AccountDto account = AccountDto.builder()
                .isLocked(user.getIsLocked())
                .isPasswordTemporary(user.getIsPasswordTemporary())
                .safeWord(user.getSafeWord())
                .securityAnswer1(user.getSecurityAnswer1())
                .securityAnswer2(user.getSecurityAnswer2())
                .securityAnswer3(user.getSecurityAnswer3())
                .telephoneNumber(user.getTelephoneNumber())
                .username(user.getUsername())
                .build();

        return Optional.of(account);


    }
}