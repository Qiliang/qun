package com.weixin.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);
    List<User> findByRoleOrRoleOrRole(User.ROLE role1,User.ROLE role2,User.ROLE role3);
    List<User> findByEmployer(String employer);
    List<User> findByParent(String parent);
}