package edu.ignaciodetoro.androidtest

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import java.sql.SQLException

class PlayerDbHelper(context: Context) : OrmLiteSqliteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Database name and version definition.
    companion object {
        const val DATABASE_NAME = "players.db"
        private const val DATABASE_VERSION = 1
    }

    // Definition of Dao objects that will perform CRUD operations.
    private var playerDao: Dao<Player, Int>? = null
    private var teamDao: Dao<Team, Int>? = null
    private var pmDao: Dao<PagesMeta, Int>? = null

    // Creation of the tables on create.
    override fun onCreate(database: SQLiteDatabase?, connectionSource: ConnectionSource?) {
        try {
            TableUtils.createTable(connectionSource, Player::class.java)
            TableUtils.createTable(connectionSource, Team::class.java)
            TableUtils.createTable(connectionSource, PagesMeta::class.java)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    // Dropping and creation of the tables on upgrade.
    override fun onUpgrade(database: SQLiteDatabase?, connectionSource: ConnectionSource?, oldVersion: Int, newVersion: Int) {
        try {
            TableUtils.dropTable<Player, Any>(connectionSource, Player::class.java, true)
            TableUtils.dropTable<Team, Any>(connectionSource, Team::class.java, true)
            TableUtils.dropTable<PagesMeta, Any>(connectionSource, PagesMeta::class.java, true)
            onCreate(database, connectionSource)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    // Method that returns Dao object corresponding to players table.
    fun getPlayerDao(): Dao<Player, Int> {
        if (playerDao == null) {
            playerDao = DaoManager.createDao(connectionSource, Player::class.java)
        }
        return playerDao as Dao<Player, Int>
    }

    // Method that returns Dao object corresponding to teams table.
    fun getTeamDao(): Dao<Team, Int> {
        if (teamDao == null) {
            teamDao = DaoManager.createDao(connectionSource, Team::class.java)
        }
        return teamDao as Dao<Team, Int>
    }

    // Method that returns Dao object corresponding to pages_meta table.
    fun getPmDao(): Dao<PagesMeta, Int> {
        if (pmDao == null) {
            pmDao = DaoManager.createDao(connectionSource, PagesMeta::class.java)
        }
        return pmDao as Dao<PagesMeta, Int>
    }
}