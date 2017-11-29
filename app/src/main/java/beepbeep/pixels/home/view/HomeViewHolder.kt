package beepbeep.pixels.home.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import beepbeep.pixels.R
import beepbeep.pixels.cache.submission.SubmissionCache
import beepbeep.pixels.shared.GlideApp
import beepbeep.pixels.shared.extension.toRelativetime
import kotlinx.android.synthetic.main.view_holder_home.view.*

class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(submission: SubmissionCache) {
        itemView.apply {
            vh_home_title.text = submission.title
            vh_home_subreddit.text = submission.subredditName
            vh_home_author.text = submission.author.trim()
            vh_home_time.text = submission.date.toRelativetime()
            GlideApp
                    .with(context)
                    .load(submission.url)
                    .centerCrop()
                    .placeholder(R.color.material_grey300)
                    .into(vh_home_image);
            //Picasso.with(context).load(submission.url).fit().centerInside().into(vh_home_image)
        }
    }

    companion object {
        @JvmStatic
        fun create(parent: ViewGroup): HomeViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_home, parent, false)
            return HomeViewHolder(itemView)
        }
    }
}