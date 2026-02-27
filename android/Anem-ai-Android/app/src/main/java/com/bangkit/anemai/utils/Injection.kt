package com.bangkit.anemai.utils

import android.app.Application
import android.content.Context
import com.bangkit.anemai.data.pref.UserPreference
import com.bangkit.anemai.data.pref.dataStore
import com.bangkit.anemai.data.repository.ArticleRepository
import com.bangkit.anemai.data.repository.DetectionRepository
import com.bangkit.anemai.data.repository.UserRepository
import com.bangkit.anemai.data.sevice.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideDetectionRepository(context: Context, application: Application): DetectionRepository {
        val userPreference = UserPreference.getInstance(context.dataStore)
        return DetectionRepository.getInstance(application, userPreference)
    }

    fun provideRepository(context: Context): UserRepository {
        val userPreference = UserPreference.getInstance(context.dataStore)
        return runBlocking {
            UserRepository.getInstance(userPreference)
        }
    }

    fun provideArticleRepository(context: Context, application: Application): ArticleRepository {
        val userPreference = UserPreference.getInstance(context.dataStore)
        val token = runBlocking { userPreference.getSession().first().token }
        val apiService = ApiConfig.getApiService(token)
        return runBlocking {
            ArticleRepository.getInstance(application, apiService)
        }
    }
}