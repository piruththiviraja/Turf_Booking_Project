package com.example.Trufbooking.repository;

import com.example.Trufbooking.entity.TurfsRatings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepo extends JpaRepository<TurfsRatings,Integer> {
}
