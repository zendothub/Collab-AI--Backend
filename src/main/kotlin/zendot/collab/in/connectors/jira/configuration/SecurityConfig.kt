package zendot.collab.`in`.connectors.jira.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
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
            .authorizeRequests()
            .requestMatchers("/login", "/oauth/callback").permitAll() // Allow these paths
            .anyRequest().authenticated()
            .and()
            .oauth2Login { oauth2 ->
                oauth2.loginPage("/login") // Custom login page if needed
                oauth2.defaultSuccessUrl("/dashboard", true) // Redirect after successful login
            }

        return http.build()
    }

    @Bean
    fun clientRegistrationRepository(): ClientRegistrationRepository {
        val clientRegistration = ClientRegistration.withRegistrationId("atlassian")
            .clientId(clientId) // Use client ID from application.properties
            .clientSecret(clientSecret) // Use client secret from application.properties
            .scope(*scopes.split(" ").toTypedArray()) // Convert scopes string into an array
            .redirectUri(redirectUri) // Redirect URI from application.properties
            .authorizationUri(authorizationUri) // Authorization URI from application.properties
            .tokenUri(tokenUri) // Token URI from application.properties
            .userInfoUri(userInfoUri) // User info URI from application.properties
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // Set the authorization grant type
            .build()

        return InMemoryClientRegistrationRepository(clientRegistration) // Register client in memory
    }

    @Bean
    fun oAuth2AuthorizedClientRepository(): OAuth2AuthorizedClientRepository {
        return HttpSessionOAuth2AuthorizedClientRepository() // Store OAuth2 client in session
    }
}
