package com.triappdriver.data.repository

import com.triappdriver.data.MapboxApiService
import com.triappdriver.data.dtos.Route
import com.triappdriver.domain.model.Coordinate
import dev.icerock.moko.geo.LatLng
import kotlinx.coroutines.delay

interface MapboxSearchRepository {
    //suspend fun searchPlaces(query: String): List<SearchResult>
    suspend fun getRoutePolyline(origin: Coordinate, destination: Coordinate): Route?

}


class MapboxSearchRepositoryImpl(
    private val api: MapboxApiService,
    private val apiKey: String
) :MapboxSearchRepository {

    override suspend fun getRoutePolyline(origin: Coordinate, destination: Coordinate): Route? {
        val coordinates = "${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}"

        try {
            val response = api.getDirections(
                coordinates = coordinates,
                accessToken = apiKey
            )

            return response.routes.firstOrNull()
            //?: return emptyList()

            // return PolylineDecoder.decode(polylineEncoded.geometry, 6)

        } catch (e: Exception) {
            println("Erro ao buscar rota do Mapbox: ${e.message}")
            return null
        }
    }
}