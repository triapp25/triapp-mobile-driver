package com.triappdriver.data.repository

import kotlinx.coroutines.delay

interface MapboxSearchRepository {
    //suspend fun searchPlaces(query: String): List<SearchResult>

}

class MapboxSearchRepositoryImpl : MapboxSearchRepository {
    //override suspend fun searchPlaces(query: String): List<SearchResult> {
    //    delay(500) // Simula delay de rede
//
    //    if (query.length < 3) return emptyList()
//
    //    // Retorno fake. Na implementação real, chame sua API do Mapbox aqui:
    //    // https://api.mapbox.com/geocoding/v5/mapbox.places/{query}.json?access_token=YOUR_TOKEN
    //    return listOf(
    //        SearchResult("1", "Av. Paulista, 1578", "Bela Vista, São Paulo", -23.561, -46.656),
    //        SearchResult("2", "Shopping Iguatemi", "Jardim Paulistano", -23.577, -46.688),
    //        SearchResult("3", "Aeroporto de Congonhas", "Vila Congonhas", -23.626, -46.656),
    //        SearchResult("4", "Parque Ibirapuera", "Vila Mariana", -23.587, -46.657),
    //        SearchResult("5", "$query (Resultado Mapa)", "Endereço detalhado...", 0.0, 0.0)
    //    )
    //}
}