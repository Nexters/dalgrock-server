package dalgrock.playlist.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dalgrock.playlist.service.dto.WeeklyReportPayloadData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * LLM 응답과 Record 기반 데이터를 병합하여 최종 리포트 JSON을 생성합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WeeklyReportMergeService {

    private static final Pattern JSON_BLOCK = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```");

    private final ObjectMapper objectMapper;

    /**
     * LLM JSON 문자열과 Record 기반 페이로드 데이터를 병합하여 최종 리포트 JSON 문자열을 반환합니다.
     */
    public String merge(String llmResponse, WeeklyReportPayloadData payload) {
        JsonNode llmRoot = parseLlmJson(llmResponse);

        ObjectNode result = objectMapper.createObjectNode();

        result.set("overallSummary", buildOverallSummary(llmRoot, payload));
        result.set("weeklyPlaylist", buildWeeklyPlaylist(payload));
        result.set("weeklyEmotionSummary", buildWeeklyEmotionSummary(llmRoot, payload));
        result.set("emotionGenreDescriptions", buildEmotionGenreDescriptions(llmRoot, payload));
        result.set("contextSummaries", buildContextSummaries(payload));
        result.set("weeklyComparison", buildWeeklyComparison(llmRoot, payload));

        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("리포트 JSON 직렬화 실패", e);
        }
    }

    private JsonNode parseLlmJson(String llmResponse) {
        String json = llmResponse.trim();
        Matcher m = JSON_BLOCK.matcher(json);
        if (m.find()) {
            json = m.group(1).trim();
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.warn("LLM 응답 JSON 파싱 실패, 원본 사용: {}", e.getMessage());
            throw new IllegalArgumentException("LLM 응답 JSON 파싱 실패", e);
        }
    }

    private ObjectNode buildOverallSummary(JsonNode llmRoot, WeeklyReportPayloadData payload) {
        ObjectNode node = objectMapper.createObjectNode();
        node.set("title", llmRoot.path("overallSummary").path("title"));
        ArrayNode tags = objectMapper.createArrayNode();
        List<String> tagList = payload.topEmotionTagsForSummary();
        if (tagList.isEmpty()) {
            tags.add("미상");
        } else {
            for (String value : tagList) {
                tags.add(value);
            }
        }
        node.set("summaryTags", tags);
        return node;
    }

    private ObjectNode buildWeeklyPlaylist(WeeklyReportPayloadData payload) {
        ObjectNode node = objectMapper.createObjectNode();
        ArrayNode musics = objectMapper.createArrayNode();
        for (Map<String, String> m : payload.weeklyPlaylistMusics()) {
            ObjectNode music = objectMapper.createObjectNode();
            music.put("title", m.getOrDefault("title", ""));
            music.put("artist", m.getOrDefault("artist", ""));
            music.put("thumbnail", m.getOrDefault("thumbnail", ""));
            musics.add(music);
        }
        node.set("musics", musics);
        return node;
    }

    private ObjectNode buildWeeklyEmotionSummary(JsonNode llmRoot, WeeklyReportPayloadData payload) {
        ObjectNode node = objectMapper.createObjectNode();
        node.set("title", llmRoot.path("weeklyEmotionSummary").path("title"));

        ArrayNode tags = objectMapper.createArrayNode();
        for (List<Map<String, String>> day : payload.dailyEmotionTagsByDay()) {
            ArrayNode dayArr = objectMapper.createArrayNode();
            for (Map<String, String> t : day) {
                ObjectNode tag = objectMapper.createObjectNode();
                tag.put("category", t.getOrDefault("category", ""));
                tag.put("value", t.getOrDefault("value", ""));
                dayArr.add(tag);
            }
            tags.add(dayArr);
        }
        node.set("tags", tags);
        return node;
    }

    private ArrayNode buildEmotionGenreDescriptions(JsonNode llmRoot, WeeklyReportPayloadData payload) {
        ArrayNode result = objectMapper.createArrayNode();
        List<Map<String, Object>> topData = payload.topEmotionGenreData();

        for (Map<String, Object> data : topData) {
            String emotion = (String) data.get("emotion");
            @SuppressWarnings("unchecked")
            List<String> genres = (List<String>) data.getOrDefault("genres", List.of());
            @SuppressWarnings("unchecked")
            List<String> thumbnails = (List<String>) data.getOrDefault("thumbnail", List.of());

            String description = findDescriptionForEmotion(llmRoot, emotion);

            ObjectNode merged = objectMapper.createObjectNode();
            merged.put("emotion", emotion);
            merged.put("description", description);
            merged.set("genres", objectMapper.valueToTree(genres));
            merged.set("thumbnail", objectMapper.valueToTree(thumbnails));
            result.add(merged);
        }
        return result;
    }

    private String findDescriptionForEmotion(JsonNode llmRoot, String emotion) {
        JsonNode llmItems = llmRoot.path("emotionGenreDescriptions");
        if (!llmItems.isArray()) {
            return "";
        }
        for (JsonNode item : llmItems) {
            if (emotion.equals(item.path("emotion").asText(""))) {
                return item.path("description").asText("");
            }
        }
        return "";
    }

    private ArrayNode buildContextSummaries(WeeklyReportPayloadData payload) {
        ArrayNode result = objectMapper.createArrayNode();
        for (Map.Entry<String, List<String>> e : payload.situationToThumbnails().entrySet()) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("value", e.getKey());
            node.set("thumbnail", objectMapper.valueToTree(e.getValue()));
            result.add(node);
        }
        return result;
    }

    private ObjectNode buildWeeklyComparison(JsonNode llmRoot, WeeklyReportPayloadData payload) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("emotion", payload.topEmotion());
        node.put("genre", payload.topGenre());
        node.set("nextWeekSuggestion", llmRoot.path("weeklyComparison").path("nextWeekSuggestion"));
        return node;
    }
}
