package tn.esprit.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "club-service")
public interface ClubClient {

    @PostMapping("/api/clubs/{id}/deduct-budget")
    void deductBudget(@PathVariable("id") Long id, @RequestParam("amount") Double amount);
}
