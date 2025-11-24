package com.example.Trufbooking.service;

import com.example.Trufbooking.entity.*;
import com.example.Trufbooking.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.Trufbooking.entity.JsonConverter.objectMapper;

@Slf4j
@Service
public class userservice {

    @Autowired
    private userrepository userrepo;
    @Autowired
    private Userinforepo userInfoRepository;
    @Autowired
    private turfrepo turfRepository;
    @Autowired
    private Userloginauthrepo userloginauth;

    @Autowired
    private RatingRepo ratingRepo;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public userservice(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public usertable registerUser(usertable user) {

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));


        return userrepo.save(user);
    }


    public Optional<usertable> authenticateUser(String email, String password) {
        Optional<usertable> user = userrepo.findByEmail(email);
        if (user.isPresent() && bCryptPasswordEncoder.matches(password, user.get().getPassword())) {
            return user;
        }

        return Optional.empty();
    }

    public boolean authenticate(String email, String password) {
        usertable user = userrepo.findByUsername(email);
        if(!user.getEmail().equals(email)) {
            throw new UsernameNotFoundException("Invalid email");
        }
        if(!user.getPassword().equals(bCryptPasswordEncoder.encode(password))) {
            throw new BadCredentialsException("Invalid password");
        }
        return true;
    }

    public Optional<usertable> findUserByEmail(String email) {

        return userrepo.findByEmail(email); // Replace with the actual implementation
    }

    public String toggleWishlist(String email, Integer turfId) {
        UserInfo user = userInfoRepository.findByEmail(email);
        if (user == null) {
            return "User not found.";
        }
        List<Integer> wishlist = user.getWishlist();
        System.out.println("Users WishList array"+wishlist);
        if (wishlist == null) {
            wishlist = new java.util.ArrayList<>();
        }

        if (wishlist.contains(turfId)) {
            wishlist.remove(turfId);
        } else {
            wishlist.add(turfId);
        }
        System.out.println("Updated wish list array: "+wishlist);
        user.setWishlist(wishlist);
        userInfoRepository.save(user);

        return "Wishlist updated successfully.";
    }
    public List<Integer> getWishlistByEmail(String email) {
        UserInfo user = userInfoRepository.findByEmail(email);
        return user.getWishlist();
    }
    public userdto getUserDetailsByEmail(String email) {
        return userrepo.findUserDetailsByEmail(email);
    }
    public List<admintable> getWishlistDetailsByEmail(String email) {
        UserInfo user = userInfoRepository.findByEmail(email);
        if (user == null || user.getWishlist() == null) {
            return new ArrayList<>();
        }
        System.out.println("After Clicking Wishlist in dashboard"+user.getWishlist());
        List<admintable> turfs = new ArrayList<>();

        // Fetch turf details one by one based on wishlist (which contains turfid)
        for (Integer turfid : user.getWishlist()) {//Integer turfid : user.getWishlist()

            Optional<admintable> turf = turfRepository.findById(turfid);
            System.out.println(turf.get());
            turf.ifPresent(turfs::add);  // Add to the list if the turf exists
        }
        return turfs;

    }
//    public usertable updateUser(String email, String newUsername, long newMobileNumber) {
//        // Use the Optional's ifPresent or orElseThrow to handle null safely
//        usertable user = userloginauth.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
//
//        user.setUsername(newUsername);
//        user.setMobile_number(newMobileNumber);
//
//        return userrepo.save(user);  // Save the updated user object back to the repository
//    }
    public double UserRating(TurfsRatingsDTO userRating){
        Optional<TurfsRatings> turf = ratingRepo.findById(userRating.getTurfId()); // checks whether that turf is already rated by someone
       if(turf.isPresent()){
           double average;
           TurfsRatings existingTurf = turf.get(); // contains the turfRatings outDated
           String userRatings = existingTurf.getUserRatings();
           try {
               Map<String,Integer> ratings = objectMapper.readValue(userRatings,Map.class);
               ratings.put(userRating.getUserEmail(),userRating.getUserRating()); // just update the rating of that user
               double total = 0;
               for(Integer rating: ratings.values()){
                   total = total+ rating;
                   System.out.println(total);
               }
               System.out.println("average");
               average = total/ratings.size();
               existingTurf.setAverage(average);
               System.out.println(average);
               try {
                   String Ratings = objectMapper.writeValueAsString(ratings);
                   existingTurf.setUserRatings(Ratings);
                   ratingRepo.save(existingTurf);
               } catch (JsonProcessingException e) {
                   throw new RuntimeException(e);
               }

               return average;
           } catch (JsonProcessingException e) {
               throw new RuntimeException(e);
           }
       }
       else{
           TurfsRatings newTurf = new TurfsRatings();
           newTurf.setTurfId(userRating.getTurfId());
           Map<String,Integer> userRatings = new HashMap<>();
           userRatings.put(userRating.getUserEmail(),userRating.getUserRating());
           try {
               String Ratings = objectMapper.writeValueAsString(userRatings);
               newTurf.setUserRatings(Ratings);
               double average =  userRating.getUserRating();
               newTurf.setAverage(average);
               ratingRepo.save(newTurf);
               return average;
           } catch (JsonProcessingException e) {
               throw new RuntimeException(e);
           }

       }
    }

    public double showRating(int turfId){
        Optional<TurfsRatings> turfRating = ratingRepo.findById(turfId);
        if(turfRating.isPresent()){
            TurfsRatings rating = turfRating.get();
            return rating.getAverage();
        }
        return 0;
    }

}

