package com.omegafrog.My.piano.app.web.infra.order;

import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.order.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaOrderRepositoryImpl implements OrderRepository {

    @Autowired
    private SimpleJpaOrderRepository jpaRepository;
    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) throws EntityNotFoundException{
        findById(id).ifPresentOrElse(entity->jpaRepository.delete(entity),
                ()-> {throw new EntityNotFoundException();});
    }
}
