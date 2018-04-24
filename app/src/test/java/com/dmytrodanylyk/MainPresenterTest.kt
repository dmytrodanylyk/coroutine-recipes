package com.dmytrodanylyk

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import org.mockito.Mockito.`when` as whenMockito

class MainPresenterTest {

    private val MOCK_RESULT = "MOCK_RESULT"

    @Test
    fun startPresenting() {
        // mock
        val mockView = mock(MainView::class.java)
        val mockDataProvider = mock(DataProviderAPI::class.java)
        whenMockito(mockDataProvider.loadData(ArgumentMatchers.anyString())).thenReturn(MOCK_RESULT)

        val presenter = MainPresenter(mockView, mockDataProvider, Unconfined, Unconfined)

        // test
        presenter.startPresenting()

        // verify
        verify(mockView).showLoading()
        verify(mockDataProvider).loadData("Task")
        verify(mockView).showData(MOCK_RESULT)
    }
}

class MainPresenter(private val view: MainView,
                    private val dataProvider: DataProviderAPI,
                    private val uiContext: CoroutineContext = UI,
                    private val bgContext: CoroutineContext = CommonPool) {

    fun startPresenting() {
        loadData()
    }

    private fun loadData() = launch(uiContext) {
        // use the provided uiContext (UI)
        view.showLoading()

        // use the provided ioContext (CommonPool)
        val task = async(bgContext) { dataProvider.loadData("Task") }
        val result = task.await()

        view.showData(result)
    }

}

interface MainView {
    fun showLoading()

    fun showData(data: String)
}

interface DataProviderAPI {
    fun loadData(data: String): String
}


