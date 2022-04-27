package com.github.marceloleite2604.colimabug;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.InternetProtocol;
import com.github.dockerjava.api.model.Ports;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy;
import org.testcontainers.shaded.org.apache.commons.lang.ArrayUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class CheckTcpConnectionStartupCheckStrategy extends IsRunningStartupCheckStrategy {

  private static final ExposedPort POSTGRES_EXPOSED_PORT = new ExposedPort(5432, InternetProtocol.TCP);

  private static final int TIMEOUT_MILLISECONDS = 10_000;

  @SneakyThrows
  @Override
  public StartupStatus checkStartupState(DockerClient dockerClient, String containerId) {
    final var startupStatus = super.checkStartupState(dockerClient, containerId);

    /*
     * This is necessary due to the fact that Colima might take more than five seconds to reflect
     * ports bindings on its containers on localhost.
     * More information on this link: https://github.com/abiosoft/colima/issues/71#issuecomment-979516106
     */
    if (StartupStatus.SUCCESSFUL.equals(startupStatus)){

      log.info("Container started. Trying to reach bound port through TCP connection.");
      final var firstPortBinding = retrieveFirstPortBinding(dockerClient, containerId);

      final var ipAddress = firstPortBinding.getHostIp();
      final var port = Integer.parseInt(firstPortBinding.getHostPortSpec());

      final var start = System.currentTimeMillis();
      boolean timeout = false;
      boolean acceptingTcpConnections = false;
      long elapsedTime = 0;

      while (!timeout && !acceptingTcpConnections) {

        acceptingTcpConnections = isAcceptingTcpConnections(ipAddress, port);
        elapsedTime = System.currentTimeMillis() - start;
        timeout = elapsedTime > TIMEOUT_MILLISECONDS;
      }

      if (acceptingTcpConnections) {
        log.info(String.format("Postgres database successfully reached after %.1f seconds.",
            ((double) elapsedTime) / 1000.0));
      } else {
        throw new IllegalStateException(
            String.format("Could not reach Postgres database on address %s and port %s after %.1f seconds.",
                ipAddress,
                port,
                ((double) TIMEOUT_MILLISECONDS) / 1000.0));
      }
    }
    return startupStatus;
  }

  private boolean isAcceptingTcpConnections(String ipAddress, int port) {
    try (Socket clientSocket = new Socket()) {
      clientSocket.connect(new InetSocketAddress(ipAddress, port), 1000);
    } catch (IOException exception) {
      return false;
    }
    return true;
  }

  private Ports.Binding retrieveFirstPortBinding(DockerClient dockerClient, String containerId) {
    final var portsBinding = dockerClient.inspectContainerCmd(containerId)
        .exec()
        .getNetworkSettings()
        .getPorts()
        .getBindings()
        .get(POSTGRES_EXPOSED_PORT);

    if (ArrayUtils.isEmpty(portsBinding)) {
      throw new IllegalStateException(String.format("Could not find port %s binding.", POSTGRES_EXPOSED_PORT));
    }

    return portsBinding[0];
  }
}
