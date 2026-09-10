package com.parcelstationx.service;

import com.parcelstationx.dao.UserDao;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.User;

public final class AuthenticationService {
  private final UserDao users;
  private final PasswordHasher passwords;

  public AuthenticationService(UserDao users, PasswordHasher passwords) {
    this.users = users;
    this.passwords = passwords;
  }

  public User login(String username, char[] password) {
    if (username == null || username.isBlank() || password == null || password.length == 0) {
      throw new BusinessException("请输入用户名和密码。");
    }
    User user =
        users.findByUsername(username.trim()).orElseThrow(() -> new BusinessException("用户名或密码错误。"));
    if (!user.enabled() || !user.passwordHash().equals(passwords.hash(new String(password)))) {
      throw new BusinessException("用户名或密码错误。");
    }
    java.util.Arrays.fill(password, '\0');
    return user;
  }
}
