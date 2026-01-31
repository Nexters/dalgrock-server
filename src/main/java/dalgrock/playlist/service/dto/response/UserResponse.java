package dalgrock.playlist.service.dto.response;

import dalgrock.playlist.model.OauthProvider;
import dalgrock.playlist.model.User;
import dalgrock.playlist.model.UserRole;

public record UserResponse(
        Long id,
        String nickname,
        String profileImage,
        OauthProvider provider,
        UserRole role
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                user.getProvider(),
                user.getRole()
        );
    }
}
