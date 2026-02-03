package dalgrock.playlist.service.dto.command;

import dalgrock.playlist.core.exception.OAuthProcessFailedException;
import dalgrock.playlist.core.exception.UnsupportedProviderException;
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
            default -> throw new UnsupportedProviderException();
        };
    }

    private static OAuthCommand ofKakao(Map<String, Object> attributes) {
        try {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            return new OAuthCommand(
                    (String) profile.get("nickname"),
                    (String) profile.get("thumbnail_image_url"),
                    String.valueOf(attributes.get("id")),
                    OauthProvider.KAKAO
            );
        } catch (Exception e) {
            throw new OAuthProcessFailedException();
        }
    }
}
