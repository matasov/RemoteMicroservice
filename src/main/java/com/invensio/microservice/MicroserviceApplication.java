package com.invensio.microservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.invensio.microservice.service.AuthorizationService;

@SpringBootApplication
public class MicroserviceApplication implements CommandLineRunner {

  AuthorizationService authorizationService;

  @Autowired
  public void setAuthorizationService(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  public static void main(String[] args) {
    SpringApplication.run(MicroserviceApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {

  }

}
