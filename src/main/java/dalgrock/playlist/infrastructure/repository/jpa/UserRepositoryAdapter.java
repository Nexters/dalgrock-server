package dalgrock.playlist.infrastructure.repository.jpa;

import dalgrock.playlist.infrastructure.repository.UserRepository;
import dalgrock.playlist.model.OauthProvider;
import dalgrock.playlist.model.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findByProviderAndProviderId(OauthProvider provider, String providerId) {
        return jpaUserRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }
}
