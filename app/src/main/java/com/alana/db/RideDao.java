package com.alana.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RideDao {
    @Insert
    void insertRide(Ride ride);

    @Query("SELECT * FROM rides ORDER BY id DESC")
    List<Ride> getAllRides();
}
