package com.example.batch;

import com.example.batch.jobs.mission02.domain.Image;
import com.example.batch.jobs.mission02.domain.Member;
import com.example.batch.jobs.mission02.domain.MemberStatus;
import com.example.batch.jobs.mission02.repository.ImageRepository;
import com.example.batch.jobs.mission02.repository.MemberRepository;
import com.example.batch.jobs.mission02.repository.RandomMemberFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("회원 삭제 배치 테스트")
class MemberDeleteJobTest {

  private static final int MEMBER_COUNT = 100_000;
  private static final int IMAGES_PER_MEMBER = 10;

  private static final Logger log = LoggerFactory.getLogger(MemberDeleteJobTest.class);

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private ImageRepository imageRepository;

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  private Job memberDeleteJob;

  @BeforeEach
  @Transactional
  void setUp() {
    // 회원 생성
    List<Member> members = RandomMemberFactory.createRandomMembers(MEMBER_COUNT);
    memberRepository.saveAll(members);

    List<Image> images = RandomMemberFactory.createRandomImagesForMembers(members, IMAGES_PER_MEMBER);
    imageRepository.saveAll(images);

    // 사전 조건 확인
    assertThat(memberRepository.count()).isEqualTo(MEMBER_COUNT);
    assertThat(imageRepository.count()).isEqualTo(MEMBER_COUNT * IMAGES_PER_MEMBER);
  }

  @AfterEach
  void tearDown() {
    imageRepository.deleteAll();
    memberRepository.deleteAll();
  }

  @Test
  @DisplayName("INACTIVE 회원과 관련된 이미지 삭제 검증")
  void shouldDeleteInactiveMembersAndCascadeImages() throws Exception {
    // when: 배치 작업 실행
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis())
        .toJobParameters();

    JobExecution jobExecution = jobLauncher.run(memberDeleteJob, jobParameters);

    // then: 배치 상태 검증
    assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    // INACTIVE 회원 및 관련 이미지가 삭제되었는지 확인
    long remainingMembers = memberRepository.count();
    long remainingImages = imageRepository.count();

    assertThat(remainingMembers).isLessThan(MEMBER_COUNT);
    assertThat(remainingImages).isLessThan(MEMBER_COUNT * IMAGES_PER_MEMBER);

    // ACTIVE 상태의 회원이 여전히 존재하는지 확인
    long activeMemberCount = memberRepository.findAll().stream()
        .filter(member -> member.getStatus() == MemberStatus.ACTIVE)
        .count();

    assertThat(activeMemberCount).isPositive(); // ACTIVE 회원이 1명 이상 있어야 함

    // 로깅
    log.info("=== 테스트 결과 ===");
    log.info("총 회원 수 (MEMBER_COUNT): {}", MEMBER_COUNT);
    log.info("총 이미지 수 (IMAGES_COUNT): {}", MEMBER_COUNT * IMAGES_PER_MEMBER);
    log.info("남은 회원 수 (remainingMembers): {}", remainingMembers);
    log.info("남은 이미지 수 (remainingImages): {}", remainingImages);
    log.info("ACTIVE 상태의 회원 수 (activeMemberCount): {}", activeMemberCount);
  }
}
