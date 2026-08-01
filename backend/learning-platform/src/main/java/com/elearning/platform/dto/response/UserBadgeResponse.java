package com.elearning.platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBadgeResponse {

    private Long id;

    private Long badgeId;

    private String badgeName;

    private String badgeDescription;

    private String badgeIcon;

    private Integer requiredPoints;

    private LocalDateTime unlockedAt;

}