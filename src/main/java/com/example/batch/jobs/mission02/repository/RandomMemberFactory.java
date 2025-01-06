package com.example.batch.jobs.mission02.repository;

import com.example.batch.jobs.mission02.domain.Image;
import com.example.batch.jobs.mission02.domain.Member;
import com.example.batch.jobs.mission02.domain.MemberStatus;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

public class RandomMemberFactory {

  private static final RandomGenerator RANDOM = RandomGenerator.getDefault();

  public static List<Member> createRandomMembers(int count) {
    return IntStream.range(0, count)
        .mapToObj(i -> Member.builder()
            .name(generateRandomName())
            .status(generateRandomStatus())
            .build())
        .toList();
  }

  public static List<Image> createRandomImagesForMembers(List<Member> members, int imagesPerMember) {
    return members.stream()
        .flatMap(member -> IntStream.range(0, imagesPerMember)
            .mapToObj(i -> Image.builder()
                .url(generateRandomUrl())
                .member(member)
                .build()))
        .toList();
  }

  private static String generateRandomName() {
    return "User-%s".formatted(RANDOM.nextLong(1_000_000_000, 9_999_999_999L));
  }

  private static MemberStatus generateRandomStatus() {
    return MemberStatus.values()[RANDOM.nextInt(MemberStatus.values().length)];
  }

  private static String generateRandomUrl() {
    return "https://example.com/image/%s".formatted(RANDOM.nextLong(1_000_000_000, 9_999_999_999L));
  }
}
