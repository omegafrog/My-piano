package com.omegafrog.My.piano.app.web.domain.notification;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class PushInstance {

    private FirebaseApp app;

    public PushInstance(String serviceAccountPath) throws IOException {
        log.info("{}", serviceAccountPath);
        FileInputStream serviceAccount =
                new FileInputStream(serviceAccountPath);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        if (FirebaseApp.getApps().size() == 0)
            app = FirebaseApp.initializeApp(options);
        else app = FirebaseApp.getApps().get(0);
    }

    public String sendMessageTo(String topic, String body, String clientToken) throws FirebaseMessagingException {
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance(app);
        return firebaseMessaging.send(Message.builder()
                .putData("body", body)
                .setToken(clientToken)
                .build());
    }

    public String sendMessageTo(String topic, String body) throws FirebaseMessagingException {
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance(app);
        return firebaseMessaging.send(Message.builder()
                .putData("body", body)
                .setTopic(topic)
                .build());
    }

    public String getMessages() {
        FirebaseMessaging instance = FirebaseMessaging.getInstance(app);
        return null;
    }
}
