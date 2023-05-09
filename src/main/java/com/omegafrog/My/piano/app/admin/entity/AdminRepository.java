package com.omegafrog.My.piano.app.admin.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Admin save(Admin admin);

    Optional<Admin> findById(Long id);

    void deleteById(Long id);

}
