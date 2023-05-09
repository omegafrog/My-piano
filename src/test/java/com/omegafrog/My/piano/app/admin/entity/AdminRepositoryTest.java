package com.omegafrog.My.piano.app.admin.entity;

import com.omegafrog.My.piano.app.admin.dto.AdminDto;
import com.omegafrog.My.piano.app.admin.dto.UpdateAdminDto;
import com.omegafrog.My.piano.app.enums.Position;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    @Test
    void saveNFindTest(){
        Admin admin = Admin.builder()
                .name("admin1")
                .email("email1")
                .build();
        Admin saved = adminRepository.save(admin);
        Optional<Admin> founded = adminRepository.findById(saved.getId());
        Assertions.assertThat(founded).isPresent();
        Assertions.assertThat(founded.get()).isEqualTo(saved);
    }

    @Test
    void updateTest(){
        Admin admin = Admin.builder()
                .name("admin1")
                .email("email1")
                .build();
        Admin saved = adminRepository.save(admin);
        String changedName = "changed";
        UpdateAdminDto updated = UpdateAdminDto.builder()
                .name(changedName)
                .email("email")
                .position(Position.ADMIN)
                .build();
        Admin updatedAdmin = saved.update(updated);
        Admin updatedAdminEntity = adminRepository.save(updatedAdmin);
        AdminDto updatedDto = updatedAdminEntity.toDto();
        Assertions.assertThat(updatedAdmin).isEqualTo(saved);
        Assertions.assertThat(updatedDto.getName()).isEqualTo(changedName);
    }

    @Test
    void deleteTest(){
        Admin admin = Admin.builder()
                .name("admin1")
                .email("email1")
                .build();
        Admin saved = adminRepository.save(admin);
        adminRepository.deleteById(saved.getId());
        Optional<Admin> founded = adminRepository.findById(saved.getId());
        Assertions.assertThat(founded).isEmpty();
    }


}