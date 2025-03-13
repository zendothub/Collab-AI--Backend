package zendot.collab.`in`.connectors.jira.authentication

import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.util.UriComponentsBuilder

@RestController
//@RequestMapping("/auth")
class AuthController(private val restTemplate: RestTemplate) {

    @Value("\${jira.client-id}")
    lateinit var JIRA_CLIENT_ID: String

    @Value("\${jira.client-secret}")
    lateinit var JIRA_CLIENT_SECRET: String

    @Value("\${jira.redirect-uri}")
    lateinit var JIRA_REDIRECT_URI: String

    @Value("\${jira.token-url}")
    lateinit var tokenUrl: String

    @Value("\${jira.auth-url}")
    lateinit var JIRA_AUTH_URL: String

    @Value("\${jira.api-url}")
    lateinit var apiUrl: String

    @Value("\${jira.scopes}")
    lateinit var JIRA_SCOPES: String

    @GetMapping("/login")
    fun login(): String {
        val authUrl = UriComponentsBuilder.fromHttpUrl(JIRA_AUTH_URL)
            .queryParam("audience", "api.atlassian.com")
            .queryParam("client_id", JIRA_CLIENT_ID)
            .queryParam("scope", "read:jira-work write:jira-work read:jira-user manage:jira-webhook offline_access")
            .queryParam("redirect_uri", JIRA_REDIRECT_URI)
            .queryParam("state", "random_value")
            .queryParam("response_type", "code")
            .queryParam("prompt", "consent")
            .toUriString()
        println("Authorization URL: $authUrl")
        return "redirect:$authUrl"
    }


    @GetMapping("/oauth/callback")
    fun oauthCallback(
        @RequestParam("code") authCode: String,
        session: HttpSession
    ): ResponseEntity<Map<String, Any>> {
        // Prepare parameters for the token exchange request
        val params = mapOf(
            "grant_type" to "authorization_code",
            "client_id" to JIRA_CLIENT_ID,
            "client_secret" to JIRA_CLIENT_SECRET,
            "code" to authCode,
            "redirect_uri" to JIRA_REDIRECT_URI
        )

        try {
            // Make the token exchange request
            val tokenResponse = restTemplate.postForEntity(tokenUrl, params, Map::class.java)

            // Check if response body is present
            val tokenData = tokenResponse.body
            if (tokenData == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("error" to "Invalid response from token endpoint"))
            }

            // Log the response to verify if refresh_token is present
            println("Token response: $tokenData")

            val accessToken = tokenData["access_token"] as? String
            val refreshToken = tokenData["refresh_token"] as? String

            // Check if the access token is present
            if (accessToken == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("error" to "Access token missing in response"))
            }

            // Store tokens in session (ensure refresh token is handled even if it's missing)
            session.setAttribute("jira_access_token", accessToken)
            session.setAttribute("jira_refresh_token", refreshToken ?: "No refresh token")  // Default if refresh token is missing

            // Return success with the access and refresh token (or a default message for refresh token)
            return ResponseEntity.ok(
                mapOf(
                    "message" to "Login successful",
                    "access_token" to accessToken,
                    "refresh_token" to (refreshToken ?: "No refresh token")  // Provide a default message if refresh token is null
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Error while exchanging authorization code for access token"))
        }
    }

    @GetMapping("/get_issues")
    fun getIssues(@RequestParam("projectKey") projectKey: String, session: HttpSession): ResponseEntity<Any> {
        val accessToken = session.getAttribute("jira_access_token") as String?
        if (accessToken == null) {
            return ResponseEntity.status(401).body("User not authenticated")
        }

        val issuesUrl = "$apiUrl/search?jql=project=$projectKey&maxResults=10"
        val headers = mapOf(
            "Authorization" to "Bearer $accessToken",
            "Accept" to "application/json"
        )

        val issuesResponse = restTemplate.getForEntity(issuesUrl, String::class.java, headers)

        return issuesResponse.body?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.status(500).body("Failed to fetch issues")
    }
}
