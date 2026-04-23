package com.aissek.userservice.adapter.out.persistence.repository;

import com.aissek.userservice.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    boolean existsByEmail(String email);
}
