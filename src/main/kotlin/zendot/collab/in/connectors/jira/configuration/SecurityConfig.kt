package zendot.collab.`in`.connectors.jira.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Value("\${jira.client-id}")
    private lateinit var clientId: String

    @Value("\${jira.client-secret}")
    private lateinit var clientSecret: String

    @Value("\${jira.redirect-uri}")
    private lateinit var redirectUri: String

    @Value("\${jira.auth-url}")
    private lateinit var authorizationUri: String

    @Value("\${jira.token-url}")
    private lateinit var tokenUri: String

    @Value("\${jira.api-url}")
    private lateinit var userInfoUri: String

    @Value("\${jira.scopes}")
    private lateinit var scopes: String

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf ->
                csrf.disable() // Disable CSRF for simplicity
            }
            .authorizeHttpRequests { auth ->
                auth
                    // Whitelist specific endpoints (no token required)
                    .requestMatchers("/login", "/oauth/callback", "/converse/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .loginPage("/login")
                    .defaultSuccessUrl("/dashboard", true) // Redirect after successful login
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
        return http.build()
    }


    @Bean
    fun clientRegistrationRepository(): ClientRegistrationRepository {
        val clientRegistration = ClientRegistration.withRegistrationId("atlassian")
            .clientId(clientId)
            .clientSecret(clientSecret)
            .scope(*scopes.split(" ").toTypedArray())
            .redirectUri(redirectUri)
            .authorizationUri(authorizationUri)
            .tokenUri(tokenUri)
            .userInfoUri(userInfoUri)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .build()

        return InMemoryClientRegistrationRepository(clientRegistration)
    }

    @Bean
    fun oAuth2AuthorizedClientRepository(): OAuth2AuthorizedClientRepository {
        return HttpSessionOAuth2AuthorizedClientRepository()
    }

}
