package eu.dec21.appointme.businesses.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Set;

@FeignClient(name = "categories", url = "${categories.service.url}")
public interface CategoryFeignClient {
    
    @GetMapping("/categories/{categoryId}/subcategories/ids")
    Set<Long> getAllSubcategoryIds(@PathVariable("categoryId") Long categoryId);
}
