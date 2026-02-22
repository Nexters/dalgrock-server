package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.OauthProvider;
import dalgrock.playlist.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByProviderAndProviderId(OauthProvider provider, String providerId);

    User save(User user);

    /**
     * 주간 리포트 생성 대상이 되는 모든 활성 사용자를 조회합니다.
     */
    List<User> findAll();
}
