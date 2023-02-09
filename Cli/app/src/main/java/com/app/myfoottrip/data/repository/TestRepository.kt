package com.app.myfoottrip.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.paging.TestPagingSource
import com.app.myfoottrip.network.api.TokenApi
import com.app.myfoottrip.network.service.TestService

class TestRepository {
    fun getBoardList() =
        Pager(
            config = PagingConfig(
                pageSize = 2,
                maxSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {TestPagingSource(Application.retrofit.create(TestService::class.java))}
        ).liveData



}