package beepbeep.pixels.home

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import beepbeep.pixels.R
import beepbeep.pixels.home.view.HomeAdapter
import beepbeep.pixels.shared.extension.addTo
import beepbeep.pixels.shared.extension.downScheduler
import beepbeep.pixels.shared.extension.isConnectedToInternet
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, LifecycleRegistryOwner {
    private val registry = LifecycleRegistry(this)
    override fun getLifecycle() = registry

    val retrySubject = PublishSubject.create<Unit>()

    val homeAdapter = HomeAdapter()

    private val input by lazy {
        object : HomeContract.Input {
            override fun isConnectedToInternet(): Boolean =
                    this@HomeActivity.isConnectedToInternet()

            override val refresh: Observable<Any>by lazy {
                RxView.clicks(refreshButton)
            }

            override val loadMore: Observable<Any> by lazy {
                RxView.clicks(loadMoreButton)
            }

            override val retry = retrySubject.hide()
        }
    }
    lateinit var presenter: HomePresenter
    val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.setDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        presenter = HomePresenter(input)

        bindView()
        lifecycle.addObserver(presenter)
    }

    fun bindView() {
        presenter.output.apply {
            showNoInternetSnackbar
                    .downScheduler(AndroidSchedulers.mainThread())
                    .subscribe {
                        Snackbar.make(homeMainContent, getString(R.string.no_internet_warning), Snackbar.LENGTH_INDEFINITE)
                                .setAction(getString(R.string.retry), { retrySubject.onNext(Unit) })
                                .show();
                    }
                    .addTo(disposables)
        }

        homeActRecyclerView.adapter = homeAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
