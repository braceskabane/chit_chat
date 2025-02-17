package com.dicoding.picodiploma.loginwithanimation.view.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.response.LoginResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val _loginResponse = MutableLiveData<LoginResponse>()
    val loginResponse: LiveData<LoginResponse> get() = _loginResponse

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    val passwordError: MutableLiveData<String?> = MutableLiveData()
    val emailError: MutableLiveData<String?> = MutableLiveData()

    private val _isDataValid = MutableLiveData<Boolean>()
    val isDataValid: LiveData<Boolean> get() = _isDataValid

    private var currentEmail: String? = null
    private var currentPassword: String? = null


    fun validatePassword(password: String) {
        when {
            password.length < 8 -> {
                passwordError.value = "Password tidak boleh kurang dari 8 karakter"
            }
            !password.any { it.isDigit() } -> {
                passwordError.value = "Password harus mengandung setidaknya satu angka"
            }
            !password.any { it.isLetterOrDigit().not() } -> {
                passwordError.value = "Password harus mengandung setidaknya satu karakter unik"
            }
            else -> {
                passwordError.value = null
            }
        }
    }

    fun validateEmail(email: String) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.value = "Email tidak valid"
        } else {
            emailError.value = null
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
            Log.d("LoginViewModel", "Token saved: ${user.accessToken}")
        }
    }
    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Sending data: Email=$email, Password=$password")
                val response = repository.login(email, password)
                _isLoading.value = false
//                Log.d("LoginViewModel", "Response received: ${Gson().toJson(response)}")

                if (response.user != null) {
                    _loginResponse.value = response
                    Log.d("LoginViewModel", "Login success: ${response.message}")
                } else if (response.error != null) {
                    val errorMessage = response.error.message ?: "Unknown error"
                    _error.value = errorMessage
                    Log.e("LoginViewModel", "Login error: $errorMessage")
                }
            } catch (e: HttpException) {
                _isLoading.value = false
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, LoginResponse::class.java)
                val errorMessage = errorBody?.error?.message ?: "Network error"
                _error.value = errorMessage
                Log.e("LoginViewModel", "Network error: $errorMessage")
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMessage = "Network error: ${e.message}"
                _error.value = errorMessage
                Log.e("LoginViewModel", "Network error: $errorMessage")
            }
        }
    }
    fun refreshTokenAndRetry(apiCall: suspend (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.performApiCallWithTokenRefresh(apiCall)
                currentEmail?.let { email ->
                    currentPassword?.let { password ->
                        login(email, password)
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}