package dalgrock.playlist.config;

import dalgrock.playlist.infrastructure.spotify.interceptor.SpotifyRetryInterceptor;
import java.net.http.HttpClient;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Configuration
public class RestClientConfig {

    @Value("${client.connection-timeout}")
    private Long connectionTimeOut;

    @Value("${client.read-timeout}")
    private Long readTimeOut;

    @Bean
    public RestClient restClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectionTimeOut))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeOut));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .requestInterceptor(new SpotifyRetryInterceptor())
                .build();
    }
}
