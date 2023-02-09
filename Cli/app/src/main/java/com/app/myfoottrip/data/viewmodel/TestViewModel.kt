package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.app.myfoottrip.data.repository.TestRepository

class TestViewModel :ViewModel() {
    val repository = TestRepository()
    val result = repository.getBoardList().cachedIn(viewModelScope)
}