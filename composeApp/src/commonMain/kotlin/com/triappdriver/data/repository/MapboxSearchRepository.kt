package com.triappdriver.data.repository

import com.triappdriver.data.MapboxApiService
import com.triappdriver.data.dtos.Route
import com.triappdriver.domain.model.Coordinate
import com.triappdriver.utils.PolylineDecoder
import kotlinx.coroutines.delay

interface MapboxSearchRepository {
    //suspend fun searchPlaces(query: String): List<SearchResult>
    suspend fun getRoutePolyline(origin: Coordinate, destination: Coordinate): Pair<Route?, List<Coordinate>>?

}


class MapboxSearchRepositoryImpl(
    private val api: MapboxApiService,
    private val apiKey: String
) :MapboxSearchRepository {

    override suspend fun getRoutePolyline(origin: Coordinate, destination: Coordinate): Pair<Route?, List<Coordinate>>? {
        val coordinates = "${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}"

        try {
            val response = api.getDirections(
                coordinates = coordinates,
                accessToken = apiKey
            )

            val route = response.routes.firstOrNull()
            //?: return emptyList()

            return route to PolylineDecoder.decode(route?.geometry, 6)

        } catch (e: Exception) {
            println("Erro ao buscar rota do Mapbox: ${e.message}")
            return null
        }
    }
}