package com.xettuyen2026;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        ConfigurableApplicationContext context =
            new SpringApplicationBuilder(Application.class)
                .headless(false)
                .run(args);

        Main main = context.getBean(Main.class);
        main.start();
    }
}
