package dalgrock.playlist.infrastructure.deepseek;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * DeepSeek API 호출 (주간 음악 분석 리포트 생성).
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DeepSeekReportClient {

    private static final String MODEL = "deepseek-chat";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.api-url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    /**
     * DeepSeek API 키가 설정되어 있는지 여부.
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * 시스템 메시지로 채팅 완료 요청을 보내고, 응답의 content 텍스트를 반환합니다.
     *
     * @param systemMessage 시스템 메시지 (분석 데이터 + 생성 지시 + 출력 형식)
     * @return DeepSeek 응답 content (리포트 JSON 문자열 등)
     * @throws DeepSeekReportException API 키 미설정, HTTP 오류, 응답 파싱 오류 시
     */
    public String ask(String systemMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new DeepSeekReportException("deepseek.api-key is not configured");
        }

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "messages", List.of(Map.of("role", "system", "content", systemMessage)),
                "stream", false
        );
        String bodyJson;
        try {
            bodyJson = OBJECT_MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new DeepSeekReportException("요청 JSON 직렬화 실패", e);
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DeepSeekReportException("DeepSeek API 요청 실패", e);
        }

        if (response.statusCode() != 200) {
            throw new DeepSeekReportException(
                    "DeepSeek API 오류: HTTP " + response.statusCode() + " - " + response.body());
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (choices.isEmpty()) {
                throw new DeepSeekReportException("DeepSeek 응답에서 내용을 찾을 수 없습니다.");
            }
            JsonNode messageContent = choices.get(0).path("message").path("content");
            if (messageContent.isMissingNode() || !messageContent.isTextual()) {
                throw new DeepSeekReportException("DeepSeek 응답에서 내용을 찾을 수 없습니다.");
            }
            return messageContent.asText();
        } catch (JsonProcessingException e) {
            throw new DeepSeekReportException("DeepSeek 응답 파싱 실패", e);
        }
    }
}
