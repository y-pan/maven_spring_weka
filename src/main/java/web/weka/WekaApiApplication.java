package web.weka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import utils.Var;
import web.config.CORSFilter;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@Controller
@SpringBootApplication
public class WekaApiApplication {

	@ResponseBody
	@RequestMapping("/")
	String entry() {
		return Var.GREETING;
	}
	public static void main(String[] args) {
		SpringApplication.run(WekaApiApplication.class, args);
	}
	
	@Bean
	public FilterRegistrationBean corsFilterRegistration() {
		FilterRegistrationBean reg = new FilterRegistrationBean(new CORSFilter());
		reg.setName("CORS Filter");
		reg.addUrlPatterns("/*");
		reg.setOrder(1);
		return reg;
		
	}
	
	
}
