package online.taphoaptit.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import online.taphoaptit.entity.CartItem;
import online.taphoaptit.entity.User;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
}
