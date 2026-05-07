package tn.esprit.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@FeignClient(name = "ml-service", url = "${ml.service.url:http://localhost:5000}")
public interface MLPredictionClient {

    @PostMapping("/predict")
    PredictionResponse predict(@RequestBody PredictionRequest request);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class PredictionRequest {
        private String content_type;
        private String difficulty;
        private String teaching_style;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class PredictionResponse {
        private Double efficiency_probability;
        private Boolean is_effective;
    }
}
