package com.example.Trufbooking.controller;


import com.example.Trufbooking.entity.AdminControlDTO;
import com.example.Trufbooking.entity.admintable;
import com.example.Trufbooking.entity.turfowner;
import com.example.Trufbooking.repository.turfownerRepo;
import com.example.Trufbooking.service.adminservice;
import com.example.Trufbooking.service.slotservice;
import com.example.Trufbooking.service.turfownerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "https://turfbookingsystem-xi.vercel.app")
//@CrossOrigin(origins = "http://localhost:5173")
public class turfownerController {
    @Autowired
    turfownerService turfser;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    turfownerRepo turfownrepo;

    @Autowired
    adminservice adminser;

    @Autowired
    slotservice slotser;
    @PostMapping("/signup")
    public ResponseEntity<turfowner> registerAdmin(@RequestBody turfowner admin) {
        String rawPassword = admin.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Raw Password: " + rawPassword);
        System.out.println("Encoded Password: " + encodedPassword);
        admin.setPassword(encodedPassword);
        turfowner result = turfser.registerAdmin(admin);
        return ResponseEntity.status(201).body(result);
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginAdmin(@RequestBody turfowner admin) {
        boolean isValid = turfser.verifyAdmin(admin.getEmail(), admin.getPassword());

        if (isValid) {
            turfowner existingAdmin = turfownrepo.findByEmail(admin.getEmail());  //SQL Query select command will run
            int adminId = existingAdmin.getAdmin_id();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful!");
            response.put("adminId", adminId);

            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Invalid username or password!");
            response.put("adminId", null);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/addturf")
    public ResponseEntity<String> addTurf(@RequestParam("admin_id") int admin_id,
                                          @RequestParam("turfname") String turfname,
                                          @RequestParam("location") String location,
                                          @RequestParam("mobilenumber") long mobilenumber,
                                          @RequestParam("price") double price,
                                          @RequestParam("sports") String sportsJSON,
                                          @RequestParam("length") double length,
                                          @RequestParam("breadth") double breadth,
                                          @RequestParam(value = "image", required = false) MultipartFile image) {
        turfowner admin = turfownrepo.findById(admin_id).orElse(null);
        if (admin == null) {
            return ResponseEntity.status(400).body("Admin with the provided turfid not found");
        }
        admintable turfDetails = new admintable();
        turfDetails.setAdmin(admin);
        turfDetails.setTurfname(turfname);
        turfDetails.setLocation(location);
        turfDetails.setMobilenumber(mobilenumber);
        turfDetails.setPrice(price);
        ObjectMapper mapper = new ObjectMapper();
        List<String> sports = null;
        System.out.println(sportsJSON);
        try {
            sports = mapper.readValue(sportsJSON, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        //turfDetails.setSports(sports);
        System.out.println("Controller Side: "+sports);
        turfDetails.setSports(sports);
        turfDetails.setLength(length);
        turfDetails.setBreadth(breadth);

        if (image != null) {
            try {
                turfDetails.setImageData(image.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean isTurfAdded = adminser.addTurf(turfDetails);
        if (isTurfAdded) {
            return ResponseEntity.ok("Turf added successfully");
        } else {
            return ResponseEntity.status(500).body("Failed to add turf");
        }
    }

    @GetMapping("/getTurfsByAdmin")
    public ResponseEntity<List<admintable>> getTurfsByAdmin(@RequestParam("adminId") int admin_id) {
        List<admintable> turfs = adminser.getTurfsByAdmin(admin_id);
        return ResponseEntity.ok(turfs);
    }

    @GetMapping("/getTurfById")
    public ResponseEntity<admintable> getTurfById(@RequestParam int turfid) {
        Optional<admintable> turf = adminser.getTurfById(turfid);
        return turf.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update turf details by ID
    @PutMapping("/updateTurf")
    public ResponseEntity<String> updateTurf(@RequestParam int turfid, @RequestBody admintable updatedTurf) {
        System.out.println("turfid: "+ turfid );
        boolean isUpdated = adminser.updateTurf(turfid, updatedTurf);
        if (isUpdated) {
            return ResponseEntity.ok("Turf updated successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to update turf. Turf ID not found.");
        }
    }



    @PostMapping("/{turfid}")
    public String generateAndSaveSlots(@PathVariable int turfid) {
        System.out.println(turfid);
        return slotser.generateAndSaveSlots(turfid);
    }

    @GetMapping("/{turfid}")
    public AdminControlDTO getSlotsForTurf(@PathVariable int turfid) {
        System.out.println(slotser.getSlotsForTurf(turfid).getSlotTimings());
        return slotser.getSlotsForTurf(turfid);
    }


    @PutMapping("/{turfId}")
    public ResponseEntity<String> confirmSlot(
            @PathVariable("turfId") int turfId,
            @RequestParam("date") String date,
            @RequestParam("time") String time) {
        try {
            boolean isUpdated = slotser.confirmSlot(turfId, date, time);
            if (isUpdated) {
                return ResponseEntity.ok("Slot successfully confirmed");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error confirming slot");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request");
        }
    }

    @PutMapping("/cancel/{turfId}")
    public ResponseEntity<Map<String, Object>> cancelSlot(@PathVariable("turfId") int turfId,
                                                          @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            String date = (String) requestBody.get("date");
            List<String> times = (List<String>) requestBody.get("time");

            boolean isUpdated = slotser.cancelSlot(turfId, date, times);
            if (isUpdated) {
                response.put("success", true);
                response.put("message", "Slots successfully cancelled");

            } else {
                response.put("success", false);
                response.put("message", "Error cancelling slots or slots already available");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @DeleteMapping("/deleteTurf")
    public ResponseEntity<String> deleteTurf(@RequestParam int turfid) {
        try {
            boolean isDeleted = adminser.deleteTurf(turfid);
            if (isDeleted) {
                return ResponseEntity.ok("Turf deleted successfully");
            } else {
                return ResponseEntity.status(400).body("Turf cannot be deleted as it contains booked slots");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete turf");
        }
    }


}
