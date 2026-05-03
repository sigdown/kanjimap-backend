package com.example.domain.repository

import com.example.domain.model.UserKanjiProgress
import com.example.domain.model.UserWordProgress
import java.time.Instant

interface ProgressRepository {
    suspend fun getWordProgress(userId: Long, wordId: Long): UserWordProgress?
    suspend fun getKanjiProgress(userId: Long, kanjiId: Long): UserKanjiProgress?
    suspend fun saveWordProgress(progress: UserWordProgress)
    suspend fun saveKanjiProgress(progress: UserKanjiProgress)
    suspend fun getWordsForReview(userId: Long, now: Instant): List<UserWordProgress>
    suspend fun getKanjisForReview(userId: Long, now: Instant): List<UserKanjiProgress>
}
