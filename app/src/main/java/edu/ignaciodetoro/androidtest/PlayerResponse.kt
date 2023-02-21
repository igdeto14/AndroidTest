package edu.ignaciodetoro.androidtest

import android.os.Parcel
import android.os.Parcelable
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.ForeignCollectionField
import com.j256.ormlite.table.DatabaseTable

// Player and Team will be mapped to players and teams tables in ORMLite.
// We need to make them Parcelable so they can be passed between fragments/activities.
// The rest of the classes are used to retrieve json responses from GET requests.

data class PlayerResponse(
    val data: List<Player>,
    val meta: Meta
)

@DatabaseTable(tableName = "players")
data class Player(
    @DatabaseField(id = true)
    val id: Int,
    @DatabaseField
    val first_name: String,
    @DatabaseField
    val height_feet: Int?,
    @DatabaseField
    val height_inches: Int?,
    @DatabaseField
    val last_name: String,
    @DatabaseField
    val position: String,
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    var team: Team,
    @DatabaseField
    val weight_pounds: Int?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readTypedObject(Team.CREATOR)!!,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    // Constructor with no arguments so OrmLite can work with this class.
    constructor() : this(0, "", 0, 0, "", "", Team(), 0)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(first_name)
        parcel.writeValue(height_feet)
        parcel.writeValue(height_inches)
        parcel.writeString(last_name)
        parcel.writeString(position)
        parcel.writeValue(weight_pounds)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Player> {
        override fun createFromParcel(parcel: Parcel): Player {
            return Player(parcel)
        }

        override fun newArray(size: Int): Array<Player?> {
            return arrayOfNulls(size)
        }
    }
}

@DatabaseTable(tableName = "teams")
data class Team(
    @DatabaseField(id = true)
    val id: Int,
    @DatabaseField
    val abbreviation: String,
    @DatabaseField
    val city: String,
    @DatabaseField
    val conference: String,
    @DatabaseField
    val division: String,
    @DatabaseField
    val full_name: String,
    @DatabaseField
    val name: String,
    @ForeignCollectionField(eager = false)
    var players: Collection<Player>? = null
)  : Parcelable {

    // Constructor with no arguments so OrmLite can work with this class.
    constructor() : this(0, "", "", "", "", "", "", null)

    constructor(source: Parcel) : this(
        source.readInt(),
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        null
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(abbreviation)
        dest.writeString(city)
        dest.writeString(conference)
        dest.writeString(division)
        dest.writeString(full_name)
        dest.writeString(name)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Team> = object : Parcelable.Creator<Team> {
            override fun createFromParcel(source: Parcel): Team = Team(source)
            override fun newArray(size: Int): Array<Team?> = arrayOfNulls(size)
        }
    }
}

data class Meta(
    val total_pages: Int,
    val current_page: Int,
    val next_page: Int,
    val per_page: Int,
    val total_count: Int
)

data class SeasonAverageResponse(
    val data: List<SeasonAverages>
)

data class SeasonAverages(
    val games_played: Int,
    val min: String,
    val pts: Float
)