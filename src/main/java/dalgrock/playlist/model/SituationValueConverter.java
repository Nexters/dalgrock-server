package dalgrock.playlist.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * DB에 enum 이름 또는 한글 표시값이 저장된 경우 모두 올바르게 매핑합니다.
 */
@Converter(autoApply = false)
public class SituationValueConverter implements AttributeConverter<SituationValue, String> {

    @Override
    public String convertToDatabaseColumn(SituationValue attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public SituationValue convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return SituationValue.UNKNOWN;
        }
        return SituationValue.fromString(dbData);
    }
}
