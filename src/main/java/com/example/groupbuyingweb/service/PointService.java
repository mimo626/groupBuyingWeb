package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.request.GroupBuyingParticipationRequest;
import com.example.groupbuyingweb.domain.dto.response.GroupBuyingParticipationResponse;
import com.example.groupbuyingweb.repository.GroupBuyingParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private GroupBuyingParticipationRepository participationRepo;

    public GroupBuyingParticipationResponse.UserResult payPoint(GroupBuyingParticipationRequest.Send send) {

        return null;
    }
}
