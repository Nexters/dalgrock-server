package dalgrock.playlist.infrastructure.repository;

import dalgrock.playlist.model.OauthProvider;
import dalgrock.playlist.model.User;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByProviderAndProviderId(OauthProvider provider, String providerId);

    User save(User user);
}
