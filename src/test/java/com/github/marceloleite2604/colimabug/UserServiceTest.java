package com.github.marceloleite2604.colimabug;

import static org.assertj.core.api.Assertions.assertThat;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureEmbeddedDatabase
class UserServiceTest {

  @Autowired
  private UserService userService;

  @Test
  void shouldAddUserOnRepository() {
    final var user = User.builder()
        .firstName("John")
        .lastName("Smith")
        .build();

    final var persistedUser = userService.save(user);

    assertThat(persistedUser).usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(user);

    assertThat(persistedUser.getId()).isNotNull();
  }
}