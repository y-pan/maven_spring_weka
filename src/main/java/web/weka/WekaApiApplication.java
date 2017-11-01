package web.weka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Controller
@SpringBootApplication
public class WekaApiApplication {

	@ResponseBody
	@RequestMapping("/")
	String entry() {
		return "Web-weka Spring Boot App";
	}
	public static void main(String[] args) {
		SpringApplication.run(WekaApiApplication.class, args);
	}
}
