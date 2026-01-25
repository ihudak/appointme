package eu.dec21.appointme.businesses.businesses.mapper;

import eu.dec21.appointme.businesses.businesses.entity.Business;
import eu.dec21.appointme.businesses.businesses.entity.BusinessImage;
import eu.dec21.appointme.businesses.businesses.request.BusinessRequest;
import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import org.springframework.stereotype.Service;

@Service
public class BusinessMapper {
                    public Business toBusiness(BusinessRequest request) {
        return Business.builder()
                .id(request.id())
                .name(request.name())
                .description(request.description())
                .address(request.address())
                .location(request.location())
                .phoneNumber(request.phoneNumber())
                .website(request.website())
                .email(request.email())
                .active(true)
                .build();
    }

    public BusinessResponse toBusinessResponse(Business business) {
        return BusinessResponse.builder()
                .id(business.getId())
                .name(business.getName())
                .description(business.getDescription())
                .address(business.getAddress())
                .location(business.getLocation())
                .phoneNumber(business.getPhoneNumber())
                .website(business.getWebsite())
                .email(business.getEmail())
                .imageUrl(business.getImages().stream()
                        .filter(BusinessImage::getIsIcon).min((img1, img2) -> Integer.compare(img1.getDisplayOrder(), img2.getDisplayOrder()))
                        .map(BusinessImage::getImageUrl)
                        .orElse(null))
                .rating(business.getRating())
                .reviewCount(business.getReviewCount())
                .active(business.isActive())
                .build();
    }
}
