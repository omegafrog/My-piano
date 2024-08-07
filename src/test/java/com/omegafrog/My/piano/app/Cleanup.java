package com.omegafrog.My.piano.app;

import com.google.common.base.CaseFormat;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Cleanup {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    public void cleanUp(){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        List<String> collect = entityManager.getMetamodel().getEntities().stream().map(entity -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entity.getName()))
                .collect(Collectors.toList());

        for (String table : collect) {
            if(table.equals("user")) table = "person";
            if(table.equals("order")) table = "orders";
            entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
        }
        entityManager.createNativeQuery("TRUNCATE TABLE cart_content").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE reply_seq").executeUpdate();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        transaction.commit();
        entityManager.close();
    }
}
