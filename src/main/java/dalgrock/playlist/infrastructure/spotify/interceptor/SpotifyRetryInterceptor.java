package dalgrock.playlist.infrastructure.spotify.interceptor;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@Slf4j
public class SpotifyRetryInterceptor implements ClientHttpRequestInterceptor {

    private static final int MAX_RETRIES = 1;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        int attempt = 0;
        IOException lastException = null;

        while (attempt <= MAX_RETRIES) {
            try {
                ClientHttpResponse response = execution.execute(request, body);

                if (response.getStatusCode().is5xxServerError() && attempt < MAX_RETRIES) {
                    log.warn("5xx error occurred. Retrying... (attempt: {}/{})", attempt + 1, MAX_RETRIES + 1);
                    attempt++;
                    continue;
                }

                return response;
            } catch (IOException e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    log.warn("Network error occurred. Retrying... (attempt: {}/{})", attempt + 1, MAX_RETRIES + 1);
                    attempt++;
                } else {
                    throw e;
                }
            }
        }
        throw lastException;
    }
}
