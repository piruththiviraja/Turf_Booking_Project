package com.example.Trufbooking.service;

import com.example.Trufbooking.entity.admintable;
import com.example.Trufbooking.entity.slot;
import com.example.Trufbooking.entity.turfDto;
import com.example.Trufbooking.entity.turfowner;
import com.example.Trufbooking.repository.admintable_repo;
import com.example.Trufbooking.repository.turfownerRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class adminservice {

    @Autowired
    admintable_repo adminrepo;

    @Autowired
    turfownerRepo turforepo;
    @Autowired
    private com.example.Trufbooking.repository.slotrepo slotrepo;

    public List<String> getDistinctLocations() {
        return adminrepo.findDistinctLocations();
    }

    public List<String> getDistinctSports() {
        return adminrepo.findByDistinctSports();
    }
    public List<String> getSportsInLocation(String location){
        return adminrepo.findBySportInLoc(location);
    }

    public List<admintable> findTurfsByLocationAndSport(String location, String sport) {
        return adminrepo.findByLocationAndSport(location, sport);
    }

    public admintable findTurfById(Integer turfId) {
        System.out.println("Things going to frontend"+adminrepo.findById(turfId).get().getTurfname());
        return adminrepo.findById(turfId).orElse(null); // Or throw a custom exception if not found
    }

    public boolean addTurf(admintable turfDetails) {
        try {
            System.out.println("serviceSide: "+turfDetails.getSports());
            List<String> duplicateSports = new ArrayList<>();
            duplicateSports.add("Cricket");
            duplicateSports.add("Football");
            System.out.println("Duplicate: "+ duplicateSports);
            adminrepo.save(turfDetails);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<admintable> getTurfsByAdmin(int admin_id) {
        turfowner admin = turforepo.findById(admin_id).orElse(null);
        if (admin != null) {
            return adminrepo.findByAdmin(admin);
        }
        return Collections.emptyList();
    }

    public Optional<admintable> getTurfById(int turfid) {
        return adminrepo.findById(turfid);
    }

    // Update turf details
    public boolean updateTurf(int turfid, admintable updatedTurf) {
        Optional<admintable> existingTurfOpt = adminrepo.findById(turfid);
        if (existingTurfOpt.isPresent()) {
            admintable existingTurf = existingTurfOpt.get();
            // Update the fields
            System.out.println(existingTurf.getSports());
            System.out.println(updatedTurf.getSports());
            existingTurf.setTurfname(updatedTurf.getTurfname());
            existingTurf.setLocation(updatedTurf.getLocation());
            existingTurf.setPrice(updatedTurf.getPrice());
            existingTurf.setSports(updatedTurf.getSports());
            existingTurf.setLength(updatedTurf.getLength());
            existingTurf.setBreadth(updatedTurf.getBreadth());
            existingTurf.setImageData(updatedTurf.getImageData());

            // Save the updated turf
            adminrepo.save(existingTurf);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean deleteTurf(int turfid) {
        if (!adminrepo.existsById(turfid)) {
            throw new IllegalArgumentException("Turf not found");
        }

        List<slot> slots = slotrepo.deleteByTurfId(turfid);
        int flag = 0;
        for (slot s : slots) {
            String timeData = s.getTime();
            if (timeData.contains("\"status\": \"booked\"")) {
                flag = 1;
                return false; // Prevent deletion if any slot is booked
            }
        }
        if(flag == 0) {
            if (adminrepo.existsById(turfid)) {
                slotrepo.deletebyturfId(turfid);
                adminrepo.deleteById(turfid);
            }
        }
        return true;
    }

}
