package gov.va.api.health.smartcards;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ApplicationTest {
  @Test
  void contextLoads() {}

  @Test
  void main() {
    Application.main(new String[0]);
  }
}
