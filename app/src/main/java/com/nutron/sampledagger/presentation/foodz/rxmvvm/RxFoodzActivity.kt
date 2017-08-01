package com.nutron.sampledagger.presentation.foodz.rxmvvm

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.nutron.sampledagger.MainApplication
import com.nutron.sampledagger.R
import com.nutron.sampledagger.data.entity.FoodzItem
import com.nutron.sampledagger.extensions.addTo
import com.nutron.sampledagger.presentation.food.rxmvvm.RxFoodDetailActivity
import com.nutron.sampledagger.presentation.foodz.FoodzAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class RxFoodzActivity : AppCompatActivity() {

    @Inject lateinit var viewModel: RxFoodzViewModel

    private val disposeBag = CompositeDisposable()
    private val foodzAdapter = FoodzAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MainApplication.appComponent.inject(this)
        initView()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        initViewModelOutput()
        initViewModelInput()
    }

    private fun initViewModelInput() {
        viewModel.input.active.accept(Unit)
    }

    private fun initViewModelOutput() {
        viewModel.output.foodzResult
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    showFoodz(items)
                }, { e ->
                    showErrorMessage(e)
                }).addTo(disposeBag)

        viewModel.output.showProgress
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isShown ->
                    if (isShown) showLoading() else hideLoading()
                }.addTo(disposeBag)

        viewModel.output.error
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showErrorMessage(it)
                }
    }

    fun showLoading() {
        foodzProgressBar.visibility = View.VISIBLE
    }

    fun hideLoading() {
        foodzProgressBar.visibility = View.GONE
    }

    fun showFoodz(items: List<FoodzItem>) {
        foodzAdapter.items = items
        foodzAdapter.notifyDataSetChanged()
    }

    fun showErrorMessage(t: Throwable) {
        Log.d("DEBUG", t.message)
        Toast.makeText(this, R.string.foodzListError, Toast.LENGTH_SHORT).show()
    }

    fun launchFoodDetail(foodzItem: FoodzItem) {
        startActivity(RxFoodDetailActivity.getStartIntent(this, foodzItem.id))
    }

    fun initView() {
        val layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(foodzRecyclerView.context, layoutManager.orientation)
        foodzAdapter.listener = { item -> launchFoodDetail(item) }
        foodzRecyclerView.addItemDecoration(dividerItemDecoration)
        foodzRecyclerView.layoutManager = layoutManager
        foodzRecyclerView.adapter = foodzAdapter

    }
}