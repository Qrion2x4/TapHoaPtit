package online.taphoaptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import online.taphoaptit.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
