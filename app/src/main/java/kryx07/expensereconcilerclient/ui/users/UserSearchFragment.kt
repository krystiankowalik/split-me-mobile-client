package kryx07.expensereconcilerclient.ui.users

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_user_search_view.view.*
import kryx07.expensereconcilerclient.App
import kryx07.expensereconcilerclient.R
import kryx07.expensereconcilerclient.events.HideProgressEvent
import kryx07.expensereconcilerclient.events.HideRefresherEvent
import kryx07.expensereconcilerclient.events.ShowProgressEvent
import kryx07.expensereconcilerclient.model.users.User
import kryx07.expensereconcilerclient.network.ApiClient
import kryx07.expensereconcilerclient.ui.DashboardActivity
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import javax.inject.Inject


class UserSearchFragment : Fragment(), SearchView.OnQueryTextListener, UserSearchMvpView {


    @Inject lateinit var presenter: UserSearchPresenter
    @Inject lateinit var apiClient: ApiClient
    @Inject lateinit var eventBus: EventBus

    lateinit var adapter: UsersAdapter

    private val usersList = arrayListOf<User>()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_user_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_user_search_view, container, false)
        super.onCreateView(inflater, container, savedInstanceState)
        App.appComponent.inject(this)

//        activity.actionBar.setDisplayHomeAsUpEnabled(true)

        adapter = UsersAdapter()
        view.users_recycler.layoutManager = LinearLayoutManager(context)
        view.users_recycler.adapter = adapter

        presenter.attachView(this)

        val supportActionBar = (activity as DashboardActivity).supportActionBar
        supportActionBar!!.title = "Add a user"
        supportActionBar.setDisplayHomeAsUpEnabled(true)

        requestUsers()

        setHasOptionsMenu(true)
        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                Timber.e("Pushed")
                //ViewUtilities.showPreviousFragment(activity.supportFragmentManager,this)
                activity.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestUsers() {

        showProgress()

        apiClient.service.allUsers
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ users ->
                    usersList.addAll(users)
                    updateData(usersList)
                })
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onDestroyView() {
        presenter.detach()
        super.onDestroyView()
    }

    override fun updateData(users: List<User>) {
        adapter.updateData(users)
        hideProgress()
    }

    private fun filter(users: List<User>, query: String): List<User> {
        val lowerCaseQuery = query.toLowerCase()
        val filteredUsers = arrayListOf<User>()
        users.forEach { u ->
            if (u.username.contains(lowerCaseQuery)) {
                filteredUsers.add(u)
            }
        }
        return filteredUsers
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        showProgress()
        var filteredList = filter(usersList, query)
        updateData(filteredList)
        return false
    }


    override fun onQueryTextChange(query: String): Boolean {
        showProgress()
        val filteredList = filter(usersList, query)
        updateData(filteredList)
        return true
    }


    override fun showProgress() {
        EventBus.getDefault().post(ShowProgressEvent())

    }

    override fun hideProgress() {
        EventBus.getDefault().post(HideProgressEvent())
        EventBus.getDefault().post(HideRefresherEvent())
    }


}