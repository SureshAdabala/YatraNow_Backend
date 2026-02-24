package com.yatranow.repository;

import com.yatranow.entity.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Optional<Owner> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Owner> findAll(Pageable pageable);

    Page<Owner> findByIsBlocked(Boolean isBlocked, Pageable pageable);
}
