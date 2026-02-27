package com.bangkit.anemai.data.repository

import android.app.Application
import com.bangkit.anemai.data.model.ArticlesResponseItem
import com.bangkit.anemai.data.sevice.ApiService
import org.json.JSONObject

class ArticleRepository(
    private val application: Application,
    private val apiService: ApiService
) {

    suspend fun fetchArticles(): List<ArticlesResponseItem> {
        val response = apiService.getArticles()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            val errorBody = response.errorBody()?.string()
            val jsonObject = errorBody?.let { JSONObject(it) }
            throw Exception(jsonObject?.getString("error") ?: "Unknown Error")
        }
    }

    suspend fun fetchArticleById(id: String): ArticlesResponseItem {
        val response = apiService.getArticleById(id)
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty response")
            return ArticlesResponseItem(
                body.sourceUrl,
                body.createdAt,
                body.imageUrl,
                body.description,
                body.id,
                body.title,
                body.content
            )
        } else {
            val errorBody = response.errorBody()?.string()
            val jsonObject = errorBody?.let { JSONObject(it) }
            throw Exception(jsonObject?.getString("error") ?: "Unknown Error")
        }
    }

    companion object {
        @Volatile
        private var instance: ArticleRepository? = null

        suspend fun getInstance(application: Application, apiService: ApiService): ArticleRepository {
            return instance ?: synchronized(this) {
                ArticleRepository(application, apiService).also { instance = it }
            }
        }
    }
}