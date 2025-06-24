
package com.convofy.convofy.Repository;

import com.convofy.convofy.Entity.Interest; // Import your Interest entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterestRepository extends JpaRepository<Interest, UUID> {

    Optional<Interest> findByName(String name);


    List<Interest> findByStatus(Interest.InterestStatus status);
}
