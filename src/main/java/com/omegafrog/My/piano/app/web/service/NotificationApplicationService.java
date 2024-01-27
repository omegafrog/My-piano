package com.omegafrog.My.piano.app.web.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.omegafrog.My.piano.app.utils.exception.subscription.SubscriptionExistException;
import com.omegafrog.My.piano.app.web.domain.notification.PushInstance;
import com.omegafrog.My.piano.app.web.domain.notification.Subscription;
import com.omegafrog.My.piano.app.web.domain.notification.SubscriptionRepository;
import io.lettuce.core.RedisCommandExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class NotificationApplicationService {

    @Autowired
    private SubscriptionRepository repository;
    @Autowired
    private PushInstance pushInstance;

    /**
     * 구독한 유저의 토큰을 저장한다.
     * @param token : 구독 요청을 전송한 유저의 토큰
     * @param userId : 구독 요청을 전송한 유저의 id
     */
    public void subscribeUser(String token, Long userId) {
        if(repository.findByUserId(userId).isPresent())
            throw new SubscriptionExistException("이미 구독한 유저입니다.");
        repository.save(new Subscription(token, userId));
    }

    /**
     * userId에 해당하는 구독한 사용자에게 메세지를 전송한다.
     * @param userId : 구독한 user의 Id
     * @return 전송된 message Id
     * @throws FirebaseMessagingException
     */
    public String sendMessageTo(String topic,  String body, Long userId) throws FirebaseMessagingException {
        Subscription subscription = repository.findByUserId(userId)
                .orElseThrow(() -> new RedisCommandExecutionException("This user is not subscribed yet"));
        String token = subscription.getToken();
        return pushInstance.sendMessageTo(topic, body, token);
    }

}
