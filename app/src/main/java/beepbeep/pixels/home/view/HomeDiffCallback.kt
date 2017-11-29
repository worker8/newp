package beepbeep.pixels.home.view

import android.support.v7.util.DiffUtil
import beepbeep.pixels.cache.submission.SubmissionCache

class HomeDiffCallback(val newList: List<SubmissionCache>, val oldList: List<SubmissionCache>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
        return oldList[oldPos] == newList[newPos]
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
        val oldId = oldList[oldPos].id
        val newId = newList[newPos].id
        return newId.equals(oldId)
    }

}