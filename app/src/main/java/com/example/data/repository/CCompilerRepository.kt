package com.example.data.repository

import com.example.data.InitialCurriculum
import com.example.data.dao.ActivityDao
import com.example.data.dao.ChallengeDao
import com.example.data.dao.ProjectDao
import com.example.data.dao.UserStatsDao
import com.example.data.model.ChallengeEntity
import com.example.data.model.DailyActivityEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.UserStatsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CCompilerRepository(
    private val projectDao: ProjectDao,
    private val challengeDao: ChallengeDao,
    private val activityDao: ActivityDao,
    private val userStatsDao: UserStatsDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allChallenges: Flow<List<ChallengeEntity>> = challengeDao.getAllChallenges()
    val completedChallengesCount: Flow<Int> = challengeDao.getCompletedCount()
    val recentActivities: Flow<List<DailyActivityEntity>> = activityDao.getRecentActivities()
    val userStats: Flow<UserStatsEntity?> = userStatsDao.getUserStats()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun initializeDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        // Seed projects (insert any missing default/template projects)
        val existingProjects = projectDao.getAllProjects().firstOrNull() ?: emptyList()
        val existingTitles = existingProjects.map { it.title }.toSet()
        for (p in InitialCurriculum.defaultProjects) {
            if (!existingTitles.contains(p.title)) {
                projectDao.insertProject(p.copy(id = 0))
            }
        }

        // Seed challenges: update with latest curriculum while preserving completion status
        for (c in InitialCurriculum.challenges) {
            val existing = challengeDao.getChallengeById(c.id)
            if (existing == null) {
                challengeDao.insertAll(listOf(c))
            } else {
                challengeDao.updateChallenge(
                    c.copy(
                        isCompleted = existing.isCompleted,
                        userCode = existing.userCode
                    )
                )
            }
        }

        // Seed stats
        val existingStats = userStatsDao.getUserStatsDirect()
        val today = getTodayDateString()
        if (existingStats == null) {
            userStatsDao.insertOrUpdateStats(
                UserStatsEntity(
                    id = 1,
                    currentStreak = 1,
                    longestStreak = 1,
                    lastActiveDate = today,
                    totalXp = 0,
                    totalRuns = 0,
                    totalSolved = 0
                )
            )
            // Seed a today activity record
            activityDao.insertOrUpdateActivity(
                DailyActivityEntity(
                    dateString = today,
                    runCount = 0,
                    challengesCompleted = 0,
                    xpEarned = 0
                )
            )
        }
    }

    suspend fun saveProject(title: String, code: String, id: Int = 0): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (id > 0) {
            projectDao.updateProject(
                ProjectEntity(
                    id = id,
                    title = title,
                    code = code,
                    updatedAt = now
                )
            )
            id.toLong()
        } else {
            projectDao.insertProject(
                ProjectEntity(
                    title = title,
                    code = code,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    suspend fun deleteProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.deleteProject(project)
    }

    suspend fun getProjectById(id: Int): ProjectEntity? = withContext(Dispatchers.IO) {
        projectDao.getProjectById(id)
    }

    suspend fun getChallengeById(id: Int): ChallengeEntity? = withContext(Dispatchers.IO) {
        challengeDao.getChallengeById(id)
    }

    suspend fun updateChallengeDraft(id: Int, userCode: String) = withContext(Dispatchers.IO) {
        val challenge = challengeDao.getChallengeById(id)
        if (challenge != null) {
            challengeDao.updateChallenge(challenge.copy(userCode = userCode))
        }
    }

    suspend fun completeChallenge(id: Int, userCode: String): Int = withContext(Dispatchers.IO) {
        val challenge = challengeDao.getChallengeById(id) ?: return@withContext 0
        val isFirstTime = !challenge.isCompleted
        val xpReward = if (isFirstTime) challenge.xpReward else 5

        challengeDao.updateChallenge(
            challenge.copy(
                isCompleted = true,
                userCode = userCode
            )
        )

        recordActivity(
            isRun = true,
            xpGained = xpReward,
            challengeSolved = isFirstTime
        )

        xpReward
    }

    suspend fun recordCodeRun() = withContext(Dispatchers.IO) {
        recordActivity(isRun = true, xpGained = 5, challengeSolved = false)
    }

    private suspend fun recordActivity(isRun: Boolean, xpGained: Int, challengeSolved: Boolean) {
        val today = getTodayDateString()
        val yesterday = getYesterdayDateString()

        // 1. Update Daily Activity
        val currentActivity = activityDao.getActivityByDate(today) ?: DailyActivityEntity(dateString = today)
        val updatedActivity = currentActivity.copy(
            runCount = currentActivity.runCount + (if (isRun) 1 else 0),
            challengesCompleted = currentActivity.challengesCompleted + (if (challengeSolved) 1 else 0),
            xpEarned = currentActivity.xpEarned + xpGained,
            timestamp = System.currentTimeMillis()
        )
        activityDao.insertOrUpdateActivity(updatedActivity)

        // 2. Update User Stats & Streaks
        val stats = userStatsDao.getUserStatsDirect() ?: UserStatsEntity()
        val newCurrentStreak = when {
            stats.lastActiveDate == today -> stats.currentStreak
            stats.lastActiveDate == yesterday -> stats.currentStreak + 1
            stats.lastActiveDate.isEmpty() -> 1
            else -> 1 // Streak broken
        }
        val newLongestStreak = maxOf(newCurrentStreak, stats.longestStreak)

        val updatedStats = stats.copy(
            currentStreak = newCurrentStreak,
            longestStreak = newLongestStreak,
            lastActiveDate = today,
            totalXp = stats.totalXp + xpGained,
            totalRuns = stats.totalRuns + (if (isRun) 1 else 0),
            totalSolved = stats.totalSolved + (if (challengeSolved) 1 else 0)
        )
        userStatsDao.insertOrUpdateStats(updatedStats)
    }

    private fun getTodayDateString(): String = dateFormat.format(Date())

    private fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return dateFormat.format(cal.time)
    }
}
