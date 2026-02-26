package dalgrock.playlist.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * DB에 enum 이름 또는 한글 표시값이 저장된 경우 모두 올바르게 매핑합니다.
 */
@Converter(autoApply = false)
public class EmotionValueConverter implements AttributeConverter<EmotionValue, String> {

    @Override
    public String convertToDatabaseColumn(EmotionValue attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public EmotionValue convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return EmotionValue.UNKNOWN;
        }
        return EmotionValue.fromString(dbData);
    }
}
