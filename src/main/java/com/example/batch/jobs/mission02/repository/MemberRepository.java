package com.example.batch.jobs.mission02.repository;

import com.example.batch.jobs.mission02.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
