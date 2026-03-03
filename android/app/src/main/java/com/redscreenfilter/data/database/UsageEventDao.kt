package com.redscreenfilter.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * UsageEvent Data Access Object (DAO)
 * Provides methods for database operations on UsageEvent table
 */
@Dao
interface UsageEventDao {
    
    /**
     * Insert a new usage event
     */
    @Insert
    suspend fun insertEvent(event: UsageEvent)
    
    /**
     * Insert multiple usage events
     */
    @Insert
    suspend fun insertEvents(events: List<UsageEvent>)
    
    /**
     * Get all usage events
     */
    @Query("SELECT * FROM usage_events ORDER BY timestamp DESC")
    suspend fun getAllEvents(): List<UsageEvent>
    
    /**
     * Get all usage events as flow for real-time updates
     */
    @Query("SELECT * FROM usage_events ORDER BY timestamp DESC")
    fun getAllEventsFlow(): Flow<List<UsageEvent>>
    
    /**
     * Get events within a time range
     */
    @Query("SELECT * FROM usage_events WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getEventsByTimeRange(startTime: Long, endTime: Long): List<UsageEvent>
    
    /**
     * Get today's events
     */
    @Query("""
        SELECT * FROM usage_events 
        WHERE timestamp >= :todayStart 
        ORDER BY timestamp DESC
    """)
    suspend fun getTodayEvents(todayStart: Long): List<UsageEvent>
    
    /**
     * Get this week's events
     */
    @Query("""
        SELECT * FROM usage_events 
        WHERE timestamp >= :weekStart 
        ORDER BY timestamp DESC
    """)
    suspend fun getWeekEvents(weekStart: Long): List<UsageEvent>
    
    /**
     * Get this month's events
     */
    @Query("""
        SELECT * FROM usage_events 
        WHERE timestamp >= :monthStart 
        ORDER BY timestamp DESC
    """)
    suspend fun getMonthEvents(monthStart: Long): List<UsageEvent>
    
    /**
     * Count total overlay usage time (when overlayEnabled was true)
     */
    @Query("SELECT COUNT(*) FROM usage_events WHERE overlayEnabled = 1 AND eventType = 'overlay_toggled'")
    suspend fun countTotalActiveEvents(): Int
    
    /**
     * Get average opacity across all events
     */
    @Query("SELECT AVG(opacity) FROM usage_events WHERE opacity > 0")
    suspend fun getAverageOpacity(): Float
    
    /**
     * Get most used preset
     */
    @Query("""
        SELECT preset FROM usage_events 
        WHERE preset != '' 
        GROUP BY preset 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
    """)
    suspend fun getMostUsedPreset(): String?
    
    /**
     * Get events by type
     */
    @Query("SELECT * FROM usage_events WHERE eventType = :eventType ORDER BY timestamp DESC")
    suspend fun getEventsByType(eventType: String): List<UsageEvent>
    
    /**
     * Delete all events
     */
    @Query("DELETE FROM usage_events")
    suspend fun deleteAllEvents()
    
    /**
     * Delete events older than specified timestamp
     */
    @Query("DELETE FROM usage_events WHERE timestamp < :timestamp")
    suspend fun deleteOldEvents(timestamp: Long)
    
    /**
     * Get total number of events
     */
    @Query("SELECT COUNT(*) FROM usage_events")
    suspend fun getEventCount(): Int
}
