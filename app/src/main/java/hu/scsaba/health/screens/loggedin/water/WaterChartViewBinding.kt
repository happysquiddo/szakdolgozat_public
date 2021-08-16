package hu.scsaba.health.screens.loggedin.water

import android.util.Log
import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.LiveData
import com.github.aachartmodel.aainfographics.aachartcreator.*
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AADataLabels
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R

@Composable
fun WaterChartViewBinding(dataSet : LiveData<MutableMap<String, Int>>, colors: Colors, startObserving : () -> Unit ) {

    val owner = LocalLifecycleOwner.current
    AndroidViewBinding(hu.scsaba.health.databinding.ChartViewBinding::inflate) {
        var drawn = false
        startObserving()
        var data : MutableList<Int> = mutableListOf()
        var categories: MutableList<String> = mutableListOf()
        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Column)
            .legendEnabled(false)
            .zoomType(AAChartZoomType.XY)
            .title("")
            .titleStyle(AAStyle().color(colors.onSurface.toHex()))
            .backgroundColor(colors.background.toHex())
            .dataLabelsEnabled(true)
            .xAxisGridLineWidth(0f)
            .yAxisGridLineWidth(0f)
            .tooltipEnabled(false)
            .borderRadius(5f)
            .yAxisTitle(HealthApplication.Strings.get(R.string.glasses_of_water))

        dataSet.observe(owner) {
            if(it.values.isNotEmpty() && !drawn){
                data = it.values.toMutableList()
                categories = it.keys.toMutableList()
                this.chartLayout.aa_drawChartWithChartModel(
                    aaChartModel
                        .categories(
                            it.keys.toTypedArray()
                        )
                        .series(
                            arrayOf(
                                AASeriesElement().data(
                                    it.values.toTypedArray()
                                ).color(colors.secondary.toHex())
                                    .dataLabels(
                                        AADataLabels()
                                            .color(colors.onPrimary.toHex())
                                            .borderColor(colors.onPrimary.toHex())
                                    )
                            )
                        )
                )
                drawn = true
            }
            if(data != it.values){
                data = it.values.toMutableList()
                this.chartLayout.aa_onlyRefreshTheChartDataWithChartOptionsSeriesArray(arrayOf(
                    AASeriesElement().data(
                        it.values.toTypedArray()
                    ))
                )
                if(!categories.containsAll(it.keys)){
                    categories = it.keys.toMutableList()
                    this.chartLayout.aa_refreshChartWithChartModel(
                        aaChartModel
                            .categories(
                                categories.toTypedArray()
                            )
                            .series(
                                arrayOf(
                                    AASeriesElement().data(
                                        data.toTypedArray()
                                    ).color(colors.secondary.toHex())
                                        .dataLabels(
                                            AADataLabels()
                                                .color(colors.onPrimary.toHex())
                                                .borderColor(colors.onPrimary.toHex())
                                        )
                                )
                            )
                    )
                }
            }
        }
    }
}

private fun Color.toHex() : String{
    return "#"+ Integer.toHexString(this.toArgb()).takeLast(6)
}