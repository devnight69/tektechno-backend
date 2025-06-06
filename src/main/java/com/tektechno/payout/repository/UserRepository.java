package com.tektechno.payout.repository;

import com.tektechno.payout.model.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

  Optional<Users> findByEmailAndStatus(String email, boolean status);

  boolean existsByMobileNumber(String mobileNumber);

  boolean existsByEmail(String email);


}
