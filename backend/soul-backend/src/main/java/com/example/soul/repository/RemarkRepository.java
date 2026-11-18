package com.example.soul.repository;

import com.example.soul.entity.Remark;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RemarkRepository extends JpaRepository<Remark, Long> {
    Optional<Remark> findByOwnerAndTarget(String owner, String target);
}
