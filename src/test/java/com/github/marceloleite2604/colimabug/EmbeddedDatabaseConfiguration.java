package com.github.marceloleite2604.colimabug;

import java.time.Duration;

import io.zonky.test.db.provider.postgres.PostgreSQLContainerCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class EmbeddedDatabaseConfiguration {

  @Bean
  public PostgreSQLContainerCustomizer postgresContainerCustomizer(CheckTcpConnectionStartupCheckStrategy checkTcpConnectionStartupCheckStrategy) {
    return container -> container.withStartupTimeout(Duration.ofSeconds(60L))
        .withStartupCheckStrategy(checkTcpConnectionStartupCheckStrategy);
  }
}
