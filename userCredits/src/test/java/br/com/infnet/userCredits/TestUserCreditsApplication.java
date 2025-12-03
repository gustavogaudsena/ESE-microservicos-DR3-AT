package br.com.infnet.userCredits;

import org.springframework.boot.SpringApplication;

public class TestUserCreditsApplication {

	public static void main(String[] args) {
		SpringApplication.from(UserCreditsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
