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
import eu.dec21.appointme.exceptions.OperationNotPermittedException;
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

    // Public methods - active businesses only
    public BusinessResponse findById(Long id) {
        return businessRepository.findById(id)
                .filter(Business::isActive)
                .map(businessMapper::toBusinessResponse)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with id " + id));
    }

    public PageResponse<BusinessResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("weightedRating").descending());
        Page<Business> businesses = businessRepository.findByActiveTrue(pageable);
        return new PageResponse<>(
            businesses.getContent().stream()
                    .map(businessMapper::toBusinessResponse)
                    .toList(),
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

    // Owner methods - manage their own businesses
    public BusinessResponse createBusiness(BusinessRequest request, Authentication connectedUser) {
        Long ownerId = SecurityUtils.getUserIdFromAuthenticationOrThrow(connectedUser);
        Business business = businessMapper.toBusiness(request);
        business.setOwnerId(ownerId);
        return businessMapper.toBusinessResponse(businessRepository.save(business));
    }

    public PageResponse<BusinessResponse> findByOwner(Authentication connectedUser, int page, int size) {
        Long ownerId = SecurityUtils.getUserIdFromAuthenticationOrThrow(connectedUser);
        Pageable pageable = PageRequest.of(page, size, Sort.by("weightedRating").descending());
        Page<Business> businesses = businessRepository.findByOwnerId(ownerId, pageable);
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

    public BusinessResponse findByIdAndOwner(Long id, Authentication connectedUser) {
        Long ownerId = SecurityUtils.getUserIdFromAuthenticationOrThrow(connectedUser);
        Business business = businessRepository.findByIdAndOwnerId(id, ownerId);
        if (business == null) {
            throw new EntityNotFoundException("Business not found with id " + id + " for current owner");
        }
        return businessMapper.toBusinessResponse(business);
    }

    public BusinessResponse updateBusinessByOwner(Long id, BusinessRequest request, Authentication connectedUser) {
        Long ownerId = SecurityUtils.getUserIdFromAuthenticationOrThrow(connectedUser);
        Business business = businessRepository.findByIdAndOwnerId(id, ownerId);
        if (business == null) {
            throw new OperationNotPermittedException("You do not have permission to update this business");
        }
        
        // Update fields from request
        if (request.name() != null) business.setName(request.name());
        if (request.description() != null) business.setDescription(request.description());
        if (request.address() != null) business.setAddress(request.address());
        if (request.location() != null) business.setLocation(request.location());
        if (request.phoneNumber() != null) business.setPhoneNumber(request.phoneNumber());
        if (request.website() != null) business.setWebsite(request.website());
        if (request.email() != null) business.setEmail(request.email());
        
        return businessMapper.toBusinessResponse(businessRepository.save(business));
    }

    public BusinessResponse toggleBusinessActiveByOwner(Long id, boolean active, Authentication connectedUser) {
        Long ownerId = SecurityUtils.getUserIdFromAuthenticationOrThrow(connectedUser);
        Business business = businessRepository.findByIdAndOwnerId(id, ownerId);
        if (business == null) {
            throw new OperationNotPermittedException("You do not have permission to modify this business");
        }
        
        business.setActive(active);
        return businessMapper.toBusinessResponse(businessRepository.save(business));
    }

    public void deleteBusinessByOwner(Long id, Authentication connectedUser) {
        Long ownerId = SecurityUtils.getUserIdFromAuthenticationOrThrow(connectedUser);
        Business business = businessRepository.findByIdAndOwnerId(id, ownerId);
        if (business == null) {
            throw new OperationNotPermittedException("You do not have permission to delete this business");
        }
        
        businessRepository.delete(business);
    }

    // Admin methods - manage all businesses
    public PageResponse<BusinessResponse> findAllBusinesses(int page, int size, boolean includeInactive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("weightedRating").descending());
        Page<Business> businesses = businessRepository.findAll(pageable);
        
        var content = includeInactive 
                ? businesses.getContent()
                : businesses.getContent().stream().filter(Business::isActive).toList();
        
        return new PageResponse<>(
            content.stream().map(businessMapper::toBusinessResponse).toList(),
            businesses.getTotalElements(),
            businesses.getTotalPages(),
            businesses.getNumber(),
            businesses.getSize(),
            businesses.isLast(),
            businesses.isEmpty()
        );
    }

    public BusinessResponse findByIdAdmin(Long id) {
        return businessRepository.findById(id)
                .map(businessMapper::toBusinessResponse)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with id " + id));
    }

    public BusinessResponse updateBusinessByAdmin(Long id, BusinessRequest request) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with id " + id));
        
        if (request.name() != null) business.setName(request.name());
        if (request.description() != null) business.setDescription(request.description());
        if (request.address() != null) business.setAddress(request.address());
        if (request.location() != null) business.setLocation(request.location());
        if (request.phoneNumber() != null) business.setPhoneNumber(request.phoneNumber());
        if (request.website() != null) business.setWebsite(request.website());
        if (request.email() != null) business.setEmail(request.email());
        
        return businessMapper.toBusinessResponse(businessRepository.save(business));
    }

    public BusinessResponse toggleBusinessActiveByAdmin(Long id, boolean active) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with id " + id));
        
        business.setActive(active);
        return businessMapper.toBusinessResponse(businessRepository.save(business));
    }

    public void deleteBusinessByAdmin(Long id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Business not found with id " + id));
        
        businessRepository.delete(business);
    }

    // Utility methods
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
