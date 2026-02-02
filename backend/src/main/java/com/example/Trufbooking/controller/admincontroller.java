package com.example.Trufbooking.controller;

import com.example.Trufbooking.entity.admintable;
import com.example.Trufbooking.entity.turfDto;
import com.example.Trufbooking.repository.admintable_repo;
import com.example.Trufbooking.service.adminservice;
import com.example.Trufbooking.service.userservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/home")
//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = "https://turfbookingsystem-xi.vercel.app")
public class admincontroller {

    @Autowired
    adminservice adminser;
    @Autowired
    admintable_repo adminrepo;
    @Autowired
    userservice userser;

    @GetMapping("/locations")
    public List<String> getDistinctLocations() {
        return adminser.getDistinctLocations();
    }

    @GetMapping("/sports")
    public List<String> getDistinctSports() {
        System.out.println(adminser.getDistinctSports());
        return adminser.getDistinctSports();
    }
    @GetMapping("/sportsLocation")
    public List<String> getSportsWithLocation(@RequestParam String location){
        return adminser.getSportsInLocation(location);
    }
    @GetMapping("/turfs")
    public List<Map<String, Object>> getTurfDetails(@RequestParam String location, @RequestParam String sport) {
        List<admintable> turfs = adminser.findTurfsByLocationAndSport(location, sport);

        List<Map<String, Object>> turfsWithImages = new ArrayList<>();
        for (admintable turf : turfs) {

            Map<String, Object> turfMap = new HashMap<>();
            turfMap.put("turfid",turf.getTurfid());
            turfMap.put("turfname", turf.getTurfname());
            turfMap.put("location", turf.getLocation());
            turfMap.put("mobilenumber", turf.getMobilenumber());
            turfMap.put("price", turf.getPrice());
            turfMap.put("sports", turf.getSports());
            turfMap.put("length", turf.getLength());
            turfMap.put("breadth", turf.getBreadth());
            turfMap.put("rating",userser.showRating(turf.getTurfid()));
            try {
                if (turf.getImage() != null) {
                    byte[] imageBytes = turf.getImage().getBytes(1, (int) turf.getImage().length());
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    turfMap.put("image", base64Image);
                } else {
                    turfMap.put("image", null);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                turfMap.put("image", null); // Handle image as null in case of error
            }

            turfsWithImages.add(turfMap);
        }

        return turfsWithImages;
    }
//    @GetMapping("/{turfid}")
//    public List<admintable> getTurfDetails(@PathVariable String turfid) {
//        List<admintable>turfdet=adminrepo.findAll();
//        return turfdet;
//    }


}
