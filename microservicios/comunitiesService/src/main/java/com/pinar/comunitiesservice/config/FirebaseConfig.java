package com.pinar.comunitiesservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("classpath:/pinar-e98e9-firebase-adminsdk-fbsvc-46a7db943f.json")
    private Resource privateKey;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        InputStream credentials = new ByteArrayInputStream(privateKey.getContentAsByteArray());
         FirebaseOptions options = FirebaseOptions.builder()
                 .setCredentials(GoogleCredentials.fromStream(credentials))
                 .build();
         return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
