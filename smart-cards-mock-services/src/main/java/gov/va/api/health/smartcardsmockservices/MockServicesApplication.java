package gov.va.api.health.smartcardsmockservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MockServicesApplication implements CommandLineRunner {
  @Autowired MockServices server;

  public static void main(String[] args) {
    SpringApplication.run(MockServicesApplication.class, args);
  }

  @Override
  public void run(String... args) {
    server.start();
  }
}
