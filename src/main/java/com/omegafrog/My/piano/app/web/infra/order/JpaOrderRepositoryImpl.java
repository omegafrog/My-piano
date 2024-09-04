package com.omegafrog.My.piano.app.web.infra.order;

import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.order.Order;
import com.omegafrog.My.piano.app.web.domain.order.OrderRepository;
import com.omegafrog.My.piano.app.web.domain.order.QOrder;
import com.omegafrog.My.piano.app.web.domain.relation.QUserPurchasedLesson;
import com.omegafrog.My.piano.app.web.domain.relation.QUserPurchasedSheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaOrderRepositoryImpl implements OrderRepository {

    @Autowired
    private SimpleJpaOrderRepository jpaRepository;
    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Override
    public Order save(Order order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) throws EntityNotFoundException {
        Order order = findById(id).orElseThrow(() -> new EntityNotFoundException("Cannot find Order entity : " + id));

        // remove relationship
        if (order.getItem() instanceof SheetPost) {
            QUserPurchasedSheetPost purchasedSheetPost = QUserPurchasedSheetPost.userPurchasedSheetPost;
            jpaQueryFactory.delete(purchasedSheetPost)
                    .where(purchasedSheetPost.sheetPost.id
                            .eq(order.getItem().getId())).execute();
        } else if (order.getItem() instanceof Lesson) {
            QUserPurchasedLesson purchasedLesson = QUserPurchasedLesson.userPurchasedLesson;
            jpaQueryFactory.delete(purchasedLesson)
                    .where(purchasedLesson.lesson.id
                            .eq(order.getItem().getId())).execute();
        }

        //remove entity
        jpaQueryFactory.delete(QOrder.order)
                .where(QOrder.order.id.eq(id)).execute();
    }

    @Override
    public List<Order> findByBuyer_id(Long id) {
        return jpaRepository.findByBuyer_Id(id);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}
