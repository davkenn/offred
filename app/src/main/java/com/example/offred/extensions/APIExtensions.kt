package com.example.offred.extensions


import com.example.offred.exceptions.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException

// Error mapping helper
private fun mapRedditError(error: Throwable): Throwable {
    return if (error is HttpException) {
        when (error.code()) {
            401 -> RedditAuthException("Authentication required")
            403 -> SubredditAccessException("Access forbidden")
        //    404 -> PostNotFoundException("Not found", 404)
            404 -> SubredditNotFoundException(error.message(),"Not found")
            429 -> {
                val retryAfter = error.response()?.headers()?.get("X-Ratelimit-Reset")?.toLongOrNull() ?: 60
                RateLimitException("Rate limited", retryAfter)
            }
            in 500..599 -> RedditServerException("Server error: ${error.code()}")
            else -> UnknownException("Unknown networking error: ${error.code()}", error.code())
        }
    } else error
}

// Extension functions
fun <T : Any> Single<T>.handleRedditErrors(): Single<T> =
    this.onErrorResumeNext { Single.error(mapRedditError(it)) }

fun <T : Any> Observable<T>.handleRedditErrors(): Observable<T> =
    this.onErrorResumeNext { Observable.error(mapRedditError(it)) }

fun Completable.handleRedditErrors(): Completable =
    this.onErrorResumeNext { Completable.error(mapRedditError(it)) }