package com.goldenpiedevs.schedule.app.ui.timetable

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.goldenpiedevs.schedule.app.R
import com.goldenpiedevs.schedule.app.core.dao.timetable.DayModel
import com.goldenpiedevs.schedule.app.ui.base.BaseFragment
import com.goldenpiedevs.schedule.app.ui.view.CenteredLayoutManager
import com.goldenpiedevs.schedule.app.ui.view.adapter.TimeTableAdapter
import io.realm.OrderedRealmCollection
import kotlinx.android.synthetic.main.time_table_layout.*


class TimeTableFragment : BaseFragment(), TimeTableView {
    private lateinit var presenter: TimeTablePresenter

    override fun getFragmentLayout(): Int = R.layout.time_table_layout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter = TimeTableImplementation()
        presenter.attachView(this)

        setUpRecyclerView(firstWeekList, secondWeekList)

        presenter.getData()

        presenter.showCurrentDay()
    }

    private fun setUpRecyclerView(vararg recyclerView: RecyclerView) {
        recyclerView.forEach {
            it.isNestedScrollingEnabled = false
            it.layoutManager = CenteredLayoutManager(context)
        }
    }

    override fun showWeekData(isFirstWeek: Boolean, orderedRealmCollection: OrderedRealmCollection<DayModel>) {
        if (isFirstWeek) {
            if (firstWeekList.adapter == null)
                firstWeekList.adapter = TimeTableAdapter(orderedRealmCollection)
        } else {
            if (secondWeekList.adapter == null)
                secondWeekList.adapter = TimeTableAdapter(orderedRealmCollection)
        }
    }

    override fun showCurrentDay(isFirstWeek: Boolean, currentDay: Int) {
        (if (isFirstWeek) firstWeekList else secondWeekList).let {
            it.post {
                presenter.scrollToView(activity!!.findViewById(R.id.appbar), baseScrollView, it.getChildAt(currentDay))
            }

        }
    }
}