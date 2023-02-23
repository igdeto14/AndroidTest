package edu.ignaciodetoro.androidtest

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.j256.ormlite.dao.Dao

class MainActivity : AppCompatActivity() {
    // RecyclerView and Database vars
    private lateinit var recyclerView: RecyclerView
    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var dbHelper: PlayerDbHelper
    private lateinit var playerDao: Dao<Player, Int>
    private lateinit var teamDao: Dao<Team, Int>
    private lateinit var pmDao: Dao<PagesMeta, Int>
    // Pagination info vars
    private var currentPage = 1
    private var totalPages = 1
    var pageNumbers = ArrayList<Int>()
    private lateinit var adapterSp: ArrayAdapter<Int>
    private var pagesRead = ArrayList<Int>(listOf(0))
    private val loadTxt = "Loading..."
    private val pageTxt = "Page"

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

        // Screen rotation ignores splash screen
        if (savedInstanceState == null) {
            // In case screen created (no rotation).
            // Showing Splash Screen Fragment.
            val fragment = SplashScreenFragment()
            val fragmentTransaction = (this as AppCompatActivity).supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.recycler_view_container, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }


        // Initialize database and Dao objects.
        dbHelper = PlayerDbHelper(this)
        playerDao = dbHelper.getPlayerDao()
        teamDao = dbHelper.getTeamDao()
        pmDao = dbHelper.getPmDao()

        // Check if database exist, if affirmative fills page info vars.
        val dbPages = pmDao.queryBuilder().orderBy("total_pages", false).queryForFirst()
        if (dbPages!=null) {
            pagesRead = dbPages.pages_read
            pageNumbers = dbPages.pages_spinner
            currentPage = dbPages.current_page
            totalPages = dbPages.total_pages
        } else {
            // Adding first item to Spinner item array in case database is empty.
            pageNumbers.add(1)
        }
        // Establishing the adapter in the Spinner.
        val pageSpinner = findViewById<Spinner>(R.id.spinner)

        // Create ArrayAdapter for the Spinner.
        adapterSp = ArrayAdapter(this, android.R.layout.simple_spinner_item, pageNumbers)
        adapterSp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        pageSpinner.adapter = adapterSp

        // Loading data from API
        pageSpinner.setSelection(currentPage-1)

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

        // Defining previous/next buttons and Page textview.
        val btnPrev = findViewById<ImageButton>(R.id.btnPrev)
        val btnNext = findViewById<ImageButton>(R.id.btnNext)
        val tvPg = findViewById<TextView>(R.id.tvPg)
        tvPg.text = pageTxt
        tintChange(currentPage)
        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                pageSpinner.setSelection(currentPage-1)
            }
        }
        btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                pageSpinner.setSelection(currentPage-1)
            }
        }

    }

    // Load data from API and saving/updating to database.
    private fun loadMoreData() {
        val tvPg = findViewById<TextView>(R.id.tvPg)
        val limit = 25                        // per page players limit.
        val offset = (currentPage-1) * limit // player order offset.
        if (!pagesRead.contains(currentPage)) {
            // Page textview set on "Loading..." until players show in RecyclerView.
            tvPg.text = loadTxt
            tvPg.textSize = 10.0f

            // Create Retrofit client.
            val retrofit = Retrofit.Builder()
                .baseUrl("https://www.balldontlie.io/api/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            // Make API call to get players.
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
                            totalPages = response.body()!!.meta.total_pages
                            adapterSp.notifyDataSetChanged()
                        }

                        // Adding players to database in "order".
                        val playersResponse = response.body()!!
                        playerAdapter.addPlayers(playersResponse.data)
                        for ((i, player) in playersResponse.data.withIndex()) {
                            player.order = offset + i
                            playerDao.createOrUpdate(player)
                            teamDao.createOrUpdate(player.team)
                        }

                        // Set page as read.
                        pagesRead.add(currentPage)

                        // Update buttons states and textview.
                        tvPg.text = pageTxt
                        tvPg.textSize = 18.0f
                        tintChange(currentPage)
                    } else {
                        Log.e("MainActivity", "Error: ${response.message()}")
                    }
                }
                override fun onFailure(call: Call<PlayerResponse>, t: Throwable) {
                    Log.e("MainActivity", "Error: ${t.message}")
                }
            })
        } else{
            // Get players from database
            val dbPlayers = playerDao.queryBuilder()
                .where().between("order", offset, offset+limit-1)
                .query()

            // Update buttons states, players and textview.
            tintChange(currentPage)
            playerAdapter.addPlayers(dbPlayers)
            tvPg.text = pageTxt
            tvPg.textSize = 18.0f
        }

        // Save Page info.
        pmDao.createOrUpdate(PagesMeta(currentPage, totalPages, pagesRead, pageNumbers))
    }

    // Method that disables/enables buttons depending on current page.
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

    // Action bar refresh database button.
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                // Confirmation dialog.
                val builder = AlertDialog.Builder(this)
                    .setMessage("Do you want to refresh the database?")
                    .setPositiveButton("Refresh"){ _: DialogInterface, _: Int ->
                        // Restart database and app.
                        dbHelper.close()
                        this.deleteDatabase(PlayerDbHelper.DATABASE_NAME)
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("Cancel"){ _: DialogInterface, _: Int ->
                    }
                builder.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}