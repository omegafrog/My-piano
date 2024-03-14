package com.omegafrog.My.piano.app.web.infrastructure.admin;

import com.omegafrog.My.piano.app.DataJpaUnitConfig;
import com.omegafrog.My.piano.app.web.dto.AdminDto;
import com.omegafrog.My.piano.app.web.dto.UpdateAdminDto;
import com.omegafrog.My.piano.app.web.enums.Position;
import com.omegafrog.My.piano.app.web.domain.admin.Admin;
import com.omegafrog.My.piano.app.web.domain.admin.AdminRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@DataJpaTest
@Import(DataJpaUnitConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    @Test
    @DisplayName("Admin을 저장하고 조회할 수 있어야 한다")
    void saveNFindTest(){
        //given
        Admin admin = Admin.builder()
                .name("admin1")
                .email("email1")
                .build();
        //when
        Admin saved = adminRepository.save(admin);
        Optional<Admin> founded = adminRepository.findById(saved.getId());
        //then
        Assertions.assertThat(founded).isPresent().contains(saved);
    }

    @Test
    @DisplayName("Admin을 update할 수 있어야 한다.")
    void updateTest(){
        //given
        Admin admin = Admin.builder()
                .name("admin1")
                .email("email1")
                .build();
        Admin saved = adminRepository.save(admin);

        //when
        String changedName = "changed";
        UpdateAdminDto updated = UpdateAdminDto.builder()
                .name(changedName)
                .email("email")
                .position(Position.ADMIN)
                .build();

        Admin updatedAdmin = saved.update(updated);
        //then
        Assertions.assertThat(updatedAdmin).isEqualTo(saved);
        Assertions.assertThat(updatedAdmin.getName()).isEqualTo(changedName);
    }

    @Test
    @DisplayName("Admin을 admin id로 삭제할 수 있어야 한다")
    void deleteTest(){
        //givne
        Admin admin = Admin.builder()
                .name("admin1")
                .email("email1")
                .build();
        Admin saved = adminRepository.save(admin);
        //when
        adminRepository.deleteById(saved.getId());
        //then
        Optional<Admin> founded = adminRepository.findById(saved.getId());
        Assertions.assertThat(founded).isEmpty();
    }
}