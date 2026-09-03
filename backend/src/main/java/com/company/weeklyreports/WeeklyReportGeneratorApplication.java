package com.company.weeklyreports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Weekly Report Generator backend.
 *
 * @SpringBootApplication is a convenience annotation that combines three things:
 *   - @Configuration      -> this class can define Spring beans
 *   - @EnableAutoConfiguration -> Spring Boot auto-configures beans based on the
 *                                 dependencies on the classpath (e.g. sees MySQL driver
 *                                 + JPA starter -> configures a DataSource + EntityManager)
 *   - @ComponentScan      -> Spring scans this package and sub-packages for
 *                             @Component/@Service/@Repository/@Controller classes
 *                             and registers them as beans automatically
 *
 * Because this class lives at com.company.weeklyreports (the root package), component
 * scanning will pick up everything under config/, auth/, user/, project/, report/, etc.
 * without any extra configuration - this is why the package structure matters.
 */
@SpringBootApplication
public class WeeklyReportGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeeklyReportGeneratorApplication.class, args);
    }

}
