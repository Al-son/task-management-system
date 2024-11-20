package ru.taskmanagment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "ru.taskmanagment")
@EntityScan(basePackages = "ru.taskmanagment.entity")
public class TaskmanagmentApplication {
	public static void main(String[] args) {
		SpringApplication.run(TaskmanagmentApplication.class, args);
	}
}
