package tn.esprit.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.esprit.eventservice.dto.PhysicalSpaceDTO;

@FeignClient(name = "physical-space-service", url = "http://localhost:7073")
public interface PhysicalSpaceClient {

    @GetMapping("/api/physical-spaces/{id}")
    PhysicalSpaceDTO getPhysicalSpaceById(@PathVariable("id") Long id);
}
