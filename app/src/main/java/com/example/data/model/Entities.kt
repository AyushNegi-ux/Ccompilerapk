package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val code: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isTemplate: Boolean = false
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val category: String,
    val difficulty: String,
    val description: String,
    val starterCode: String, // Starter skeleton with instructions - do it yourself
    val solutionCode: String = "", // Complete reference solution (revealed on request)
    val testInputs: String, // newline separated or delimiter
    val expectedOutputs: String, // corresponding expected outputs
    val hints: String,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val userCode: String? = null
)

@Entity(tableName = "daily_activities")
data class DailyActivityEntity(
    @PrimaryKey val dateString: String, // YYYY-MM-DD
    val runCount: Int = 0,
    val challengesCompleted: Int = 0,
    val xpEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val lastActiveDate: String = "",
    val totalXp: Int = 0,
    val totalRuns: Int = 0,
    val totalSolved: Int = 0
)
