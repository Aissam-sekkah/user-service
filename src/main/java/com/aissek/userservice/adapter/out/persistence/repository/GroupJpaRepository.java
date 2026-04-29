package com.aissek.userservice.adapter.out.persistence.repository;

import com.aissek.userservice.adapter.out.persistence.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupJpaRepository extends JpaRepository<GroupEntity, String> {
    boolean existsByName(String name);
}
