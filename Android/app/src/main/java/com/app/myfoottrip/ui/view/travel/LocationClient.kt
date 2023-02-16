package com.app.myfoottrip.ui.view.travel

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {

    fun getLocationUpdates(interval: Long): Flow<Location>

    class LocationException(message: String) : java.lang.Exception()
} // End of LocationClient interface
