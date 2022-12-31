package com.spring.boot.user.application;

import com.spring.boot.common.exception.AuthenticationFailException;
import com.spring.boot.common.exception.DuplicatedException;
import com.spring.boot.common.exception.NotFoundException;
import com.spring.boot.user.application.dto.UserInfoDto;
import com.spring.boot.user.domain.User;
import com.spring.boot.user.domain.UserRepository;
import com.spring.boot.role.application.RoleService;
import com.spring.boot.role.domain.Role;
import com.spring.boot.role.domain.RoleName;
import java.util.Optional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final RoleService roleService;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleService = roleService;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public UserInfoDto register(String name, String email, String password, RoleName roleName) {
    User user = saveMember(name, email, password, roleName);
    user.initRole(roleService.getRole(roleName));
    return UserInfoDto.from(user);
  }

  private User saveMember(String name, String email, String password, RoleName roleName) {
    userRepository.findByEmail(email)
        .ifPresent(find -> {
          throw new DuplicatedException("email", email);
        });

    Role role = roleService.getRole(roleName);

    User user = new User(
        email,
        passwordEncoder.encode(password),
        name,
        role);
    return userRepository.save(user);
  }

  private Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public UserInfoDto login(String email, String rawPassword) {
    return findByEmail(email)
        .map(findMember -> {
          if (!findMember.checkPassword(rawPassword, passwordEncoder)) {
            throw new BadCredentialsException("password doesn't match");
          }
          return findMember;
        })
        .map(UserInfoDto::from)
        .orElseThrow(() -> new AuthenticationFailException("email is invalid"));
  }

  @Transactional
  public void updateProfileImage(Long id, String profileImagePath) {
    userRepository.findById(id)
        .ifPresent(findMember -> {
          findMember.setProfileImagePath(profileImagePath);
        });
  }

  public User findById(Long id) {
    return userRepository.findById(id).orElseThrow(() -> new NotFoundException(User.class, id));
  }

}
