package com.vsch.mvi.presentation.plugSelect

import android.provider.Settings.Global.getString
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.vsch.mvi.R
import com.vsch.mvi.base.GlobalStoreModel
import com.vsch.mvi.base.StoreData
import com.vsch.mvi.navigation.Route
import com.zj.wanandroid.utils.RouteUtils
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import com.vsch.mvi.ui.theme.ButtonBackground
import com.vsch.mvi.ui.theme.CustomCard
import com.vsch.mvi.ui.theme.FormCardText
import com.vsch.mvi.ui.theme.FormHeaderText
import com.vsch.mvi.ui.theme.GradEnd
import com.vsch.mvi.ui.theme.GradStart
import com.vsch.mvi.ui.theme.dimens
import com.vsch.mvi.Common.AutoFocusingTextEval
import com.vsch.mvi.payment.PaymentModelVerifone
import com.vsch.mvi.Common.ShowCustomMessageBox
import com.vsch.mvi.base.SharedData

@Composable
fun  PlugInfoScreen(
    navCtrl: NavHostController,
    payViewModel: PaymentModelVerifone
) {

    val logicmodel = hiltViewModel<GlobalStoreModel>()
    val uiviewModel = hiltViewModel<PlugInfoScreenModel>()
    val scope = rememberCoroutineScope()
    val state by uiviewModel.state.collectAsState()
    val logicstore by logicmodel.containerstore.collectAsState()
    if (!state.isLoading) uiviewModel.setStore(logicstore)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(bottom = MaterialTheme.dimens.small2)
            .clickable {
                uiviewModel.incrementClickCounter()
            }
    ) {

        when {
            !state.isLoading -> ContentWithProgress()
            state.isLoading -> PlugSelectScreenContent(
                navCtrl,
                state.isDevicePhone,
                state.st,
                onPlugEnterNum = { text ->
                    run {
                        logicmodel.setPlugNum(text)
                        uiviewModel.setPlugNum(text)
                    }
                },


                onSetLang = { langs ->
                    run {
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(
                                langs
                            )
                        )
                        logicmodel.setLang(langs)
                        uiviewModel.setLang(langs)
                    }
                },
                 onSelectScreen =
                {
                    scope.launch {
                        var result = uiviewModel.findNumber()
                        if (result) {
                            logicmodel.setStore(state.st)
                            if (state.st.currentplug!!.state == 3 || state.st.currentplug!!.state == 5|| state.st.currentplug!!.state == 4|| state.st.currentplug!!.state == 12)
                            {
                                logicmodel.GetChargingData()
                                RouteUtils.navTo(
                                    navCtrl,
                                    Route.BUSYPLUG
                                )
                            }
                            if (state.st.currentplug!!.state == 0||state.st.currentplug!!.state == 1) RouteUtils.navTo(
                                navCtrl,
                                Route.AVAILIBLEPLUG
                            )

                            if (state.st.currentplug!!.state == 8)
                            {
                                uiviewModel.setMessage(mutableListOf<Any>(R.string.msg_plugwasdisable,state.st.plugnumber))
                            }
                        } else
                        {
                            uiviewModel.setMessage(mutableListOf<Any>(R.string.msg_plugnotexist,state.st.plugnumber))
                        }
                    }

                },
            )
        }





if (state.showMessage.isNotEmpty()) {
    val text: String = stringResource(state.showMessage.get(0) as Int,state.showMessage.get(1).toString())
    ShowCustomMessageBox(text, onSetView = { uiviewModel.setMessage(mutableListOf()) })
}

        if (state.formclicks >= 6) {
            run {
                ShowSettingsDialog(
                    SharedData(logicstore.terminal.toString(),logicmodel.getUrl(),logicstore.paymetmodelite),
                    onDialogDismissClick =
                    {
                        uiviewModel.clearClickCounter()
                    },
                    onDialogOkClick =
                    { sharedData ->
                        uiviewModel.clearClickCounter()
                        scope.launch {
                            logicmodel.updateStartData(sharedData)
                            logicmodel.reinit()
                            uiviewModel.setStore(logicstore)

                        }
                    })
            }
        }
    }
}


@Composable
private fun PlugSelectScreenContent(
    navCtrl: NavHostController,
    isDevicePhone: Boolean,
    store: StoreData,
    onPlugEnterNum: (String) -> Unit,
    onSetLang: (String) -> Unit,
    onSelectScreen: () -> Unit,
) {

    val screenHeight = LocalConfiguration.current.screenHeightDp
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Box (
        modifier = Modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GradStart, GradEnd),
                    start = Offset(0f,screenHeight.toFloat()),
                    end = Offset(screenWidth.toFloat(),0f ) 
                )
            )
            .fillMaxHeight()

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.small1))
            Row(
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                Spacer(Modifier.weight(1f))
                Image(
                    painter = painterResource(R.drawable.suomi),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(MaterialTheme.dimens.logoSize)
                        .height(MaterialTheme.dimens.logoSize)
                        .clip(CircleShape)
                        .clickable
                        {
                            onSetLang("fi")
                        },
                    contentDescription = "",
                )
                Spacer(
                    modifier = Modifier
                        .weight(0.1f)
                        .height(MaterialTheme.dimens.small1)
                )
                Image(
                    painter = painterResource(R.drawable.sweden),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier

                        .width(MaterialTheme.dimens.logoSize)
                        .height(MaterialTheme.dimens.logoSize)
                        .clip(CircleShape)
                        .clickable
                        {
                            onSetLang("sv")
                        },
                    contentDescription = "",
                )
                Spacer(
                    modifier = Modifier
                        .weight(0.1f)
                        .height(MaterialTheme.dimens.small1)
                )
                Image(
                    painter = painterResource(R.drawable.eng),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(MaterialTheme.dimens.logoSize)
                        .height(MaterialTheme.dimens.logoSize)
                        .clip(CircleShape)
                        .clickable
                        {
                            onSetLang("en")
                        },
                    contentDescription = "",
                )
                Spacer(
                    modifier = Modifier
                        .weight(0.1f)
                )
            }
                Row(
                    modifier = Modifier
                        .padding(top = (screenHeight /13).dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                        Text(
                            text = stringResource(R.string.main_screen_welcome).uppercase(),
                            style = TextStyle(
                                color = FormHeaderText,
                                textAlign = TextAlign.Center,
                                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                            )
                        )

                  }
       }
        Box(
            modifier = Modifier
                .padding(top = (screenHeight /4).dp)
                .clip(shape = RoundedCornerShape(MaterialTheme.dimens.cardround, MaterialTheme.dimens.cardround, 0.dp, 0.dp))
                .background(CustomCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()

            ) {


                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium2))
                Text(
                    text = stringResource(R.string.plug_select_screen_title),
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = TextStyle(
                        color = FormCardText,
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    )
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium3))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((screenHeight / 8).dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AutoFocusingTextEval(store.plugnumber, onPlugEnterNum , onEnterEvent = {
                        onSelectScreen()})
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}


@Composable
private fun ContentWithProgress() {
    Surface(color = Color.LightGray) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}


