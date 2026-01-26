package eu.dec21.appointme.businesses.businesses.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "application.rating")
public class RatingConfig {
    private int confidenceThreshold = 10;
    private double globalMean = 3.5;

    public void setConfidenceThreshold(int confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public void setGlobalMean(double globalMean) {
        this.globalMean = globalMean;
    }
}
