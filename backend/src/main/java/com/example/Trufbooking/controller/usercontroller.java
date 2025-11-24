package com.example.Trufbooking.controller;

import com.example.Trufbooking.entity.*;
import com.example.Trufbooking.repository.Userinforepo;
import com.example.Trufbooking.repository.userrepository;
import com.example.Trufbooking.service.userservice;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.*;
@CrossOrigin(origins = "http://localhost:5173")
//@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/home")
public class usercontroller {
    @Autowired
    userservice userser;
    @Autowired
    Userinforepo userInfoRepository;
    @Autowired
    userrepository userrepo;
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody usertable user) {
        try {
            usertable result = userser.registerUser(user);

            String email = result.getEmail();
            System.out.println("Email from result: " + email);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(400).body("Email is missing or invalid.");
            }

            UserInfo userInfo = new UserInfo();
            userInfo.setEmail(email);

            userInfoRepository.save(userInfo);

            return ResponseEntity.status(201).body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }



    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> requestMap) {
        String email = requestMap.get("email");
        String password = requestMap.get("password");
        Optional<usertable> user = userser.authenticateUser(email, password);
        if (user.isPresent()) {
            return ResponseEntity.ok("Login Successful");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
    }

    public ResponseEntity<String> login(@RequestBody usertable loginRequest, HttpSession session){
        try{
            boolean isAuthenticated = userser.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
            if(isAuthenticated){
                session.setAttribute("user", loginRequest.getEmail());
                return ResponseEntity.ok("Login Successful");
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
            }
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }
    @PostMapping("/toggle")
    public ResponseEntity<String> toggleWishlist(
            @RequestParam String email,
            @RequestParam Integer turfId
    ) {
        System.out.println("Email from frontend:  "+email);
        System.out.println("Turf id from frontend: "+ turfId);
        String result = userser.toggleWishlist(email, turfId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/wishlist")
    public List<Integer> getWishlist(@RequestParam String email) {
        System.out.println("Adding Wishlist");
        return userser.getWishlistByEmail(email);
    }
    @GetMapping("/user/{email}")
    public userdto getUserDetails(@PathVariable String email) {
        userdto userDetails = userser.getUserDetailsByEmail(email);
        System.out.println("Fetched User Details: " + userDetails);
        return userDetails;
    }
    @GetMapping("/wishlist/{email}")
    public ResponseEntity<List<Map<String, Object>>> getWishlistbyturfid(@PathVariable String email) { //
        // Fetch wishlist details by email
        //List<admintable>
        List<admintable> wishlistDetails = userser.getWishlistDetailsByEmail(email);
        System.out.println(wishlistDetails);
        List<Map<String, Object>> wishlistWithImages = new ArrayList<>();

        for (admintable turf : wishlistDetails) {
            Map<String, Object> turfMap = new HashMap<>();
            turfMap.put("turfid", turf.getTurfid());
            turfMap.put("turfname", turf.getTurfname());
            turfMap.put("location", turf.getLocation());
            turfMap.put("mobilenumber", turf.getMobilenumber());
            turfMap.put("price", turf.getPrice());
            turfMap.put("sports", turf.getSports());
            turfMap.put("length", turf.getLength());
            turfMap.put("breadth", turf.getBreadth());

            try {
                // Check if image exists and convert it to base64 string
                if (turf.getImage() != null) {
                    byte[] imageBytes = turf.getImage().getBytes(1, (int) turf.getImage().length());
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    turfMap.put("image", base64Image);
                } else {
                    turfMap.put("image", null); // If no image, set to null
                }
            } catch (SQLException e) {
                e.printStackTrace();
                turfMap.put("image", null); // Handle image as null in case of error
            }

            // Add the current turf map to the list
            wishlistWithImages.add(turfMap);
        }

        return ResponseEntity.ok(wishlistWithImages);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateUserDetails(
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("mobileNumber") long mobileNumber) {

        return userrepo.findById(email).map(user -> {
            user.setUsername(username);
            user.setMobile_number(mobileNumber);
            userrepo.save(user);
            return ResponseEntity.ok("User details updated successfully");
        }).orElse(ResponseEntity.badRequest().body("User not found"));
    }
    @PostMapping("/uploadImage")
    public ResponseEntity<String> uploadImage(
            @RequestParam("email") String email,
            @RequestParam("image") MultipartFile imageFile) {

        return userInfoRepository.findById(email).map(userInfo -> {
            try {
                userInfo.setImage(imageFile.getBytes());
                userInfoRepository.save(userInfo);
                return ResponseEntity.ok("Image uploaded successfully");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Failed to upload image: " + e.getMessage());
            }
        }).orElse(ResponseEntity.badRequest().body("UserInfo not found"));
    }
    @GetMapping("/user/image/{email}")
    public ResponseEntity<Map<String, String>> getUserImage(@PathVariable String email) {
        return userInfoRepository.findById(email).map(userInfo -> {
            try {
                if (userInfo.getImage() != null && userInfo.getImage().length > 0) {
                    // Encode the image to Base64
                    String base64Image = Base64.getEncoder().encodeToString(userInfo.getImage());
                    Map<String, String> response = new HashMap<>();
                    response.put("image", base64Image);
                    return ResponseEntity.ok(response);
                } else {
                    // Return null for missing image
                    Map<String, String> response = new HashMap<>();
                    response.put("image", null);
                    return ResponseEntity.ok(response);
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "Failed to retrieve image: " + e.getMessage()));
            }
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", "User not found")));
    }

    @PostMapping("/ratings")
    public double setUserRatings (@RequestBody TurfsRatingsDTO userRating){
        return userser.UserRating(userRating);
    }

}


