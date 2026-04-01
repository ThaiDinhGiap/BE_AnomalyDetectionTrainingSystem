package com.sep490.anomaly_training_backend.service.impl;

import com.sep490.anomaly_training_backend.enums.OAuthProvider;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService oauth2UserService;

    @Test
    void processOAuth2User_WhenUserExists_ShouldReturnSameUser() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(Map.of("email", "test@gmail.com", "name", "John", "sub", "sub-123"));
        User user = new User();
        user.setEmail("test@gmail.com");

        lenient().when(userRepository.findByEmailAndDeleteFlagFalse("test@gmail.com")).thenReturn(Optional.of(user));



        OAuth2User result = ReflectionTestUtils.invokeMethod(oauth2UserService, "processMicrosoftUser", oAuth2User);

        assertThat(result.getAttributes()).containsEntry("email", "test@gmail.com");
    }

    @Test
    void processOAuth2User_WhenUserNew_ShouldCreateUser() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(Map.of("email", "new@gmail.com", "name", "New John", "sub", "sub-456"));

        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        OAuth2User result = ReflectionTestUtils.invokeMethod(oauth2UserService, "processMicrosoftUser", oAuth2User);

        assertThat(result.getAttributes()).containsEntry("email", "new@gmail.com");
        verify(userRepository).save(any(User.class));
    }
}
