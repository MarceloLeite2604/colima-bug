package com.github.marceloleite2604.colimabug;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  @Autowired
  private final UserRepository userRepository;

  public User save(User user) {
    return userRepository.save(user);
  }
}
