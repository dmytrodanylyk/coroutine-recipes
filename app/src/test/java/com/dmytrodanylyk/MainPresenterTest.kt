package com.dmytrodanylyk

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import kotlin.coroutines.experimental.CoroutineContext
import org.mockito.Mockito.`when` as whenMockito

class MainPresenterTest {

    private val MOCK_RESULT = "MOCK_RESULT"

    @Test
    fun startPresenting() {
        // mock
        val mockView = mock(MainView::class.java)
        val mockDataProvider = mock(DataProviderAPI::class.java)
        whenMockito(mockDataProvider.loadData(ArgumentMatchers.anyString())).thenReturn(MOCK_RESULT)

        val presenter = MainPresenter(mockView, mockDataProvider, Dispatchers.Unconfined, Dispatchers.Unconfined)

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
                    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main,
                    private val bgDispatcher: CoroutineContext = Dispatchers.IO) {

    fun startPresenting() {
        loadData()
    }

    private fun loadData() = GlobalScope.launch(uiDispatcher) {
        // use the provided uiContext (UI)
        view.showLoading()

        // use the provided ioContext (CommonPool)
        val task = async(bgDispatcher) { dataProvider.loadData("Task") }
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


