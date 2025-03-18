package zendot.collab.`in`.bedrock.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient

@Configuration
class AwsConfig {

    // Inject AWS credentials from application.properties
    @Value("\${aws.accessKeyId}")
    lateinit var accessKeyId: String

    @Value("\${aws.secretAccessKey}")
    lateinit var secretAccessKey: String

    @Value("\${aws.region}")
    lateinit var region: String

    @Bean
    fun bedrockRuntimeClient(): BedrockRuntimeClient {
        println("Configured AWS Region: $region")


        // Create AWS credentials from the properties
        val credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey)


        // Return the Bedrock client with the credentials
        return BedrockRuntimeClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))  // Use StaticCredentialsProvider
            .region(Region.of(region))  // Set region from properties
            .build()
    }
}