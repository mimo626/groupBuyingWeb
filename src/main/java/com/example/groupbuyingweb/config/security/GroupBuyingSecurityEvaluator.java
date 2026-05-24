package com.example.groupbuyingweb.config.security;

import com.example.groupbuyingweb.domain.entity.mysql.GroupBuying;
import com.example.groupbuyingweb.domain.enums.GroupBuyingStatus;
import com.example.groupbuyingweb.domain.enums.UserRole;
import com.example.groupbuyingweb.repository.mysql.GroupBuyingParticipationRepository;
import com.example.groupbuyingweb.repository.mysql.GroupBuyingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("groupBuyingSecurity")
@RequiredArgsConstructor
public class GroupBuyingSecurityEvaluator {

    private final GroupBuyingRepository groupBuyingRepository;
    private final GroupBuyingParticipationRepository participationRepository;

    // 공통 로직=========================================================================

    // 유저 ID 가져오기 (비로그인 시 null 반환)
    private String getUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    // 엔티티를 받아서 주최자인지 확인
    private boolean checkOrganizer(GroupBuying groupBuying, String userId) {
        return groupBuying.getMember().getId().equals(userId);
    }

    // 권한 검증 API (@PreAuthorize 에서 호출)=================================================

    // 주최자 확인
    public boolean isOrganizer(Authentication authentication, Long groupBuyingId) {
        String userId = getUserId(authentication);
        if (userId == null) return false;

        return groupBuyingRepository.findById(groupBuyingId)
                .map(groupBuying -> checkOrganizer(groupBuying, userId))
                .orElse(false);
    }

    // 참여자 확인
    public boolean isParticipant(Authentication authentication, Long groupBuyingId) {
        String userId = getUserId(authentication);
        if (userId == null) return false;

        return participationRepository.existsByGroupBuyingIdAndMemberIdAndRole(
                groupBuyingId, userId, UserRole.PARTICIPANT);
    }

    // 수정/삭제 권한 (주최자 + 모집중 + 다른 참여자 0명)
    public boolean canModifyGroupBuying(Authentication authentication, Long groupBuyingId) {
        String userId = getUserId(authentication);
        if (userId == null) return false;

        return groupBuyingRepository.findById(groupBuyingId)
                .map(groupBuying -> {
                    boolean isOrganizer = checkOrganizer(groupBuying, userId);
                    boolean isRecruiting = GroupBuyingStatus.RECRUITING.equals(groupBuying.getStatus());
                    boolean hasParticipants = participationRepository.existsByGroupBuyingIdAndRole(groupBuyingId, UserRole.PARTICIPANT);

                    return isOrganizer && isRecruiting && !hasParticipants;
                })
                .orElse(false);
    }

    // 참여 취소 권한 (참여자 본인 + 모집중)
    public boolean canCancelParticipation(Authentication authentication, Long groupBuyingId) {
        String userId = getUserId(authentication);
        if (userId == null) return false;

        return groupBuyingRepository.findById(groupBuyingId)
                .map(groupBuying -> {
                    boolean isRecruiting = GroupBuyingStatus.RECRUITING.equals(groupBuying.getStatus());
                    boolean isParticipant = participationRepository.existsByGroupBuyingIdAndMemberIdAndRole(groupBuyingId, userId, UserRole.PARTICIPANT);

                    return isRecruiting && isParticipant;
                })
                .orElse(false);
    }

    // 참여 권한 (주최자 아님 + 아직 참여 안함)
    public boolean canParticipate(Authentication authentication, Long groupBuyingId) {
        String userId = getUserId(authentication);
        if (userId == null) return false;

        return groupBuyingRepository.findById(groupBuyingId)
                .map(groupBuying -> {
                    boolean isOrganizer = checkOrganizer(groupBuying, userId);
                    boolean isAlreadyParticipant = participationRepository.existsByGroupBuyingIdAndMemberIdAndRole(groupBuyingId, userId, UserRole.PARTICIPANT);

                     boolean isRecruiting = GroupBuyingStatus.RECRUITING.equals(groupBuying.getStatus());

                    return !isOrganizer && !isAlreadyParticipant && isRecruiting;
                })
                .orElse(false);
    }
}