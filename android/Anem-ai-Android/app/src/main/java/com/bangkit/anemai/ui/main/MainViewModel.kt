package com.bangkit.anemai.ui.main

import androidx.lifecycle.*
import com.bangkit.anemai.data.model.UserIdResponse
import com.bangkit.anemai.data.model.ArticlesResponseItem
import com.bangkit.anemai.data.pref.UserModel
import com.bangkit.anemai.data.repository.ArticleRepository
import com.bangkit.anemai.data.repository.DetectionRepository
import com.bangkit.anemai.data.repository.UserRepository
import com.bangkit.anemai.utils.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.HttpException

class MainViewModel(
    private val detectionRepository: DetectionRepository,
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _userDetail = MutableLiveData<UserIdResponse?>()
    val userDetail: LiveData<UserIdResponse?> get() = _userDetail

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun getSession(): LiveData<UserModel> = userRepository.getSession().asLiveData()

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    // ----------------------------
    // Fetch user detail with retry
    // ----------------------------
    fun getDetailUser(userId: String, retries: Int = 3) {
        viewModelScope.launch {
            var attempt = 0
            while (attempt < retries) {
                try {
                    _userDetail.value = null
                    val response = userRepository.getDetailUser(userId)
                    _userDetail.postValue(response)
                    break
                } catch (e: HttpException) {
                    when (e.code()) {
                        503 -> {
                            attempt++
                            if (attempt < retries) delay(2000)
                            else _errorMessage.postValue("Server busy. Please try again later.")
                        }
                        401 -> {
                            _errorMessage.postValue("Session expired. Please login again.")
                            break
                        }
                        else -> {
                            _errorMessage.postValue("Network error: ${e.code()}")
                            break
                        }
                    }
                } catch (e: Exception) {
                    _errorMessage.postValue("Connection failed. Check your internet.")
                    break
                }
            }
        }
    }

    // ----------------------------
    // Predict anemia
    // ----------------------------
    fun predict(multipart: MultipartBody.Part) = detectionRepository.predictAnemia(multipart)

    // ----------------------------
    // Fetch history
    // ----------------------------
    fun getHistory() = detectionRepository.fetchHistory()

    // ----------------------------
    // Fetch articles with retry
    // ----------------------------
    fun getArticles(retries: Int = 3): LiveData<Result<List<ArticlesResponseItem>>> {
        val liveData = MutableLiveData<Result<List<ArticlesResponseItem>>>()
        liveData.value = Result.Loading

        viewModelScope.launch {
            var attempt = 0
            while (attempt < retries) {
                try {
                    val articles = articleRepository.fetchArticles()
                    liveData.postValue(Result.Success(articles))
                    break
                } catch (e: HttpException) {
                    if (e.code() == 503) {
                        attempt++
                        if (attempt < retries) delay(2000)
                        else {
                            liveData.postValue(Result.Error("Server busy. Try again later."))
                            _errorMessage.postValue("Server busy. Try again later.")
                        }
                    } else {
                        liveData.postValue(Result.Error("Network error: ${e.code()}"))
                        _errorMessage.postValue("Network error: ${e.code()}")
                        break
                    }
                } catch (e: Exception) {
                    liveData.postValue(Result.Error("Connection failed. Check your internet."))
                    _errorMessage.postValue("Connection failed. Check your internet.")
                    break
                }
            }
        }

        return liveData
    }

    // ----------------------------
    // Fetch article by ID
    // ----------------------------
    fun getArticleById(articleId: String): LiveData<Result<ArticlesResponseItem>> {
        val liveData = MutableLiveData<Result<ArticlesResponseItem>>()
        liveData.value = Result.Loading

        viewModelScope.launch {
            try {
                val article = articleRepository.fetchArticleById(articleId)
                liveData.postValue(Result.Success(article))
            } catch (e: HttpException) {
                if (e.code() == 503) {
                    liveData.postValue(Result.Error("Server busy. Please try again later."))
                    _errorMessage.postValue("Server busy. Please try again later.")
                } else {
                    liveData.postValue(Result.Error("Network error: ${e.code()}"))
                    _errorMessage.postValue("Network error: ${e.code()}")
                }
            } catch (e: Exception) {
                liveData.postValue(Result.Error("Connection failed. Check your internet."))
                _errorMessage.postValue("Connection failed. Check your internet.")
            }
        }

        return liveData
    }
}