package edu.ignaciodetoro.androidtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.j256.ormlite.dao.Dao

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var playerAdapter: PlayerAdapter
    private var currentPage = 1
    private var totalPages = 1
    private lateinit var playerDao: Dao<Player, Int>
    private lateinit var teamDao: Dao<Team, Int>
    private val loadTxt = "Loading..."
    private val pageTxt = "Page"
    var pageNumbers = ArrayList<Int>()
    private lateinit var adapterSp: ArrayAdapter<Int>
    private lateinit var playersTemp: ArrayList<Player>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize RecyclerView and Adapter
        recyclerView = findViewById(R.id.recycler_view)
        playerAdapter = PlayerAdapter()

        // Associating Adapter to RecyclerView
        recyclerView.adapter = playerAdapter

        // Associating LinearLayoutManager to RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize database and Dao objects.
        val dbHelper = PlayerDbHelper(this)
        playerDao = dbHelper.getPlayerDao()
        teamDao = dbHelper.getTeamDao()

        // Screen rotation
        @Suppress("DEPRECATION")
        if (savedInstanceState != null) {
            // Retrieve data before rotation.
            currentPage = savedInstanceState.getInt("currentPage")
            totalPages = savedInstanceState.getInt("totalPages")
            pageNumbers = savedInstanceState.getIntegerArrayList("pageNumbers") as ArrayList<Int>
        } else {
            // In case screen created (no rotation).
            // Showing Splash Screen Fragment.
            val fragment = SplashScreenFragment()
            val fragmentTransaction = (this as AppCompatActivity).supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.recycler_view_container, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            // Adding first item to the Spinner item array.
            pageNumbers.add(1)
        }

        // Loading data from API
        loadMoreData()

        // Create ArrayAdapter for the Spinner.
        adapterSp = ArrayAdapter(this, android.R.layout.simple_spinner_item, pageNumbers)
        adapterSp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Establishing the adapter in the Spinner.
        val pageSpinner = findViewById<Spinner>(R.id.spinner)
        pageSpinner.adapter = adapterSp

        // If we choose an item from the Spinner, its corresponding page will be loaded.
        pageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentPage = position + 1
                loadMoreData()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Nothing
            }
        }

        // Defining previous/next buttons.
        val btnPrev = findViewById<ImageButton>(R.id.btnPrev)
        val btnNext = findViewById<ImageButton>(R.id.btnNext)
        val tvPg = findViewById<TextView>(R.id.tvPg)
        tvPg.text = pageTxt
        tintChange(currentPage)
        btnPrev.setOnClickListener {
            tvPg.text = loadTxt
            if (currentPage > 1) {
                currentPage--
                pageSpinner.setSelection(currentPage-1)
            }
        }
        btnNext.setOnClickListener {
            tvPg.text = loadTxt
            if (currentPage < totalPages) {
                currentPage++
                pageSpinner.setSelection(currentPage-1)
            }
        }

    }

    // Load data from API and saving/updating to database.
    private fun loadMoreData() {
            val tvPg = findViewById<TextView>(R.id.tvPg)

            val limit = 25
            tvPg.text = loadTxt
            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.balldontlie.io/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)
            service.getPlayers(currentPage, limit).enqueue(object : Callback<PlayerResponse> {
                override fun onResponse(
                    call: Call<PlayerResponse>,
                    response: Response<PlayerResponse>
                ) {
                    if (response.isSuccessful) {
                        // Updating Spinner items only on the first API request.
                        if (totalPages == 1) {
                            for (i in 2..response.body()!!.meta.total_pages) {
                                pageNumbers.add(i)
                            }
                            adapterSp.notifyDataSetChanged()
                        }

                        // Adding players to database.
                        val playersResponse = response.body()!!
                        totalPages = playersResponse.meta.total_pages
                        playersTemp = playersResponse.data as ArrayList<Player>
                        playerAdapter.addPlayers(playersResponse.data)
                        for (player in playersResponse.data) {
                            playerDao.createOrUpdate(player)
                            teamDao.createOrUpdate(player.team)
                        }

                        // Update buttons states and textview.
                        tvPg.text = pageTxt
                        tintChange(currentPage)


                    } else {
                        Log.e("MainActivity", "Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<PlayerResponse>, t: Throwable) {
                    Log.e("MainActivity", "Error: ${t.message}")
                }
            })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentPage", currentPage)
        outState.putInt("totalPages", totalPages)
        outState.putIntegerArrayList("pageNumbers", pageNumbers)
        outState.putParcelableArrayList("playersTemp", playersTemp)
    }

    private fun tintChange(currentPage: Int){
        val btnPrev = findViewById<ImageButton>(R.id.btnPrev)
        val btnNext = findViewById<ImageButton>(R.id.btnNext)
        val typedValue = TypedValue()
        val theme = theme
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorSecondary,
            typedValue,
            true
        )
        val color = typedValue.data
        btnPrev.isEnabled = currentPage!=1
        btnNext.isEnabled = currentPage!=totalPages
        when (currentPage) {
            1 -> {
                btnPrev.isEnabled = false
                btnPrev.setColorFilter(R.color.grey_300)
                btnNext.isEnabled = true
                btnNext.setColorFilter(color)
            }
            totalPages -> {
                btnNext.isEnabled = false
                btnNext.setColorFilter(R.color.grey_300)
                btnPrev.isEnabled = true
                btnPrev.setColorFilter(color)
            }
            else -> {
                btnNext.isEnabled = true
                btnNext.setColorFilter(color)
                btnPrev.isEnabled = true
                btnPrev.setColorFilter(color)
            }
        }
    }
}