package com.omegafrog.My.piano.app.web.infra.sheetPost;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class JpaSheetPostRepositoryImpl implements SheetPostRepository {
    @Autowired
    private SimpleJpaSheetPostRepository jpaRepository;


    @Override
    public SheetPost save(SheetPost sheetPost) {
        return jpaRepository.save(sheetPost);
    }

    @Override
    public Optional<SheetPost> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<SheetPost> findBySheetId(Long sheetId) {
        return jpaRepository.findBySheet_id(sheetId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    public void deleteAll(){
        jpaRepository.deleteAll();
    }

    @Override
    public Long count() {
        return jpaRepository.count();
    }
}
