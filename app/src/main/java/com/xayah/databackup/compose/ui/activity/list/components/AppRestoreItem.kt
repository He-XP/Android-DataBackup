package com.xayah.databackup.compose.ui.activity.list.components

import android.graphics.BitmapFactory
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.graphics.drawable.toDrawable
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.xayah.databackup.App
import com.xayah.databackup.R
import com.xayah.databackup.compose.ui.activity.list.components.animation.ExpandAnimation
import com.xayah.databackup.data.AppInfoRestore
import com.xayah.databackup.util.Command
import com.xayah.databackup.util.Path
import com.xayah.databackup.util.RemoteFile
import com.xayah.databackup.util.readIsReadIcon

@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun AppRestoreItem(appInfoRestore: AppInfoRestore, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val iconSmallSize = dimensionResource(R.dimen.icon_small_size)
    val tinyPadding = dimensionResource(R.dimen.padding_tiny)
    val nonePadding = dimensionResource(R.dimen.padding_none)
    val smallPadding = dimensionResource(R.dimen.padding_small)
    val mediumPadding = dimensionResource(R.dimen.padding_medium)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(mediumPadding))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if ((appInfoRestore.selectApp && appInfoRestore.selectData).not() &&
                    (appInfoRestore.selectApp || appInfoRestore.selectData)
                ) {
                    if (appInfoRestore.selectApp.not()) {
                        if (appInfoRestore.detailRestoreList[appInfoRestore.restoreIndex].hasApp)
                            appInfoRestore.selectApp = appInfoRestore.selectApp.not()
                    } else {
                        if (appInfoRestore.detailRestoreList[appInfoRestore.restoreIndex].hasData)
                            appInfoRestore.selectData = appInfoRestore.selectData.not()
                    }
                } else {
                    if (appInfoRestore.detailRestoreList[appInfoRestore.restoreIndex].hasApp)
                        appInfoRestore.selectApp = appInfoRestore.selectApp.not()
                    if (appInfoRestore.detailRestoreList[appInfoRestore.restoreIndex].hasData)
                        appInfoRestore.selectData = appInfoRestore.selectData.not()
                }
            }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (appInfoRestore.detailBase.appIcon == null) {
                appInfoRestore.detailBase.appIcon =
                    AppCompatResources.getDrawable(context, R.drawable.ic_round_android)
                if (App.globalContext.readIsReadIcon()) {
                    try {
                        val bytes = RemoteFile.getInstance()
                            .readBytes("${Path.getBackupDataSavePath()}/${appInfoRestore.detailBase.packageName}/icon.png")
                        appInfoRestore.detailBase.appIcon =
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                .toDrawable(context.resources)
                    } catch (_: Exception) {
                    }
                }
            }

            Image(
                modifier = Modifier.size(iconSmallSize),
                painter = rememberDrawablePainter(drawable = appInfoRestore.detailBase.appIcon),
                contentDescription = null
            )
            Column(
                modifier = Modifier
                    .padding(smallPadding, nonePadding)
                    .weight(1f)
            ) {
                Text(
                    text = appInfoRestore.detailBase.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = appInfoRestore.detailBase.packageName,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            FilledIconToggleButton(
                checked = appInfoRestore.selectApp,
                onCheckedChange = { appInfoRestore.selectApp = it }
            ) {
                if (appInfoRestore.selectApp) {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.ic_round_apps),
                        contentDescription = stringResource(id = R.string.application)
                    )
                } else {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.ic_round_apps),
                        contentDescription = stringResource(id = R.string.application)
                    )
                }
            }
            FilledIconToggleButton(
                checked = appInfoRestore.selectData,
                onCheckedChange = { appInfoRestore.selectData = it }
            ) {
                if (appInfoRestore.selectData) {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.ic_round_database),
                        contentDescription = stringResource(id = R.string.data)
                    )
                } else {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.ic_round_database),
                        contentDescription = stringResource(id = R.string.data)
                    )
                }
            }
        }
        var expand by remember { mutableStateOf(false) }
        Row {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(mediumPadding)
            ) {
                if (appInfoRestore.detailRestoreList.isNotEmpty()) {
                    var dateMenu by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                        SuggestionChip(
                            onClick = { dateMenu = true },
                            label = { Text(Command.getDate(appInfoRestore.detailRestoreList[appInfoRestore.restoreIndex].date)) }
                        )
                        DropdownMenu(
                            expanded = dateMenu,
                            onDismissRequest = { dateMenu = false }
                        ) {
                            val items = mutableListOf<String>()
                            appInfoRestore.detailRestoreList.forEach { items.add(Command.getDate(it.date)) }
                            for ((index, i) in items.withIndex()) {
                                DropdownMenuItem(
                                    text = { Text(i) },
                                    onClick = {
                                        appInfoRestore.restoreIndex = index
                                        dateMenu = false
                                    })
                            }
                        }
                    }
                }
                if (appInfoRestore.detailRestoreList[appInfoRestore.restoreIndex].sizeBytes != 0L) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text(appInfoRestore.detailRestoreList[appInfoRestore.restoreIndex].sizeDisplay) }
                    )
                }
            }
            IconToggleButton(checked = expand, onCheckedChange = { expand = it }) {
                if (expand) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = null
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
        }
        ExpandAnimation(expand) {
            if (it) {
                Row {
                    TextButton(onClick = { }) { Text(stringResource(R.string.delete)) }
                }
            }
        }
        Divider(modifier = Modifier.padding(nonePadding, tinyPadding))
    }
}
