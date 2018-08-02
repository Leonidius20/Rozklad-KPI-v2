package com.goldenpiedevs.schedule.app.ui.launcher

import android.graphics.BitmapFactory
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import com.goldenpiedevs.schedule.app.R
import com.goldenpiedevs.schedule.app.core.api.group.GroupManager
import com.goldenpiedevs.schedule.app.core.api.lessons.LessonsManager
import com.goldenpiedevs.schedule.app.core.dao.group.GroupModel
import com.goldenpiedevs.schedule.app.core.utils.AppPreference
import com.goldenpiedevs.schedule.app.ui.base.BasePresenterImpl
import com.jakewharton.rxbinding2.widget.RxAutoCompleteTextView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.blurry.Blurry
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class LauncherImplementation : BasePresenterImpl<LauncherView>(), LauncherPresenter {

    private val MIN_LENGTH_TO_START = 2

    @Inject
    lateinit var groupManager: GroupManager
    @Inject
    lateinit var lessonsManager: LessonsManager

    private lateinit var autoCompleteTextView: AutoCompleteTextView

    override fun setAutocompleteTextView(autoCompleteTextView: AutoCompleteTextView) {
        this.autoCompleteTextView = autoCompleteTextView

        addOnAutoCompleteTextViewItemClickedSubscriber(this.autoCompleteTextView)
        addOnAutoCompleteTextViewTextChangedObserver(this.autoCompleteTextView)
    }

    override fun showNextScreen() {
        if (AppPreference.isFirstLaunch) {
            showInitView()
        } else {
            showMainScreen()
        }
    }

    private fun showMainScreen() {
        (view as AppCompatActivity).finish()

//        view.getContext().startActivity(Intent(view.getContext(), Object::class.java)) //FIXME: Change to Main Activity
    }

    private fun showInitView() {
        view.showGroupChooserView()
    }

    override fun blurView(view: View) {
        Blurry.with(view.context)
                .radius(10)
                .sampling(8)
                .color(ContextCompat.getColor(view.context, R.color.blur))
                .async()
                .from(BitmapFactory.decodeResource(view.resources, R.drawable.init_screen_back))
                .into(view as ImageView?)
    }

    private fun addOnAutoCompleteTextViewTextChangedObserver(autoCompleteTextView: AutoCompleteTextView) {
        val subscription = RxTextView.textChangeEvents(autoCompleteTextView)
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .filter { it.text().length >= MIN_LENGTH_TO_START }
                .switchMap {
                    val text = it.text().toString().apply {
                        toUpperCase()
                        if (contains("И"))
                            replace("И", "i")
                    }

                    groupManager.autocomplete(text)
                            .onErrorResumeNext(Observable.empty())
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        compositeDisposable.add(
                subscription.subscribe(
                        {
                            if (!it.isSuccessful) {
                                autoCompleteTextView.dismissDropDown()
                                return@subscribe
                            }

                            val list = it.body()
                            val itemsAdapter = ArrayAdapter<GroupModel>(view.getContext(),
                                    R.layout.centered_material_list_item, list!!.data)

                            autoCompleteTextView.setAdapter<ArrayAdapter<GroupModel>>(itemsAdapter)

                            if (!list.data!!.isNotEmpty()) {
                                autoCompleteTextView.dismissDropDown()
                            } else {
                                autoCompleteTextView.showDropDown()
                            }
                        },
                        { Log.e(TAG, "onError", it) }))
    }

    private fun addOnAutoCompleteTextViewItemClickedSubscriber(autoCompleteTextView: AutoCompleteTextView) {
        val adapterViewItemClickEventObservable = RxAutoCompleteTextView.itemClickEvents(autoCompleteTextView)
                .map {
                    val item = autoCompleteTextView.adapter.getItem(it.position()) as GroupModel
                    item.groupId
                }
                .observeOn(Schedulers.io())
                .switchMap { groupManager.groupDetails(it) }
                .observeOn(AndroidSchedulers.mainThread())

        compositeDisposable.add(
                adapterViewItemClickEventObservable.subscribe(
                        { awaitNextScreen(it.body()!!) },
                        { throwable -> Log.e(TAG, "onError", throwable) }))
    }

    private fun awaitNextScreen(body: GroupModel) {
        view.showProgreeDialog()

        launch {
            val response = lessonsManager.loadTimeTable(body.groupId).await()
            launch(UI) {
                view.dismissProgreeDialog()

                if (response.isSuccessful) {
                    showMainScreen()
                } else {
                    view.onError()
                }
            }

        }
    }

}