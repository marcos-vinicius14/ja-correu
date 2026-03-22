package org.jacorreu.identity.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jacorreu.identity.application.dto.request.CreateUserRequest;
import org.jacorreu.identity.core.gateway.PasswordEncoderGateway;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.Email;
import org.jacorreu.user.core.domain.valueobjects.Password;
import org.jacorreu.user.core.gateway.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CreateUserUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoderGateway passwordEncoder;

  @InjectMocks
  private CreateUserUseCase createUserUseCase;

  @Test
  public void testCreateUserSuccess() {
    String email = "test@example.com";
    String password = "password123";
    CreateUserRequest request = new CreateUserRequest("testuser", email, password);

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("senhaCriptografada");

    Result<Void> result = createUserUseCase.execute(request);

    assertTrue(result.isSuccess());
    verify(userRepository, times(1)).save(any(UserDomain.class));

    ArgumentCaptor<UserDomain> userCaptor = ArgumentCaptor.forClass(UserDomain.class);
    verify(userRepository).save(userCaptor.capture());
    UserDomain savedUser = userCaptor.getValue();
    assertEquals(email, savedUser.getEmail().getValue());
    assertEquals("senhaCriptografada", savedUser.getPassword().getValue());

  }

  @Test
  public void testCreateUserFailure_UserAlreadyExists() {
    String email = "teste@gmail.com";
    String password = "password123";
    CreateUserRequest request = new CreateUserRequest("testuser", email, password);

    when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

    Result<Void> result = createUserUseCase.execute(request);

    assertFalse(result.isSuccess());
    assertEquals("Usuario ja existente! Realize o login!", result.getNotification().getErrors().getFirst().message());
    verify(passwordEncoder, times(0)).encode(anyString());
    verify(userRepository, times(0)).save(any(UserDomain.class));
  }
}
