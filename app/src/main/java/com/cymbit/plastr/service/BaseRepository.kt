package com.cymbit.plastr.service

import retrofit2.Response
import java.io.IOException

open class BaseRepository{

    suspend fun <T: Any> safeApiResult(call: suspend ()-> Response<T>, errorMessage: String) : Result<T>{
        val response = call.invoke()
        if(response.isSuccessful) return Result.Success(response.body()!!)
        return Result.Error(IOException(errorMessage))
    }


    sealed class Result<out T: Any> {
        data class Success<out T : Any>(val data: T) : Result<T>()
        data class Error(val exception: Exception) : Result<Nothing>()
    }
}