package com.example.renewed.repos

import io.reactivex.rxjava3.core.Single

interface BaseRepo {
    fun login(): Single<String>

}
