package com.nomelestar.serversc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ServerscApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerscApplication.class, args);
    }

}
