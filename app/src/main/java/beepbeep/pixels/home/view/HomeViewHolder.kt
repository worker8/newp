package beepbeep.pixels.home.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import beepbeep.pixels.R

class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        @JvmStatic
        fun create(parent: ViewGroup): HomeViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_home, parent, false)
            return HomeViewHolder(itemView)
        }
    }
}