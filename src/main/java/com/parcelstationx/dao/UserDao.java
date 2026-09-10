package com.parcelstationx.dao;

import com.parcelstationx.model.User;
import java.util.Optional;

public interface UserDao extends BaseDao<User, Long> {
  Optional<User> findByUsername(String username);
}
