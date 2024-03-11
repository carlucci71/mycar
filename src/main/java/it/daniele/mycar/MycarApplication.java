package it.daniele.mycar;

import cool.cena.openai.autoconfigure.OpenAiAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@ComponentScan(basePackages = {"it.daniele.mycar", "cool.cena.openai.autoconfigure"},
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
				classes = OpenAiAutoConfiguration.class))

@SpringBootApplication
public class MycarApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(MycarApplication.class, args);
	}

}
