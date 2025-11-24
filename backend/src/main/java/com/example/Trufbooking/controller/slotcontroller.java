package com.example.Trufbooking.controller;

import com.example.Trufbooking.entity.admintable;
import com.example.Trufbooking.entity.slotdto;
import com.example.Trufbooking.service.adminservice;
import com.example.Trufbooking.service.slotservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/home")
//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = "http://localhost:5173")
public class slotcontroller {

    @Autowired
    private slotservice slotser;

    @Autowired
    adminservice adminser;

    @GetMapping("/{turfId}")
    public Map<String, Object> getAvailableSlotsWithTurfDetails(@PathVariable Integer turfId) {
        System.out.println(turfId);
        // Get available slots
        List<slotdto> availableSlots = slotser.getAvailableSlots(turfId);
        // Fetch turf details using adminservice
        admintable turfDetails = adminser.findTurfById(turfId);

        // Combine results into a response map
        Map<String, Object> response = new HashMap<>();
        response.put("turfDetails", turfDetails);
        response.put("availableSlots", availableSlots);

        return response;
    }
    @PutMapping("/update-dates")
    public ResponseEntity<String> updateDates() {
        try {
            System.out.println("When user clicks setSlot daily this also gets trigger");
            slotser.updateDatesWithCurrentDate();
            return ResponseEntity.ok("Dates updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating dates: " + e.getMessage());
        }
    }
}