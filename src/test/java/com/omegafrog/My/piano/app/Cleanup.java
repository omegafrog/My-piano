package com.omegafrog.My.piano.app;

import com.google.common.base.CaseFormat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
@Slf4j
public class Cleanup {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired(required = false)
    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    private List<TestResettable> resettables;

    public void cleanUp() {
        log.info("cleanup start");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        // entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY
        // FALSE;").executeUpdate();
        List<String> collect = entityManager.getMetamodel().getEntities().stream()
                .map(entity -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entity.getName()))
                .collect(Collectors.toList());

        for (String table : collect) {
            if (table.equals("user"))
                table = "person";
            if (table.equals("order"))
                table = "orders";
            entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
        }
        // entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY
        // TRUE;").executeUpdate();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        transaction.commit();
        entityManager.close();

        File file = new File("static/sheets");
        File file2 = new File("static/thumbnails");
        File[] sheetFiles = file.listFiles();
        if (sheetFiles != null) {
            for (File f : sheetFiles) {
                f.delete();
            }
        }

        File[] thumbnailFiles = file2.listFiles();
        if (thumbnailFiles != null) {
            for (File f : thumbnailFiles) {
                f.delete();
            }
        }

        if (resettables != null) {
            for (TestResettable resettable : resettables) {
                try {
                    resettable.reset();
                } catch (RuntimeException e) {
                    log.warn("failed to reset test store: {}", resettable.getClass().getName(), e);
                }
            }
        }

        if (redisTemplate != null) {
            try {
                redisTemplate.getConnectionFactory().getConnection().flushAll();
            } catch (RuntimeException e) {
                log.warn("redis flushAll skipped: {}", e.getMessage());
            }
        }
    }
}
