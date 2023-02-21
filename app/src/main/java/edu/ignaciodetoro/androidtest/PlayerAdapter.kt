package edu.ignaciodetoro.androidtest

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class PlayerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: MutableList<Player> = mutableListOf()

    // Update RecyclerView items.
    @SuppressLint("NotifyDataSetChanged")
    fun addPlayers(players: List<Player>) {
        items.clear()
        items.addAll(players)
        notifyDataSetChanged()
    }

    // Inflates every item using item_player.xml.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    // Number of items.
    override fun getItemCount(): Int {
        return items.size
    }

    // Updates data ViewHolder data.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val player = items[position]
        val playerViewHolder = holder as PlayerViewHolder
        playerViewHolder.bind(player)
    }

    // Defines bind to fill every item view.
    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val teamLogoImageView: ImageView = itemView.findViewById(R.id.team_logo)
        private val playerNameTextView: TextView = itemView.findViewById(R.id.player_name)
        private val playerPositionTextView: TextView = itemView.findViewById(R.id.player_position)
        private val teamNameTextView: TextView = itemView.findViewById(R.id.team_name)


        fun bind(player: Player) {
            val teamLogoUrl = if (player.team.conference == "West") {
                "https://content.sportslogos.net/logos/6/1001/full/2996.gif"
            } else {
                "https://content.sportslogos.net/logos/6/999/full/2995.gif"
            }

            Glide.with(itemView.context)
                .load(teamLogoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(teamLogoImageView)

            val fullName = player.first_name+" "+player.last_name
            playerNameTextView.text = fullName
            playerPositionTextView.text = player.position
            teamNameTextView.text = player.team.full_name
            itemView.setOnClickListener { onItemClick(player) }
        }

        // Inflates a new fragment when item clicked providing player object.
        private fun onItemClick(player: Player) {
            val fragment = PlayerDetailFragment()
            val args = Bundle()
            args.putParcelable("player", player)
            fragment.arguments = args
            val fragmentTransaction = (itemView.context as AppCompatActivity).supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.recycler_view_container, fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

}
