package com.omegafrog.My.piano.app.external.elasticsearch;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface SheetPostIndexRepository extends CrudRepository<SheetPostIndex, Long> {
    long deleteByIdIn(Collection<Long> ids);

    Slice<SheetPostIndex> findAll(Pageable pageable);

    void deleteById(Long id);
}
