package com.vsch.mvi.presentation.plugSelect

import com.vsch.mvi.base.StoreData
import com.vsch.mvi.base.UiEvent
import com.vsch.mvi.base.UiState
import com.vsch.mvi.rest.bean.*
import javax.annotation.concurrent.Immutable

@Immutable
sealed class PlugInfoScreenUiEvent : UiEvent {
    data class OnSetStore(val stdata: StoreData) : PlugInfoScreenUiEvent()
    data class OnSetLang(val lang: String) : PlugInfoScreenUiEvent()
    data class OnSetPlugsStatus(val statusvalue: List<StatusPlugs>) : PlugInfoScreenUiEvent()
    data class OnPlugNumberChanged(val num: String) : PlugInfoScreenUiEvent()
    data class OnSetFindPlug(val plugdata: StatusPlugs) : PlugInfoScreenUiEvent()
    data class onFormClick(val num: String) : PlugInfoScreenUiEvent()
    data class onUpdateTerminal(val terminal: String) : PlugInfoScreenUiEvent()
    data class onShowMessage(val mess: MutableList<Any>) : PlugInfoScreenUiEvent()
    data class onMessageBoxView(val vals: Boolean) : PlugInfoScreenUiEvent()
}

@Immutable
data class PlugInfoScreenState(
    val isLoading: Boolean,
    override var isRefresh: Boolean,
    val isDevicePhone: Boolean,
    var st:StoreData,
    var formclicks: Int,
    var showMessage: MutableList<Any>,
    var messageBoxView: Boolean,
) : UiState {

    companion object {
        fun initial() = PlugInfoScreenState(
            isLoading = false,
            isRefresh=false,
            isDevicePhone=true,
            st = StoreData(),
            formclicks = 0,
            showMessage = mutableListOf<Any>(),
            messageBoxView = false,
        )
    }

    override fun toString(): String {
        return "isLoading: $isLoading"
    }
}