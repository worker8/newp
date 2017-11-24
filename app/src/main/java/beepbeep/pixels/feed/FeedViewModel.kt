package beepbeep.pixels.feed

import beepbeep.pixels.feed.list.FeedListViewModel

sealed class FeedAction

class SetAction(val items: List<FeedListViewModel>) : FeedAction()
object LoadAction : FeedAction()

data class FeedViewModel(val isLoading: Boolean = false,
                         val items: List<FeedListViewModel> = listOf()) {
    companion object {
        fun reduce(viewModel: FeedViewModel, action: FeedAction): FeedViewModel =
                when (action) {
                    is SetAction -> FeedViewModel(false, action.items)
                    is LoadAction -> FeedViewModel(true, viewModel.items)
                }
    }
}