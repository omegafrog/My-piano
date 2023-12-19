package com.omegafrog.My.piano.app.web.infra.coupon;

import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCouponRepositoryImpl implements CouponRepository {

    @Autowired
    private SimpleJpaCouponRepository jpaRepository;

    @Override
    public Coupon save(Coupon coupon) {
        return jpaRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
}
