package edu.ignaciodetoro.androidtest

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    // GET request for players.
    @GET("players")
    fun getPlayers(@Query("page") page: Int, @Query("per_page") perPage: Int): Call<PlayerResponse>

    // GET request for season_averages.
    @GET("season_averages")
    fun getSeasonAverages(@Query("player_ids[]") playerId: Int, @Query("season") season: Int): Call<SeasonAverageResponse>
}