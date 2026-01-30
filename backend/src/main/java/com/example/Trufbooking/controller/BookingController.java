package com.example.Trufbooking.controller;

import com.example.Trufbooking.entity.Booking;
import com.example.Trufbooking.repository.Bookingrepo;
import com.example.Trufbooking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    @Autowired
    private Bookingrepo bookingRepository;

    @PostMapping("/add")
    public String addBooking(@RequestBody Booking booking) {
        System.out.println("Here is where the slot we booked gets added in postmapping");
        System.out.println(booking.getTurfName());
        bookingRepository.save(booking); // Save the booking object directly
        return "Booking added successfully!";
    }
    @GetMapping("/{email}")
    public List<Booking> getBookingsByEmail(@PathVariable String email) {
        // System.out.println("My guess: "+ bookingRepository.findByEmail(email).getFirst().getBooking_id());
        return bookingRepository.findByEmail(email);
    }
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> deleteBooking(@PathVariable("bookingId") int booking_id) {
        try {
            // Attempt to delete the booking by its ID
            bookingRepository.deleteById(booking_id);
            return ResponseEntity.ok("Booking successfully deleted");
        } catch (EmptyResultDataAccessException e) {
            // Handle case where booking ID does not exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking ID not found");
        } catch (Exception e) {
            // Handle generic errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while deleting booking: " + e.getMessage());
        }
    }

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookingdetails")
    public List<Map<String, Object>> getBookingDetails(@RequestParam("adminId") int adminId) {
        return bookingService.getBookingDetails(adminId);
    }
    @GetMapping("/getBookingsById")
    public Map<String,Object> getBookings(@RequestParam("bookingId") int bookingId){
        System.out.println("Frontend is calling this controller");
        System.out.println("Booking Id is"+ bookingId);
        return bookingService.getBooking(bookingId);
    }

    @PutMapping("/updateBookingById")
    public void updateBookingTicket(@RequestBody Booking updatedTicket){
        //System.out.println(removedTime.get(0));
        System.out.println(updatedTicket.getTurfid());
        bookingService.updateTicket(updatedTicket);
    }




}
