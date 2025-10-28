package online.taphoaptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import online.taphoaptit.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
