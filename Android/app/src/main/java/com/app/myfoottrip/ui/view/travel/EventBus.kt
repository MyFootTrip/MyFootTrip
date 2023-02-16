package com.app.myfoottrip.ui.view.travel

import kotlinx.coroutines.flow.*

object EventBus {
    private val events = MutableSharedFlow<Any>(replay = 0)
    val mutableEvents = events.asSharedFlow()

    suspend fun post(event : Any) {
        events.emit(event)
    }

    inline fun <reified T> subscribe(): Flow<T> {
        return mutableEvents.filter { it is T }.map { it as T }
    }
} // End of EventBus object