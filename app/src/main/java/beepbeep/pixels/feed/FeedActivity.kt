package beepbeep.pixels.feed

import android.arch.lifecycle.LifecycleRegistry
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import beepbeep.pixels.R
import beepbeep.pixels.home.HomeContract
import beepbeep.pixels.home.HomePresenter
import beepbeep.pixels.home.HomeRepo
import beepbeep.pixels.home.view.HomeAdapter
import beepbeep.pixels.shared.extension.addTo
import beepbeep.pixels.shared.extension.isConnectedToInternet
import com.github.kittinunf.reactiveandroid.reactive.view.click
import com.github.kittinunf.reactiveandroid.reactive.view.rx
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.content_home.*

class FeedActivity : AppCompatActivity(), FeedContract.Input {
    private val registry = LifecycleRegistry(this)
    override fun getLifecycle() = registry

    private val interactor: FeedContract.Output by lazy {
        FeedInteractor(this, FakeFeedRepository())
    }

    private val refreshable = BehaviorSubject.create<View>()

    private val loadMoreable = BehaviorSubject.create<View>()

    override val onRefresh: Observable<Unit> = refreshable.hide().map { Unit }

    override val onLoadMore: Observable<Unit> = loadMoreable.hide().map { Unit }

    private val disposables = CompositeDisposable()

    val retrySubject = PublishSubject.create<Unit>()
    val homeAdapter = HomeAdapter()

    private val input by lazy {
        object : HomeContract.Input {
            override fun isConnectedToInternet(): Boolean =
                    this@FeedActivity.isConnectedToInternet()

            override val refresh: Observable<Any> by lazy {
                refreshButton.rx.click().map { Any() }
            }

            override val loadMore: Observable<Any> by lazy {
                loadMoreButton.rx.click().map { Any() }
            }

            override val retry = retrySubject.hide()
        }
    }

    lateinit var presenter: HomePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        presenter = HomePresenter(input)
        setupViews()
        setupBindings()
    }

    private fun setupViews() {
        refreshButton.setOnClickListener(refreshable::onNext)
        loadMoreButton.setOnClickListener(loadMoreable::onNext)
    }

    private fun setupBindings() {
        interactor.loadings
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    loadingProgress.visibility = if (it) View.VISIBLE else View.GONE
                }
                .addTo(disposables)

        interactor.items
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    resultText.text = "${it.size}: $it"
                }
                .addTo(disposables)

        HomeRepo().bindToDb()?.apply {
            subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { listing ->
                        homeAdapter.addPosts(listing)
                        //listing.forEachIndexed { index, submissionCache ->
                        //Log.d("ddw", "[${index}] ${submissionCache.author}: ${submissionCache.title}")
                        //}
                    }
                    .addTo(disposables)

            homeActRecyclerView.adapter = homeAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}