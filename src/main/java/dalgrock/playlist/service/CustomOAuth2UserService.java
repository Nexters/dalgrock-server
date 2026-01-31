package dalgrock.playlist.service;

import dalgrock.playlist.infrastructure.repository.UserRepository;
import dalgrock.playlist.model.User;
import dalgrock.playlist.model.UserRole;
import dalgrock.playlist.service.dto.command.OAuthCommand;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 1. 어느 소셜 서비스인지 확인 (kakao, google 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 2. 해당 서비스의 고유 식별자 키 이름 (kakao는 'id')
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 3. 서비스에 맞게 데이터를 파싱 (앞서 만든 OAuthCommand 활용)
        OAuthCommand command = OAuthCommand.of(registrationId, oAuth2User.getAttributes());

        // 4. DB 저장 및 업데이트 (provider와 providerId 조합으로 식별)
        User user = saveOrUpdate(command);

        // 5. Spring Security 세션에 담을 객체 반환 (내부적으로만 잠시 사용됨)
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                oAuth2User.getAttributes(),
                userNameAttributeName
        );
    }

    private User saveOrUpdate(OAuthCommand command) {
        return userRepository.findByProviderAndProviderId(command.provider(), command.providerId())
                .map(entity -> entity.update(command.nickname(), command.profileImage()))
                .orElseGet(() -> userRepository.save(User.builder()
                        .nickname(command.nickname())
                        .profileImage(command.profileImage())
                        .provider(command.provider())
                        .providerId(command.providerId())
                        .role(UserRole.USER)
                        .build()));
    }
}
