package com.example.renewed.di

import com.example.renewed.API
import com.example.renewed.BaseSubredditsAndPostsRepo
import com.example.renewed.LoginAPI
import com.example.renewed.Room.SavedSubredditsDAO
import com.example.renewed.Room.T3DAO
import com.example.renewed.Room.T5DAO
import com.example.renewed.SubredditsAndPostsRepository
import com.example.renewed.moshiadapters.DescriptionAdapter
import com.example.renewed.moshiadapters.RedditHolderAdapter
import com.example.renewed.moshiadapters.RedditPostAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object RedditModule {
    //private const val BASE_URL = "https://www.reddit.com/api/v1/authorize?"
    private const val BASE_URL = "https://www.reddit.com/"


    @Singleton
    @Provides
    fun provideAPICallInterface(retrofit: Retrofit): API =
        retrofit.create(API::class.java)


    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, mosh: Moshi): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(mosh))
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .baseUrl(BASE_URL)
        .build()


    @Singleton
    @Provides
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor,redirect: RedirectInterceptor):
            OkHttpClient = OkHttpClient
        .Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .addInterceptor(httpLoggingInterceptor)
        .addInterceptor(redirect)
        .build()


    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor() = HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

    @Singleton
    @Provides
    fun provideRedirectInterceptor() = RedirectInterceptor()


    @Singleton
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(RedditPostAdapter())
        .add(RedditHolderAdapter())
        .add(DescriptionAdapter())
        .build()


    @Singleton
    @Provides
    fun provideLoginInterface(retrofit: Retrofit): LoginAPI =
        retrofit.create(LoginAPI::class.java)
}


@InstallIn(SingletonComponent::class)
@Module
object RepoModule {

    @Singleton
    @Provides
    fun providePostsRepository(api: API, t5Dao: T5DAO, t3Dao: T3DAO,savedSubredditsDAO: SavedSubredditsDAO):
            BaseSubredditsAndPostsRepo = SubredditsAndPostsRepository(api, t5Dao, t3Dao)

}

