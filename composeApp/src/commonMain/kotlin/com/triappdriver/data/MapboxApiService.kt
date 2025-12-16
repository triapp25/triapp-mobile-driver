package com.triappdriver.data


import com.triappdriver.data.dtos.MapboxDirectionsDTO
import com.triappdriver.data.dtos.MapboxGeocodeDTO
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface MapboxApiService {

    // https://api.mapbox.com/geocoding/v5/mapbox.places/{query}.json?access_token=YOUR_TOKEN
    @GET("geocoding/v5/mapbox.places/{query}.json")
    suspend fun geocode(
        @Path("query") query: String,
        @Query("access_token") accessToken: String,
        @Query("limit") limit: Int = 10,
        @Query("language") language: String = "pt"
    ): MapboxGeocodeDTO

    @GET("geocoding/v5/mapbox.places/{longitude},{latitude}.json")
    suspend fun reverseGeocode(
        @Path("longitude") longitude: Double,
        @Path("latitude") latitude: Double,
        @Query("access_token") accessToken: String,
        @Query("limit") limit: Int = 1,
        @Query("language") language: String = "pt"
    ): MapboxGeocodeDTO

    @GET("directions/v5/mapbox/driving/{coordinates}")
    suspend fun getDirections(
        @Path("coordinates") coordinates: String,
        @Query("access_token") accessToken: String,
        @Query("geometries") geometries: String = "polyline6",
        @Query("steps") steps: Boolean = false
    ): MapboxDirectionsDTO
}