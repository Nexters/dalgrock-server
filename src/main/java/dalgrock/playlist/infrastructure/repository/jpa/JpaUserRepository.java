package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.model.OauthProvider;
import dalgrock.playlist.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(OauthProvider provider, String providerId);
}
