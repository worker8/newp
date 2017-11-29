package beepbeep.pixels.home.view

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import beepbeep.pixels.cache.submission.SubmissionCache

class HomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var posts = mutableListOf<SubmissionCache>()

    fun addPosts(newPosts: List<SubmissionCache>) {

        val oldSize = posts.size
        posts.apply { addAll(newPosts) }
                .run { distinctBy { it.id } }
                .also { posts = mutableListOf() }
                .let { posts.addAll(it) }
//
        notifyItemRangeInserted(oldSize, posts.size - oldSize)
//        val homeDiffCallback = HomeDiffCallback(newList = newPosts, oldList = posts)
//        val diff = DiffUtil.calculateDiff(homeDiffCallback)
//        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HomeViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as HomeViewHolder).bind(posts.get(position))
    }
}