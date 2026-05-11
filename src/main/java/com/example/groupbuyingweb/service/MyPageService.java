package com.example.groupbuyingweb.service;

import com.example.groupbuyingweb.domain.dto.request.MyPageRequest;
import com.example.groupbuyingweb.domain.dto.response.MyPageResponse;
import com.example.groupbuyingweb.domain.entity.Member;
import com.example.groupbuyingweb.domain.entity.UserNearbyAddress;
import com.example.groupbuyingweb.repository.MemberRepository;
import com.example.groupbuyingweb.repository.UserNearbyAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MemberRepository memberRepository;
    private final UserNearbyAddressRepository userNearbyAddressRepository;

    private final AddressService addressService;

    public MyPageResponse.Profile getProfile(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow();
        int acornLevel = Math.min(20, (member.getAcornExp() / 10) + 1);

        return new MyPageResponse.Profile(
                member.getId(),
                member.getLoginId(),
                member.getNickname(),
                member.getPoint(),
                member.getAcornExp(),
                acornLevel
                );
    }

    public MyPageResponse.Neighborhood getNeighborhood(String memberId) {

        MemberRepository.MemberLocationInfo location = memberRepository.findLocationById(memberId)
                .orElseThrow();
        List<UserNearbyAddress> nearbyAddressList = userNearbyAddressRepository.findAllByMemberId(memberId);

        return new MyPageResponse.Neighborhood(
                location.getAddress(),
                location.getRadius(),
                location.getEntX(),
                location.getEntX(),
                nearbyAddressList);
    }


    @Transactional
    public MyPageResponse.Neighborhood patchNeighborhood(String memberId, MyPageRequest.UpdateNeighborhood request) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.patchAddress(request);

        userNearbyAddressRepository.deleteAllByMemberId(memberId);

        // 변경된
        List<UserNearbyAddress> nearbyAddressList =
                addressService.createNearbyAddresses(
                        member,
                        request.entX(),
                        request.entY()
                );

        userNearbyAddressRepository.saveAll(nearbyAddressList);

        return new MyPageResponse.Neighborhood(
                member.getAddress(),
                member.getRadius(),
                member.getEntX(),
                member.getEntY(),
                nearbyAddressList
        );

    }
}
