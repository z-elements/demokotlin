package com.vsch.mvi.presentation.plugSelect

import com.vsch.mvi.base.BaseViewModel
import com.vsch.mvi.base.Reducer
import com.vsch.mvi.base.TimeCapsule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.vsch.mvi.base.StoreData
import com.vsch.mvi.domain.use_case.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@HiltViewModel
class  PlugInfoScreenModel @Inject constructor(
    private val requestsHTTP: IRequests,
    private val dispatcher: CoroutineDispatcher,
) : BaseViewModel<PlugInfoScreenState, PlugInfoScreenUiEvent>() {

    private val reducer = PlugInfoReducer(PlugInfoScreenState.initial())

    override val state: StateFlow<PlugInfoScreenState>
        get() = reducer.state

    val timeMachine: TimeCapsule<PlugInfoScreenState>
        get() = reducer.timeCapsule

    suspend fun fetchData(): Job {
        return viewModelScope.launch (dispatcher) {
            flow {
                emit(requestsHTTP.invoke(state.value.st, HttpRequestsCommand.StatusPlugs))
            }.onEach {
                if (it!!.data != null) {
                    it!!.data
                    if (it!!.data!=null) setStore( it!!.data!!)
                }
            }.catch {
                Log.d("errstring", it.toString())
            }.collect()
        }
    }

    fun setStore(st: StoreData) {
        sendEvent(PlugInfoScreenUiEvent.OnSetStore(st))
    }

    fun setMessage(vals: MutableList<Any>) {
        sendEvent(PlugInfoScreenUiEvent.onShowMessage(vals))
    }

    fun setMessageBoxView(vals: Boolean) {
        sendEvent(PlugInfoScreenUiEvent.onMessageBoxView(vals))
    }
    fun setPlugNum(vars: String) {
        sendEvent(PlugInfoScreenUiEvent.OnPlugNumberChanged(vars))
    }

    fun updateStartData(terminal: String)
    {
        sendEvent(PlugInfoScreenUiEvent.onUpdateTerminal(terminal))
    }

    fun incrementClickCounter() {
        sendEvent(PlugInfoScreenUiEvent.onFormClick(""))
    }
    fun clearClickCounter() {
        sendEvent(PlugInfoScreenUiEvent.onFormClick("clear"))
    }

     @OptIn(ExperimentalCoroutinesApi::class)
     suspend fun findNumber(): Boolean {
        var result = viewModelScope.async(dispatcher)
        {
            if (state.value.st.plugnumber=="")  return@async false
            lateinit var jobs: Job
            runBlocking {
                jobs =  fetchData()
            }
            jobs.join()
            if (state.value.st.statusplugs!= null)
            {
                for (i in 0 until state.value.st.statusplugs!!.size) {
                    if (state.value.st.statusplugs!![i].electroID == state.value.st.plugnumber.toInt()) {
                        sendEvent(PlugInfoScreenUiEvent.OnSetFindPlug(state.value.st.statusplugs!![i]))
                        return@async true
                    }
                }
            } else {
                return@async false
            }
        }
        if (result.await() == true)  return true else return false

    }

    fun setLang(lang: String) {
        sendEvent(PlugInfoScreenUiEvent.OnSetLang(lang))
    }

    private fun sendEvent(event: PlugInfoScreenUiEvent) {
        reducer.sendEvent(event)
    }

    private class PlugInfoReducer(initial: PlugInfoScreenState) : Reducer<PlugInfoScreenState, PlugInfoScreenUiEvent>(initial) {
        override fun reduce(oldState: PlugInfoScreenState, event: PlugInfoScreenUiEvent) {
            when (event) {
                is PlugInfoScreenUiEvent.OnPlugNumberChanged -> {
                    val newstore = oldState.st
                    newstore.plugnumber = event.num
                    updateState(oldState.copy(st = newstore))
                }

                is PlugInfoScreenUiEvent.OnSetStore -> {
                    setState(oldState.copy(isLoading = true,isRefresh = !oldState.isRefresh, st = event.stdata))
                }

                is PlugInfoScreenUiEvent.OnSetLang -> {
                    val data = oldState.st
                    data.lang = event.lang
                    updateState(oldState.copy(st = data))
                }

                is PlugInfoScreenUiEvent.OnSetPlugsStatus -> {
                    Log.d("receive_data", event.statusvalue.toString())
                    val data = oldState.st
                    data.statusplugs = event.statusvalue
                    updateState(oldState.copy(st = data))
                }

                is PlugInfoScreenUiEvent.OnSetFindPlug -> {
                    val data = oldState.st
                    data.currentplug = event.plugdata
                    updateState(oldState.copy( st = data))
                }

                is PlugInfoScreenUiEvent.onFormClick -> {
                    if (event.num=="") {
                        var data = oldState.formclicks
                        data++
                        setState(oldState.copy(formclicks = data))
                    }

                    if (event.num=="clear") {
                        setState(oldState.copy(formclicks = 0))
                    }

                }
                is PlugInfoScreenUiEvent.onUpdateTerminal -> {
                    val data = oldState.st
                    data.terminal = event.terminal.toInt()
                    updateState(oldState.copy(st = data))

                }

                is PlugInfoScreenUiEvent.onShowMessage -> {
                    updateState(oldState.copy(showMessage = event.mess))
                }

                is PlugInfoScreenUiEvent.onMessageBoxView -> {
                    updateState(oldState.copy(messageBoxView = event.vals))

                }

            }
        }
    }
}