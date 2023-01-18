package com.spring.boot.member.application.dto;

import com.spring.boot.member.domain.Member;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Builder
@Getter
public class MemberResponseDto {
  private Long id;
  private String name;
  private String email;
  private String profileImagePath;
  private GrantedAuthority grantedAuthority;

  public static MemberResponseDto from(
      Member member){
    return MemberResponseDto.builder()
        .id(member.getId())
        .email(member.getEmail())
        .name(member.getName())
        .profileImagePath(member.getProfileImagePath())
        .grantedAuthority(member.getGrantedAuthority())
        .build();
  }
}