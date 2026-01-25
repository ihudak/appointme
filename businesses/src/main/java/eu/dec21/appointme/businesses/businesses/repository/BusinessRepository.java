package eu.dec21.appointme.businesses.businesses.repository;


import eu.dec21.appointme.businesses.businesses.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository extends JpaRepository<Business, Long> {
}
