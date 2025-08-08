package com.example.offred.exceptions

import java.io.IOException


sealed class RedditApiException(
    message: String,
    val code: Int? = null,
    cause: Throwable? = null
) : IOException(message, cause)

// Network-related exceptions
class NetworkException(
    message: String,
    cause: Throwable? = null
) : RedditApiException(message, cause = cause)

class NoInternetException(
    message: String = "No internet connection",
    cause: Throwable? = null
) : RedditApiException(message, cause = cause)

class NetworkTimeoutException(
    message: String = "Request timed out",
    cause: Throwable? = null
) : RedditApiException(message, cause = cause)

// Authentication exceptions
class RedditAuthException(
    message: String = "Authentication required"
) : RedditApiException(message, code = 401)

// Reddit-specific exceptions
class SubredditNotFoundException(
    val subredditName: String? = null,
    message: String = "Subreddit not found"
) : RedditApiException(message, code = 404)

class PostNotFoundException(
    val postId: String? = null,
    message: String = "Post not found"
) : RedditApiException(message, code = 404)

class SubredditAccessException(
    val reason: String? = null,
    message: String = "Cannot access this subreddit"
) : RedditApiException(message, code = 403)

// Rate limiting
class RateLimitException(
    message: String = "Rate limit exceeded",
    val retryAfterSeconds: Long
) : RedditApiException(message, code = 429)

// Server errors
class RedditServerException(
    message: String = "Reddit server error"
) : RedditApiException(message, code = 500)

class UnknownException(
    message: String = "Unknown Network Exception",
    code:Int?
) : RedditApiException(message,code = code)

class RedditMaintenanceException(
    message: String = "Reddit is temporarily unavailable"
) : RedditApiException(message, code = 503)