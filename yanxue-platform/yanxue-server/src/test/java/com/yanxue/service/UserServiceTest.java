package com.yanxue.service;

import com.yanxue.dto.RegisterRequest;
import com.yanxue.entity.User;
import com.yanxue.mapper.UserMapper;
import com.yanxue.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerShouldStoreBlankPhoneAsNull() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("  user1  ");
        request.setPassword("123456");
        request.setNickname("   ");
        request.setPhone("   ");

        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        }).when(userMapper).insert(any(User.class));
        when(jwtUtil.generateToken(1L)).thenReturn("token");

        String token = userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());

        User insertedUser = userCaptor.getValue();
        assertEquals("user1", insertedUser.getUsername());
        assertEquals("user1", insertedUser.getNickname());
        assertNull(insertedUser.getPhone());
        assertEquals("password", insertedUser.getLoginType());
        assertEquals("encoded-password", insertedUser.getPassword());
        assertEquals("token", token);
    }

    @Test
    void existsByPhoneShouldIgnoreBlankPhone() {
        boolean exists = userService.existsByPhone("   ");

        assertFalse(exists);
        verify(userMapper, never()).selectOne(any());
    }

    @Test
    void loginShouldTrimAccountBeforeLookup() {
        User user = new User();
        user.setId(2L);
        user.setPassword("encoded-password");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches(eq("123456"), eq("encoded-password"))).thenReturn(true);
        when(jwtUtil.generateToken(2L)).thenReturn("token");

        String token = userService.login("  user1  ", "123456");

        assertEquals("token", token);
    }

    @Test
    void loginShouldReturnNullWhenStoredPasswordIsMalformed() {
        User user = new User();
        user.setId(3L);
        user.setUsername("broken-user");
        user.setPassword("not-a-bcrypt-hash");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches(eq("123456"), eq("not-a-bcrypt-hash")))
                .thenThrow(new IllegalArgumentException("Encoded password does not look like BCrypt"));

        String token = userService.login("broken-user", "123456");

        assertNull(token);
    }
}
