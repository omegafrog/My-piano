package com.omegafrog.My.piano.app.web.domain.notification;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Component
public class PushInstance {


    private FirebaseApp app;

    public PushInstance() throws IOException {
        FileInputStream serviceAccount =
                new FileInputStream("src/main/resources/firebase-admin.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        app = FirebaseApp.initializeApp(options);
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
}