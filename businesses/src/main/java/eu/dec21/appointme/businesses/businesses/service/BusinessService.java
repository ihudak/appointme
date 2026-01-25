package eu.dec21.appointme.businesses.businesses.service;

import eu.dec21.appointme.businesses.businesses.entity.Business;
import eu.dec21.appointme.businesses.businesses.mapper.BusinessMapper;
import eu.dec21.appointme.businesses.businesses.repository.BusinessRepository;
import eu.dec21.appointme.businesses.businesses.request.BusinessRequest;
import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import eu.dec21.appointme.common.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessMapper businessMapper;
    private final BusinessRepository businessRepository;

    public BusinessResponse save(BusinessRequest request, Authentication connectedUser) {
        Long ownerId = SecurityUtils.getUserIdFromAuthenticationOrThrow(connectedUser);
        Business business = businessMapper.toBusiness(request);
        business.setOwner(ownerId);
        return businessMapper.toBusinessResponse(businessRepository.save(business));
    }

    public BusinessResponse findById(Long id) {
        return businessRepository.findById(id).map(businessMapper::toBusinessResponse).orElseThrow(() -> new EntityNotFoundException("Business not found with id " + id));
    }
}
