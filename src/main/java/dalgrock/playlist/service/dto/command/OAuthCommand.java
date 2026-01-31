package dalgrock.playlist.service.dto.command;

import dalgrock.playlist.model.OauthProvider;
import java.util.Map;

public record OAuthCommand(
        String nickname,
        String profileImage,
        String providerId,
        OauthProvider provider
) {

    public static OAuthCommand of(
            String registrationId,
            Map<String, Object> attributes
    ) {
        return switch (registrationId.toLowerCase()) {
            case "kakao" -> ofKakao(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        };
    }

    private static OAuthCommand ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return new OAuthCommand(
                (String) profile.get("nickname"),
                (String) profile.get("thumbnail_image_url"),
                String.valueOf(attributes.get("id")),
                OauthProvider.KAKAO
        );
    }
}
