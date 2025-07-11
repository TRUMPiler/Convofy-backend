package com.convofy.convofy.Repository;

import com.convofy.convofy.Entity.Complain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplainRepository extends JpaRepository<Complain,String> {
}
