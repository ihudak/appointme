package eu.dec21.appointme.businesses.businesses.service;

import eu.dec21.appointme.businesses.businesses.config.RatingConfig;
import eu.dec21.appointme.businesses.businesses.entity.Business;
import eu.dec21.appointme.businesses.businesses.mapper.BusinessMapper;
import eu.dec21.appointme.businesses.businesses.repository.BusinessRepository;
import eu.dec21.appointme.businesses.businesses.request.BusinessRequest;
import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import eu.dec21.appointme.businesses.client.CategoryFeignClient;
import eu.dec21.appointme.common.response.PageResponse;
import eu.dec21.appointme.common.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessMapper businessMapper;
    private final BusinessRepository businessRepository;
    private final RatingConfig ratingConfig;
    private final CategoryFeignClient categoryFeignClient;

    public BusinessResponse save(BusinessRequest request, Authentication connectedUser) {
        Long ownerId = SecurityUtils.getUserIdFromAuthenticationOrThrow(connectedUser);
        Business business = businessMapper.toBusiness(request);
        business.setOwnerId(ownerId);
        return businessMapper.toBusinessResponse(businessRepository.save(business));
    }

    public BusinessResponse findById(Long id) {
        return businessRepository.findById(id).map(businessMapper::toBusinessResponse).orElseThrow(() -> new EntityNotFoundException("Business not found with id " + id));
    }

    public PageResponse<BusinessResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("weightedRating").descending());
        Page<Business> businesses = businessRepository.findAll(pageable);
        return new PageResponse<>(
            businesses.getContent().stream().map(businessMapper::toBusinessResponse).toList(),
            businesses.getTotalElements(),
            businesses.getTotalPages(),
            businesses.getNumber(),
            businesses.getSize(),
            businesses.isLast(),
            businesses.isEmpty()
        );
    }

    public PageResponse<BusinessResponse> findByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("weightedRating").descending());
        Page<Business> businesses = businessRepository.findByCategoryId(categoryId, pageable);
        return new PageResponse<>(
            businesses.getContent().stream().map(businessMapper::toBusinessResponse).toList(),
            businesses.getTotalElements(),
            businesses.getTotalPages(),
            businesses.getNumber(),
            businesses.getSize(),
            businesses.isLast(),
            businesses.isEmpty()
        );
    }

    public PageResponse<BusinessResponse> findByCategoryWithSubcategories(Long categoryId, int page, int size) {
        Set<Long> allCategoryIds = new HashSet<>(categoryFeignClient.getAllSubcategoryIds(categoryId));
        allCategoryIds.add(categoryId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("weightedRating").descending());
        Page<Business> businesses = businessRepository.findByCategoryIdIn(allCategoryIds, pageable);
        
        return new PageResponse<>(
            businesses.getContent().stream().map(businessMapper::toBusinessResponse).toList(),
            businesses.getTotalElements(),
            businesses.getTotalPages(),
            businesses.getNumber(),
            businesses.getSize(),
            businesses.isLast(),
            businesses.isEmpty()
        );
    }

    public Double calculateWeightedRating(Business business) {
        return business.getCalculatedRating(
                ratingConfig.getConfidenceThreshold(),
                ratingConfig.getGlobalMean()
        );
    }

    public void updateBusinessRating(Business business, Double newRating, Integer newReviewCount) {
        business.updateRating(
                newRating,
                newReviewCount,
                ratingConfig.getConfidenceThreshold(),
                ratingConfig.getGlobalMean()
        );
        businessRepository.save(business);
    }
}
