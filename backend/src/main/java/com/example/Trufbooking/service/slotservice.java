package com.example.Trufbooking.service;

import com.example.Trufbooking.entity.*;
import com.example.Trufbooking.repository.Bookingrepo;
import com.example.Trufbooking.repository.admintable_repo;
import com.example.Trufbooking.repository.slotrepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
// import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
// import jdk.swing.interop.SwingInterOpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class slotservice {
    @Autowired
    slotrepo slotrepo;
    @Autowired
    admintable_repo adminrepo;
    @Autowired
    Bookingrepo bookingRepository;
//    @PostConstruct
//    public void init() {
//        System.out.println("SlotUpdater initialized!");
//        updateDatesWithCurrentDate();
//    }
    public List<slotdto> getAvailableSlots(Integer turfid) {
        List<Object[]> queryResults = slotrepo.findAvailableSlotsByTurfId(turfid);
        List<slotdto> availableSlots = new ArrayList<>();

        // Iterate over query results and populate the SlotDTO objects
        for (Object[] result : queryResults) {
//            Integer turfid = (Integer) result[0];
            String date = (String) result[1];
            String availableTimes = (String) result[2];

            // Split the availableTimes string into a list of strings (assuming times are comma-separated)
            List<String> timesList = List.of(availableTimes.split(","));

            // Create a SlotDTO object and add to the list
            availableSlots.add(new slotdto(date, timesList));
        }

        return availableSlots;
    }
    @Transactional
    public void updateDatesWithCurrentDate() {
        // Get today's date and day after tomorrow's date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


        Calendar cal = Calendar.getInstance();
        Date today = new Date();
        cal.setTime(today);

        // Calculate the day after tomorrow
        cal.add(Calendar.DATE, 7);
        String dayAfterTomorrow = sdf.format(cal.getTime());

        // Create the new JSON object for the day after tomorrow
        String newSlotData = createSlotDataForDate(dayAfterTomorrow);

        // Update the JSON column using the repository
        slotrepo.updateSlotData(newSlotData);
    }

    private String createSlotDataForDate(String date) {
        // Create a new JSON structure for the day after tomorrow
        StringBuilder slotsJson = new StringBuilder();
        slotsJson.append("[");
        for (int i = 0; i < 24; i++) {
            String startTime = String.format("%02d:00", i);
            String endTime = String.format("%02d:00", (i + 1) % 24);
            slotsJson.append(String.format(
                    "{\"time\": \"%s-%s\", \"status\": \"available\"}",
                    startTime, endTime
            ));
            if (i < 23) slotsJson.append(", ");
        }
        slotsJson.append("]");

        // Return the final JSON for the new date
        return String.format("{\"date\": \"%s\", \"slots\": %s}", date, slotsJson.toString());
    }

    @Scheduled(cron = "0 0 0 * * ?")  // This will run at midnight every day
    public void updateTodaysDateAutomatically() {
        System.out.println("Updates Here ??");
        updateDatesWithCurrentDate();
    }

    public  String generateAndSaveSlots(int turfid) {
        Optional<admintable> turfOptional = adminrepo.findById(turfid);
        if (!turfOptional.isPresent()) {
            return "Turf not found!";
        }

        admintable turf = turfOptional.get();
        List<Map<String, Object>> slotsData = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);

            List<Map<String, String>> slots = new ArrayList<>();
            for (int hour = 0; hour < 24; hour++) {
                Map<String, String> slot = new HashMap<>();
                slot.put("time", String.format("%02d:00-%02d:00", hour, (hour + 1) % 24));
                slot.put("status", "available");
                slots.add(slot);
            }

            Map<String, Object> daySlot = new HashMap<>();
            daySlot.put("date", date.toString());
            daySlot.put("slots", slots);
            slotsData.add(daySlot);
        }

        String timeJson = new Gson().toJson(slotsData);

        slot slotDetail = new slot();
        slotDetail.setTurf(turf);
        slotDetail.setTime(timeJson);

        slotrepo.save(slotDetail);
        return "Slots generated and saved successfully!";
    }
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminControlDTO getSlotsForTurf(int turfid) {

        Optional<slot> slotDetailOptional = slotrepo.findByTurfId(turfid);
        if(!slotDetailOptional.isPresent()){
            AdminControlDTO adminControl = new AdminControlDTO();
            List<Map<String,Object>> slotTimings = null;
            List<Booking> bookingDetails = null;
            adminControl.setSlotTimings(slotTimings);
            adminControl.setBookingList(bookingDetails);
            return adminControl;
        }
        else{
            // if the slot is present inside the slot repo
            AdminControlDTO adminControl = new AdminControlDTO();
            slot slotDetails = slotDetailOptional.get();
            String slotTimings = slotDetails.getTime();
            try {
                List<Map<String,Object>> slotConvert = objectMapper.readValue(slotTimings,List.class);
                List<Booking> bookingDetails = bookingRepository.getUserBooking(turfid);
                adminControl.setSlotTimings(slotConvert); // gives slots allocated for that turf
                adminControl.setBookingList(bookingDetails); // gives slots booked by users only
                return adminControl; // contain two lists 1) contains slots of that turf 2) contains slots booked by all the users
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        }


    }



    public boolean confirmSlot(int turfId, String date, String time) {
        System.out.println(time);
        Optional<slot> slotOpt = slotrepo.findByTurfId(turfId); // gets slot on the basis of turfId
        if (slotOpt.isPresent()) { // checks whether slots are allocated for that turf
            slot slot = slotOpt.get(); // if yes get slot of that turf

            try {
                // Assuming the slot's time column contains the JSON data as a String
                String timeJson = slot.getTime(); // slot timing
                List<Map<String, Object>> slotsData = objectMapper.readValue(timeJson, List.class);

                for (Map<String, Object> dateSlot : slotsData) {
                    if (date.equals(dateSlot.get("date"))) {
                        List<Map<String, String>> slotList = (List<Map<String, String>>) dateSlot.get("slots");
                        for (Map<String, String> slotDetails : slotList) {
                            if (time.equals(slotDetails.get("time")) && "available".equals(slotDetails.get("status"))) {
                                // Update the slot's status to "booked"
                                slotDetails.put("status", "booked");

                                // Convert the modified list back to a JSON string
                                String updatedTimeJson = objectMapper.writeValueAsString(slotsData);
                                slot.setTime(updatedTimeJson);  // Update the slot with new JSON
                                slotrepo.save(slot);
                                return true;
                            }
                            else if(time.equals(slotDetails.get("time")) && "booked".equals(slotDetails.get("status"))){
                                System.out.println("Time selected by admin"+ time);
                                slotDetails.put("status","available");
                                String updatedTimeJson = objectMapper.writeValueAsString(slotsData);
                                slot.setTime(updatedTimeJson);  // Update the slot with new JSON
                                slotrepo.save(slot);
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }

//    public boolean confirmSlot(int turfId, String date, String time) {
//        Optional<slot> slotOpt = slotrepo.findByTurfId(turfId);
//        if (slotOpt.isPresent()) {
//            slot slot = slotOpt.get();
//
//            try {
//                // Assuming the slot's time column contains the JSON data as a String
//                String timeJson = slot.getTime();
//                List<Map<String, Object>> slotsData = objectMapper.readValue(timeJson, List.class);
//
//                for (Map<String, Object> dateSlot : slotsData) {
//                    if (date.equals(dateSlot.get("date"))) {
//                        List<Map<String, String>> slotList = (List<Map<String, String>>) dateSlot.get("slots");
//                        for (Map<String, String> slotDetails : slotList) {
//                            if (time.equals(slotDetails.get("time")) && "available".equals(slotDetails.get("status"))) {
//                                // Update the slot's status to "booked"
//                                slotDetails.put("status", "booked");
//
//                                // Convert the modified list back to a JSON string
//                                String updatedTimeJson = objectMapper.writeValueAsString(slotsData);
//                                slot.setTime(updatedTimeJson);  // Update the slot with new JSON
//                                slotrepo.save(slot);
//                                return true;
//                            }
//                            else{
//
//                            }
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return false;
//    }

    public boolean cancelSlot(int turfId, String date, List<String> times) {
        LocalDate currentDate = LocalDate.now();
        LocalDate date1 = LocalDate.parse(date);
        LocalTime currentTime = LocalTime.now();
        String[] newTime = null;
        LocalTime time2 = null;
        List<LocalTime> Timings = new ArrayList<>();
        if(currentDate.isAfter(date1)){
            System.out.println(currentTime);
            return false;
        }
        else if(currentDate.equals(date1) || currentDate.isBefore(date1)) {
            Optional<slot> slotOpt = slotrepo.findByTurfId(turfId);
            if (slotOpt.isPresent()) {
                slot slot = slotOpt.get();
                try {
                    String timeJson = slot.getTime();
                    List<Map<String, Object>> slotsData = objectMapper.readValue(timeJson, List.class);

                    boolean slotUpdated = false;

                    // Iterate through each dateSlot to find the matching date
                    for (Map<String, Object> dateSlot : slotsData) {
                        if (date.equals(dateSlot.get("date"))) {
                            List<Map<String, String>> slotList = (List<Map<String, String>>) dateSlot.get("slots");

                            // Iterate through the time slots to cancel
                            for (String time : times) {
                                for (Map<String, String> slotDetails : slotList) {
                                    if (time.equals(slotDetails.get("time")) && "booked".equals(slotDetails.get("status"))) {
                                        newTime = slotDetails.get("time").split("-");
                                        time2 = LocalTime.parse(newTime[0]);
                                        Timings.add(time2);
                                        if(currentTime.isAfter(time2) && currentDate.equals(date1)){
                                            return false;
                                        }
                                        slotDetails.put("status", "available");
                                        slotUpdated = true;
                                    }

                                }
                            }
                        }
                    }

                    if (slotUpdated) {
                        System.out.println("Timings booked: "+ Timings);
                        String updatedTimeJson = objectMapper.writeValueAsString(slotsData);
                        slot.setTime(updatedTimeJson);
                        slotrepo.save(slot);
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error parsing or updating slot data.");
                }
            }
        }
        return false; // Return false if no updates occurred

}



}