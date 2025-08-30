package com.foxfox.demo.repository;

import com.foxfox.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户持久化接口:
 * - 注册前校验: existsByUsername / existsByEmail
 * - 登录查询: findByUsernameOrEmail (可支持用户名或邮箱登录)
 * - 其他精确查询: findByUsername / findByEmail
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsernameAndPassword(String username, String password);

}