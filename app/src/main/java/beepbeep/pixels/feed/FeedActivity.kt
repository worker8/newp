package beepbeep.pixels.feed

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import beepbeep.pixels.R
import beepbeep.pixels.shared.extension.addTo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.content_home.loadMoreButton
import kotlinx.android.synthetic.main.content_home.loadingProgress
import kotlinx.android.synthetic.main.content_home.refreshButton
import kotlinx.android.synthetic.main.content_home.resultText

class FeedActivity : AppCompatActivity(), FeedContract.Input {

    private val interactor: FeedContract.Output by lazy {
        FeedInteractor(this, FakeFeedRepository())
    }

    private val refreshable = BehaviorSubject.create<View>()

    private val loadMoreable = BehaviorSubject.create<View>()

    override val onRefresh: Observable<Unit> = refreshable.hide().map { Unit }

    override val onLoadMore: Observable<Unit> = loadMoreable.hide().map { Unit }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}