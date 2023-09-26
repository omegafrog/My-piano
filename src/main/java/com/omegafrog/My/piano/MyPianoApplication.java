package com.omegafrog.My.piano;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
public class MyPianoApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(MyPianoApplication.class);
		app.addListeners(new ApplicationPidFileWriter()); // ApplicationPidFileWriter 설정
		app.run(args);
	}

}
