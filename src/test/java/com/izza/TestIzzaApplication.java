package com.izza;

import org.springframework.boot.SpringApplication;

public class TestIzzaApplication {

    public static void main(String[] args) {
        SpringApplication.from(IzzaApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
