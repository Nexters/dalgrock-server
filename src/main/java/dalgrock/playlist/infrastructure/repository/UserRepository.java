package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.OauthProvider;
import dalgrock.playlist.model.User;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByProviderAndProviderId(OauthProvider provider, String providerId);

    Optional<User> findById(Long id);

    User save(User user);
}
