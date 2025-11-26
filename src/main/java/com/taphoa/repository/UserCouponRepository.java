package com.taphoa.repository;

import com.taphoa.entity.UserCoupon;
import com.taphoa.entity.User;
import com.taphoa.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByUserAndCoupon(User user, Coupon coupon);
}