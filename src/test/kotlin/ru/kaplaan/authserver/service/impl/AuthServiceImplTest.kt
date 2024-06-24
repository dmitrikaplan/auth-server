package ru.kaplaan.authserver.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import ru.kaplaan.authserver.domain.entity.user.Role
import ru.kaplaan.authserver.domain.entity.user.User
import ru.kaplaan.authserver.domain.exception.user.UserAlreadyRegisteredException
import ru.kaplaan.authserver.service.EmailService
import ru.kaplaan.authserver.service.JwtService
import ru.kaplaan.authserver.service.RefreshTokenService
import ru.kaplaan.authserver.service.UserService

@ExtendWith(MockitoExtension::class)
class AuthServiceImplTest{

    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var refreshTokenService: RefreshTokenService

    @Mock
    private lateinit var emailService: EmailService

    @Mock
    private lateinit var jwtService: JwtService

    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var authServiceImpl: AuthServiceImpl


    @Test
    fun `register with not registered user`(){
        //given
        val user = User().apply {
            this.email = "test@test.ru"
            this.username = "testuser"
            this.password = "testpassword"
            this.role = Role.ROLE_USER
        }
        val encodedPassword = "encodedPassword"

        //when
        doReturn(null).whenever(userService).getUserByUsername(user.username)
        doReturn(encodedPassword).whenever(passwordEncoder).encode(user.password)
        doReturn(user).whenever(userService).save(user)
        doNothing().whenever(emailService).activateUserByEmail(eq(user.email), eq(user.username), any())

        val registeredUser = authServiceImpl.register(user)

        //then
        assertEquals(encodedPassword, registeredUser.password)
        assertNotNull(registeredUser.activationCode)
        assertFalse(registeredUser.activated)

        verifyNoMoreInteractions(userService, emailService)

    }

    @Test
    fun `register with registered user`(){
        //given
        val user = User().apply {
            this.email = "test@test.ru"
            this.username = "testuser"
            this.password = "testpassword"
            this.role = Role.ROLE_USER
        }

        //when
        doReturn(user).whenever(userService).getUserByUsername(user.username)

        //then
        assertThrows(UserAlreadyRegisteredException::class.java){
            authServiceImpl.register(user)
        }

        verifyNoMoreInteractions(passwordEncoder, userService, emailService)
    }



}