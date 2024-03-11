package it.daniele.mycar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"it.daniele.mycar", "cool.cena.openai.autoconfigure"})
@SpringBootApplication
public class MycarApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(MycarApplication.class, args);
	}

}
