package com.muse_ai.logic.entity.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>  {
    @Query("""
         SELECT u
         FROM User u
         WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :term, '%'))
         """)
    List<User> findUsersWithCharacterInName(@Param("term") String term);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
