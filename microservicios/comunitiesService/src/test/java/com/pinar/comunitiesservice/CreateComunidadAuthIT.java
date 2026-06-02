package com.pinar.comunitiesservice;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

class CreateComunidadAuthIT {

    @Test
    void printCustomToken() throws Exception {
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("pinar-e98e9-firebase-adminsdk-fbsvc-cf4ec6b377.json")) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(FirebaseOptions.builder().setCredentials(credentials).build());
            }
            System.out.println("CUSTOM_TOKEN=" + FirebaseAuth.getInstance().createCustomToken("smoke-test-user"));
        }
    }
}
