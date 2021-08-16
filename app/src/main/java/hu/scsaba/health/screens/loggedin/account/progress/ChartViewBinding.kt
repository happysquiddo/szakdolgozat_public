package hu.scsaba.health.screens.loggedin.account.progress

import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartZoomType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AADataLabels
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle
import hu.scsaba.health.HealthApplication
import hu.scsaba.health.R
import hu.scsaba.health.model.entities.progress.ProgressEntity

@Composable
fun ChartViewBinding(dataSet : List<ProgressEntity>, colors: Colors) {


    AndroidViewBinding(hu.scsaba.health.databinding.ChartViewBinding::inflate) {

        val data: MutableList<Int> = mutableListOf()
        dataSet.forEach {
            data.add(it.workoutsDone)
        }
        val categories: MutableList<String> = mutableListOf()
        dataSet.forEach {
            categories.add(it.weekOfYear.toString() + ". " + HealthApplication.Strings.get(R.string.week))
        }

        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Column)
            .legendEnabled(false)
            .zoomType(AAChartZoomType.XY)
            .title(HealthApplication.Strings.get(R.string.workouts_per_week))
            .titleStyle(AAStyle().color(colors.onSurface.toHex()))
            .backgroundColor(colors.background.toHex())
            .dataLabelsEnabled(true)
            .xAxisGridLineWidth(0f)
            .yAxisGridLineWidth(0f)
            .tooltipEnabled(false)
            .borderRadius(22f)
            .yAxisTitle(HealthApplication.Strings.get(R.string.workouts))

            .categories(
                categories.toTypedArray()
            )
            .series(
                arrayOf(
                    AASeriesElement().data(
                        data.toTypedArray()
                    ).color(colors.secondary.toHex())
                        .dataLabels(AADataLabels()
                            .color(colors.onPrimary.toHex())
                            .borderColor(colors.onPrimary.toHex())
                        )
                )
            )
        this.chartLayout.aa_drawChartWithChartModel(aaChartModel)
    }
}

private fun Color.toHex() : String{
    return "#"+Integer.toHexString(this.toArgb()).takeLast(6)
}
