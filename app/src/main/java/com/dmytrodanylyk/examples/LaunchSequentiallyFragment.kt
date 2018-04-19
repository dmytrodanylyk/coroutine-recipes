package com.dmytrodanylyk.examples

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dmytrodanylyk.R
import kotlinx.android.synthetic.main.fragment_button.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

class LaunchSequentiallyFragment : Fragment() {

    private val uiContext: CoroutineContext = UI
    private val dataProvider = DataProvider()
    private val job: Job = Job()

    companion object {
        const val TAG = "LaunchSequentiallyFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_button, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.setOnClickListener { loadData() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

    private fun loadData() = launch(uiContext, parent = job) {
        showLoading()

        val result1 = dataProvider.loadData()
        val result2 = dataProvider.loadData()

        showText("$result1\n$result2")
        hideLoading()
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showText(data: String) {
        textView.text = data
    }

    class DataProvider(private val context: CoroutineContext = CommonPool) {

        suspend fun loadData(): String = withContext(context) {
            delay(2, TimeUnit.SECONDS) // imitate long running operation
            "Data is available: ${Random().nextInt()}"
        }
    }
}