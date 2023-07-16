package com.example.renewed



import com.example.renewed.models.Listing
import com.example.renewed.models.RedditPostType
import io.reactivex.rxjava3.core.Single

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface API {

    @GET("/r/random.json")//
    fun getRandomSubreddit(): Single<RedditPostType>

    @GET("/r/{subName}/random.json")//
    fun getRandomPost(@Path("subName")subName:String): Single<List<Listing>>

    @GET("r/{subName}/new.json")
    fun getPostsInDateRange(@Path("subName") subName:String,
                            @Query("limit")limit:String="10",
                            @Query("before") before:String?=null,
                            @Query("after") after:String?=null): Single<Listing>





    @GET("r/{subName}/comments.json")
    fun getCommentsInDateRange(@Path("subName") subName:String,
                            @Query("limit")limit:String="100",
                            @Query("before") before:String?=null,
                            @Query("after") after:String?=null): Single<Listing>

    @GET("r/politics/comments/zl1c39/lawmakers_unveil_bipartisan_bill_that_aims_to_ban.json")
    fun getPostWithLotsOfComments(@Query("limit")limit:String="20",
                                @Query("before") before:String?=null,
                                    @Query("after") after:String?=null): Single<List<Listing>>

    @GET("r/{subName}/hot.json")
    fun getHotComments(@Path("subName") subName:String,@Query("limit")limit:String="2",
                                  @Query("before") before:String?=null,
                                  @Query("after") after:String?=null): Single<Listing>


}
