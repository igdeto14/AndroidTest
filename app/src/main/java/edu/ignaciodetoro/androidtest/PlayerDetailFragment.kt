package edu.ignaciodetoro.androidtest

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlayerDetailFragment : Fragment() {
    private lateinit var player: Player
    private lateinit var actionBar: ActionBar

    // Get player provided from PlayerAdapter and set up action bar.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT>=33){
            player = requireArguments().getParcelable("player", Player::class.java)!!}
        else {
            @Suppress("DEPRECATION")
            player = requireArguments().getParcelable("player")!!
        }
        setHasOptionsMenu(true)
        actionBar = (activity as AppCompatActivity).supportActionBar!!
        actionBar.title = "Player Details"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var gamesPlayed = "Games played: Loading..."
        var minPlayed = "Minutes played: Loading..."
        var points = "Average points: Loading..."

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_player_detail, container, false)

        // Current season average information
        val ssGames = view.findViewById<TextView>(R.id.season_games)
        val ssPts = view.findViewById<TextView>(R.id.season_points)
        val ssMins = view.findViewById<TextView>(R.id.season_mins)
        ssMins.text = minPlayed
        ssGames.text = gamesPlayed
        ssPts.text = points

        // Create Retrofit client
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.balldontlie.io/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiService::class.java)

        // Make API call to get season averages to fill season average info.
        api.getSeasonAverages(player.id, 2018).enqueue(
            object : Callback<SeasonAverageResponse> {
                override fun onResponse(call: Call<SeasonAverageResponse>, response: Response<SeasonAverageResponse>) {
                    if (response.isSuccessful) {
                        if (response.body()?.data?.size!! >0){
                        val data = response.body()?.data?.get(0) //
                        gamesPlayed = if (data?.games_played.toString() != "0"){
                            "Games played: " + data?.games_played.toString()
                        } else {
                            "Games played: Not Available."
                        }
                        minPlayed = if (data?.min != "0.0"){
                            "Minutes played: " + data?.min
                        } else {
                            "Minutes played: Not Available."
                        }
                        points = if (data?.pts.toString() != "0.0"){
                            "Average points: " + data?.pts.toString()
                        } else {
                            "Average points: Not Available."
                        } } else {
                            gamesPlayed = "Games played: Not Available."
                            minPlayed = "Minutes played: Not Available."
                            points = "Average points: Not Available."
                        }
                        ssMins.text = minPlayed
                        ssGames.text = gamesPlayed
                        ssPts.text = points
                    } else {
                        Log.d("PlayerDetailFragment", "Error getting season averages: ${response.code()}")
                    }
                }

            override fun onFailure(call: Call<SeasonAverageResponse>, t: Throwable) {
                Log.d("PlayerDetailFragment", "Error getting season averages", t)
            }
        })

        // Set up the back arrow in the action bar.
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(androidx.appcompat.R.drawable.abc_ic_ab_back_material)

        // Player information
        val playerNameTextView = view.findViewById<TextView>(R.id.player_name)
        val playerPositionTextView = view.findViewById<TextView>(R.id.player_position)
        val playerHeightTextView = view.findViewById<TextView>(R.id.player_height)
        val playerWeightTextView = view.findViewById<TextView>(R.id.player_weight)
        val fullName = player.first_name+" "+player.last_name
        val posText = if (player.position != ""){
            "Position: "+player.position+"."
        } else {
            "Position: Not Available."
        }
        val hText = if (player.height_feet != null){
            "Height: "+player.height_feet.toString()+" ft."
        } else {
            "Height: Not Available."
        }
        val vText = if (player.weight_pounds != null){
            "Weight: "+player.weight_pounds.toString()+" lbs."
        } else {
            "Weight: Not Available."
        }
        playerNameTextView.text = fullName
        playerPositionTextView.text = posText
        playerHeightTextView.text = hText
        playerWeightTextView.text = vText

        // Team information.
        val teamNameTextView = view.findViewById<TextView>(R.id.team_name)
        val teamAbbreviationTextView = view.findViewById<TextView>(R.id.team_abbreviation)
        val teamCityTextView = view.findViewById<TextView>(R.id.team_city)
        val teamImageView = view.findViewById<ImageView>(R.id.team_image)
        val abv = "Abbreviation: " + player.team.abbreviation
        val city = "City: " + player.team.city

        teamNameTextView.text = player.team.full_name
        teamAbbreviationTextView.text = abv
        teamCityTextView.text = city
        val teamLogoUrl = if (player.team.conference == "West") {
            "https://content.sportslogos.net/logos/6/1001/full/2996.gif"
        } else {
            "https://content.sportslogos.net/logos/6/999/full/2995.gif"
        }
        Glide.with(this)
            .load(teamLogoUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(teamImageView)
        return view
    }

    // Disables back arrow when returning to MainActivity and changes action bar title.
    override fun onDestroyView() {
        super.onDestroyView()
        actionBar.setDisplayHomeAsUpEnabled(false)
        actionBar.title = "NBA Players List"
    }

    // Manages back arrow button to return to MainActivity.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                (activity as AppCompatActivity).supportFragmentManager.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}