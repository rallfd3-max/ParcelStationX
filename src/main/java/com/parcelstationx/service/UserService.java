package com.parcelstationx.service;

import com.parcelstationx.dao.UserDao;
import com.parcelstationx.exception.BusinessException;
import com.parcelstationx.model.*;
import java.time.LocalDateTime;
import java.util.List;

public final class UserService {
  private final UserDao users;
  private final PasswordHasher passwords;

  public UserService(UserDao users, PasswordHasher passwords) {
    this.users = users;
    this.passwords = passwords;
  }

  public User create(String username, String password, String displayName, UserRole role) {
    if (username == null || username.isBlank() || password == null || password.length() < 6)
      throw new BusinessException("账号不能为空，密码至少 6 位。");
    if (users.findByUsername(username).isPresent()) throw new BusinessException("账号已存在。");
    return users.save(
        new User(
            null,
            username,
            passwords.hash(password),
            displayName,
            role,
            true,
            LocalDateTime.now(),
            null));
  }

  public User setEnabled(long id, boolean enabled) {
    User u = users.findById(id).orElseThrow(() -> new BusinessException("员工不存在。"));
    if (u.role() == UserRole.ADMIN && !enabled) throw new BusinessException("不能禁用管理员。");
    return users.save(
        new User(
            u.id(),
            u.username(),
            u.passwordHash(),
            u.displayName(),
            u.role(),
            enabled,
            u.createdAt(),
            u.lastLoginAt()));
  }

  public List<User> findAll() {
    return users.findAll();
  }
}
