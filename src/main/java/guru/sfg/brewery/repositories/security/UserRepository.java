package guru.sfg.brewery.repositories.security;

import guru.sfg.brewery.domain.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
