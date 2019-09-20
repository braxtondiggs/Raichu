package com.cymbit.plastr.service

import android.util.Log
import com.cymbit.plastr.BuildConfig
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

open class BaseRepository {

    suspend fun <T : Any> safeApiResult(call: suspend () -> Response<T>): Result<T> {
        try {
            val response = call.invoke()
            if (BuildConfig.DEBUG) Log.v("PLASTR", response.raw().request().url().toString())
            if (response.isSuccessful) response.body()?.let { return Result.Success(it) }
            return Result.Error(response.raw().message())
        } catch (e: IOException) {
            return if (e is SocketTimeoutException) {
                Result.Error("timeout")
            } else {
                Result.Error(e.message)
            }
        }
    }


    sealed class Result<out T : Any> {
        data class Success<out T : Any>(val data: T) : Result<T>()
        data class Error(val exception: String?) : Result<Nothing>()
    }
}