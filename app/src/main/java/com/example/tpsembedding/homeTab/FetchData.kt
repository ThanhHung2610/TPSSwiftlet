@file:Suppress("DEPRECATION")

package com.example.tpsembedding.homeTab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tpsembedding.HouseManager.WebApi
import com.example.tpsembedding.MainActivity
import com.example.tpsembedding.R
import com.example.tpsembedding.Utils
import com.example.tpsembedding.Utils.redirect2login
import com.example.tpsembedding.data.BirdPredictionDataChart
import com.example.tpsembedding.data.Birdprediction
import com.example.tpsembedding.data.DataManager
import com.example.tpsembedding.data.Flyin
import com.example.tpsembedding.data.Flyout
import com.example.tpsembedding.data.General
import com.example.tpsembedding.data.SharedViewModel
import com.example.tpsembedding.data.WeatherLabel
import com.example.tpsembedding.data.sizeOfWeatherIcon
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import com.google.android.material.tabs.TabLayout


class FetchData : Fragment() {

    private lateinit var generalLchart: LineChart
    private lateinit var flyinLchart:LineChart
    private lateinit var flyoutLchart:LineChart
    private lateinit var birdpredictionBchart:BarChart

    private lateinit var general_todayTv:TextView
    private lateinit var general_day1Tv:TextView
    private lateinit var general_day2Tv:TextView

    private lateinit var flyin_todayTv:TextView
    private lateinit var flyin_day1Tv:TextView
    private lateinit var flyin_day2Tv:TextView

    private lateinit var flyout_todayTv:TextView
    private lateinit var flyout_day1Tv:TextView
    private lateinit var flyout_day2Tv:TextView

    private lateinit var bp_predictionTv:TextView
    private lateinit var bp_prediction2Tv:TextView

    private lateinit var generaldata: General
    private lateinit var flyindata: Flyin
    private lateinit var flyoutdata: Flyout
    private lateinit var birdpredictiondata: Birdprediction
    private lateinit var birdPredictionDataDisplay: BirdPredictionDataChart
    private lateinit var weatherdata: WeatherLabel

    private lateinit var fdata_NscV:NestedScrollView
    private lateinit var fdata_swipe: SwipeRefreshLayout

    private lateinit var birdprediction_tl:TabLayout

    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var containerView: ViewGroup
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fetch_data, container, false)
    }

    private fun setupListeners(view: View){
        generalLchart = view.findViewById(R.id.general_lchart)
        flyinLchart = view.findViewById(R.id.flyin_lchart)
        flyoutLchart = view.findViewById(R.id.flyout_lchart)
        birdpredictionBchart = view.findViewById(R.id.birdprediction_bchart)

        general_todayTv = view.findViewById(R.id.general_today)
        general_day1Tv = view.findViewById(R.id.general_day1)
        general_day2Tv = view.findViewById(R.id.general_day2)

        flyin_todayTv = view.findViewById(R.id.flyin_today)
        flyin_day1Tv = view.findViewById(R.id.flyin_day1)
        flyin_day2Tv = view.findViewById(R.id.flyin_day2)

        flyout_todayTv = view.findViewById(R.id.flyout_today)
        flyout_day1Tv = view.findViewById(R.id.flyout_day1)
        flyout_day2Tv = view.findViewById(R.id.flyout_day2)

        bp_predictionTv = view.findViewById(R.id.bp_prediction)
        bp_prediction2Tv = view.findViewById(R.id.bp_prediction2)

        fdata_NscV = view.findViewById(R.id.fetch_data_nestedScV)
        fdata_swipe = view.findViewById(R.id.fetch_data_Swipe)

        birdprediction_tl = view.findViewById(R.id.birdprediction_Tl)

        general_todayTv.setOnClickListener { onLChartLegendClick(generalLchart,
            lchartLabels[0],general_todayTv, nameLChart[0]) }
        general_day1Tv.setOnClickListener { onLChartLegendClick(generalLchart,
            lchartLabels[1],general_day1Tv, nameLChart[0]) }
        general_day2Tv.setOnClickListener { onLChartLegendClick(generalLchart,
            lchartLabels[2],general_day2Tv, nameLChart[0]) }

        flyin_todayTv.setOnClickListener { onLChartLegendClick(flyinLchart,
            lchartLabels[0],flyin_todayTv, nameLChart[1]) }
        flyin_day1Tv.setOnClickListener { onLChartLegendClick(flyinLchart,
            lchartLabels[1],flyin_day1Tv, nameLChart[1]) }
        flyin_day2Tv.setOnClickListener { onLChartLegendClick(flyinLchart,
            lchartLabels[2],flyin_day2Tv, nameLChart[1]) }

        flyout_todayTv.setOnClickListener { onLChartLegendClick(flyoutLchart,
            lchartLabels[0],flyout_todayTv, nameLChart[2]) }
        flyout_day1Tv.setOnClickListener { onLChartLegendClick(flyoutLchart,
            lchartLabels[1],flyout_day1Tv, nameLChart[2]) }
        flyout_day2Tv.setOnClickListener { onLChartLegendClick(flyoutLchart,
            lchartLabels[2],flyout_day2Tv, nameLChart[2]) }

        bp_predictionTv.setOnClickListener { onBirdPredictionLegendClick(birdpredictionBchart,
            bp_labels[0],bp_predictionTv) }
        bp_prediction2Tv.setOnClickListener { onBirdPredictionLegendClick(birdpredictionBchart,
            bp_labels[1],bp_prediction2Tv) }

        fdata_NscV.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            // Check if the user has scrolled to the top
            if (scrollY == 0) {
                fdata_swipe.isEnabled = true // Enable SwipeRefreshLayout
            } else {
                fdata_swipe.isEnabled = false // Disable SwipeRefreshLayout
            }
        }
        fdata_swipe.setOnRefreshListener {
            // Handle refresh action
            refreshDataRecord()
            fdata_swipe.isRefreshing = false // Hide the refreshing indicator when done
        }
        birdprediction_tl.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Refresh chart with dataset corresponding to Type Tab
                val labels = getLabelsChart(birdpredictionBchart)
                refeshBirdPredictionChart(birdpredictionBchart,labels)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        containerView = view.findViewById(R.id.fetch_data_view_container)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        // Observe LiveData for changes
        sharedViewModel.noDataAvailable.observe(viewLifecycleOwner, Observer { noDataAvailable ->
            updateView(noDataAvailable)
        })
        sharedViewModel.currentHouseId.observe(viewLifecycleOwner, Observer { currentHouseId ->
            if (sharedViewModel.noDataAvailable.value!=true) {
                fetchData(currentHouseId)
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    private fun updateView(noDataAvailable:Boolean){
        containerView.removeAllViews()
        if (noDataAvailable){
            val newView = LayoutInflater.from(requireContext()).inflate(R.layout.no_data_available,containerView, false)
            containerView.addView(newView)
        }
        else{
            val newView = LayoutInflater.from(requireContext()).inflate(R.layout.fetch_data, containerView, false)
            containerView.addView(newView)
            // Reinitialize listeners on the new view
            setupListeners(newView)
        }
    }

    private fun refreshDataRecord(){
        val selectedHouseId = sharedViewModel.currentHouseId.value
        if (selectedHouseId != null) {
            fetchData(selectedHouseId)
        }
    }
    private fun resetLegendType(list:List<TextView>){
        for (i in 0 until list.size){
            // Remove strikethrough effect
            list[i].paintFlags = list[i].paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }
    private fun getLabelsChart(chart: BarLineChartBase<*>):MutableList<String>{
        return chart.legend.entries.map { it.label }.toMutableList()
    }

    // Starting block handling line chart
    private fun onLChartLegendClick(chart:LineChart,label: String, legend: TextView, nameChart: String){
        chart.data?.let {
            val labels = getLabelsChart(chart)
            if (label in labels){
                labels.remove(label)
                // Add strikethrough effect
                legend.paintFlags = legend.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            else{
                labels.add(label)
                // Remove strikethrough effect
                legend.paintFlags = legend.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            chart.data = LineData(createLChartDatasets(labels,nameChart))
            // Reset the affine transformation of the chart
            chart.fitScreen()
            // Notify to recalculate properties depend on chart's data: axis range, minimum, maximum values,...
            chart.notifyDataSetChanged()
            // Update to UI
            chart.invalidate()
        }
    }
    private fun createLChartDatasets(labels: List<String>, nameChart:String):MutableList<ILineDataSet>{
        var datasets = mutableListOf<ILineDataSet>()
        if (nameChart == "General"){
            datasets = createGeneralDatasets(labels)
        }
        else if (nameChart == "Fly in"){
            datasets = createFlyinDatasets(labels)
        }
        else if (nameChart == "Fly out"){
            datasets = createFlyoutDatasets(labels)
        }
        return datasets
    }

    private fun drawFlyinChart(data:Flyin?){
        data?.let {
            val linedata = LineData(createFlyinDatasets(lchartLabels))
            mainHandler.post {
                resetLegendType(listOf(flyin_todayTv,flyin_day1Tv,flyin_day2Tv))
                drawLineChart(flyinLchart,linedata,data.labels)
            }
        }
    }
    private fun drawFlyoutChart(data:Flyout?){
        data?.let {
            val linedata = LineData(createFlyoutDatasets(lchartLabels))
            mainHandler.post {
                resetLegendType(listOf(flyout_todayTv,flyout_day1Tv,flyout_day2Tv))
                drawLineChart(flyoutLchart,linedata,data.labels)
            }
        }
    }
    private fun drawGeneralChart(data:General?){
        data?.let {
            val linedata = LineData(createGeneralDatasets(lchartLabels))
            mainHandler.post {
                resetLegendType(listOf(general_todayTv,general_day1Tv,general_day2Tv))
                drawLineChart(generalLchart,linedata,data.labels)
            }
        }
    }

    private fun drawLineChart(lchart:LineChart, data:LineData, labels: List<String>){

        lchart.data = data
        lchart.setBackgroundColor(resources.getColor(R.color.white))

        lchart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lchart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        //xAxis.labelCount = data.labels.size // Set number of labels
        lchart.xAxis.labelRotationAngle = -45f

        // Set custom XAxisRenderer for top axis
        lchart.setXAxisRenderer( CustomXAxisRenderer(
            lchart.viewPortHandler,
            lchart.xAxis,
            lchart.getTransformer(YAxis.AxisDependency.LEFT),
            weatherdata.weather,
            weatherdata.weather2,
            weatherdata.weather3,
            requireContext()
        ))


        val yAxisLeft = lchart.axisLeft
        yAxisLeft.axisMinimum = 0f
        //yAxisLeft.axisMaximum = 180f
        // Adjust chart padding
        lchart.setExtraOffsets(0f, sizeOfWeatherIcon, 0f, 15f)

        lchart.axisRight.isEnabled = false
        lchart.legend.isEnabled = false
        lchart.description.isEnabled = false

        lchart.animate()
        // Reset the affine transformation of the chart
        lchart.fitScreen()
        lchart.invalidate() // refresh
    }

    private fun createFlyoutDatasets(labels: List<String>):MutableList<ILineDataSet>{
        var datasets = mutableListOf<ILineDataSet>()
        flyoutdata?.let {
            for (i in 0 until labels.size){
                val dataset = when(labels[i]){
                    lchartLabels[0]->{createLineDataset(it.flyOut, lchartLabels[0],resources.getColor(R.color.rouge))}
                    lchartLabels[1]->{createLineDataset(it.flyOut2, lchartLabels[1],resources.getColor(R.color.forest_green))}
                    lchartLabels[2]->{createLineDataset(it.flyOut3, lchartLabels[2],resources.getColor(R.color.cobalt_blue))}
                    else -> {null}
                }
                if (dataset != null) {
                    datasets.add(dataset)
                }
            }
        }
        return datasets
    }
    private fun createFlyinDatasets(labels: List<String>):MutableList<ILineDataSet>{
        var datasets = mutableListOf<ILineDataSet>()
        flyindata?.let {
            for (i in 0 until labels.size){
                val dataset = when(labels[i]){
                    lchartLabels[0]->{createLineDataset(it.flyIn, lchartLabels[0],resources.getColor(R.color.rouge))}
                    lchartLabels[1]->{createLineDataset(it.flyIn2, lchartLabels[1],resources.getColor(R.color.forest_green))}
                    lchartLabels[2]->{createLineDataset(it.flyIn3, lchartLabels[2],resources.getColor(R.color.cobalt_blue))}
                    else -> {null}
                }
                if (dataset != null) {
                    datasets.add(dataset)
                }
            }
        }
        return datasets
    }
    private fun createGeneralDatasets(labels: List<String>):MutableList<ILineDataSet>{
        var datasets = mutableListOf<ILineDataSet>()
        generaldata?.let {
            for (i in 0 until labels.size){
                val dataset = when(labels[i]){
                    lchartLabels[0]->{createLineDataset(it.data, lchartLabels[0],resources.getColor(R.color.rouge))}
                    lchartLabels[1]->{createLineDataset(it.data1, lchartLabels[1],resources.getColor(R.color.forest_green))}
                    lchartLabels[2]->{createLineDataset(it.data2, lchartLabels[2],resources.getColor(R.color.cobalt_blue))}
                    else -> {null}
                }
                if (dataset != null) {
                    datasets.add(dataset)
                }
            }
        }
        return datasets
    }
    private fun createLineDataset(data: List<Int>, type:String, color: Int):LineDataSet{
        val entries = data.mapIndexed { index, value -> Entry(index.toFloat(),value.toFloat()) }
        val dataset = LineDataSet(entries,type)
        dataset.color = color
        dataset.setDrawCircles(false)
        dataset.setDrawValues(false)
        dataset.setDrawHighlightIndicators(false)  // Disable crosshairs
        return dataset
    }
    // Ending block handling line chart

    // Starting block handling bar chart
    private fun refeshBirdPredictionChart(chart:BarChart,labels: List<String>){
        getBirdPredictionDataToDisplay(labels)
        chart.data = birdPredictionDataDisplay.data
        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(birdPredictionDataDisplay.labels)
        //xAxis.labelCount = birdPredictionDataDisplay.labels.size // Set number of labels
        // config for Group BarChart
        if (labels.size>1) {
            // Set bar width and group bars
            val barWidth = 0.3f
            val groupSpace = 0.2f
            val barSpace = 0.1f

            chart.data.barWidth = barWidth
            chart.groupBars(0f, groupSpace, barSpace)
        }
        else{
            chart.data.barWidth = 0.6f
        }
        // Reset the affine transformation of the chart
        chart.fitScreen()
        // Notify to recalculate properties depend on chart's data: axis range, minimum, maximum values,...
        chart.notifyDataSetChanged()
        // Update to UI
        chart.invalidate()
    }
    private fun onBirdPredictionLegendClick(chart: BarChart, label: String, legend: TextView){
        chart.data?.let {
            val labels = getLabelsChart(chart)
            if (label in labels){
                labels.remove(label)
                // Add strikethrough effect
                legend.paintFlags = legend.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }else{
                labels.add(label)
                // Remove strikethrough effect
                legend.paintFlags = legend.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            refeshBirdPredictionChart(chart,labels)
        }
    }
    private fun drawBirdPredictionChart(data:Birdprediction?){
        data?.let {
            getBirdPredictionDataToDisplay(bp_labels)
            mainHandler.post {
                resetLegendType(listOf(bp_predictionTv,bp_prediction2Tv))
                drawBarChart(birdpredictionBchart,birdPredictionDataDisplay.data,birdPredictionDataDisplay.labels)
            }
        }
    }
    private fun drawBarChart(bchart:BarChart,bardata:BarData,labels: ArrayList<String>){
        bchart.data = bardata
        bchart.setBackgroundColor(resources.getColor(R.color.white))
        // Set bar width and group bars // barwidtg*2 + groupspace + barspace = 0.9
        val barWidth = 0.3f
        val groupSpace = 0.2f
        val barSpace = 0.1f

        bardata.barWidth = barWidth
        bchart.groupBars(0f, groupSpace, barSpace)
        // Calculate the group width
//        val groupWidth = bchart.data.getGroupWidth(groupSpace, barSpace)
//        val i = groupWidth

        val xAxis = bchart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.setCenterAxisLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //xAxis.labelCount = birdPredictionDataDisplay.labels.size // Set number of labels
        xAxis.labelRotationAngle = -45f
        //xAxis.axisMinimum = 0f
        xAxis.granularity = 1.0f

        // Adjust chart padding
        bchart.setExtraOffsets(0f, 0f, 0f, 15f)

        val yAxisLeft = bchart.axisLeft
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.granularity = 10f
        yAxisLeft.axisMinimum = 0f

        bchart.axisRight.isEnabled = false
        bchart.legend.isEnabled = false
        bchart.description.isEnabled = false
        bchart.animate()
        // Reset the affine transformation of the chart
        bchart.fitScreen()
        bchart.invalidate() // refresh
    }
    private fun getBirdPredictionDataToDisplay(labels: List<String>){
        val bp_labels_list = birdpredictiondata.labelPrediction
        // Get type of bprediction chart
        val currentType = birdprediction_tl.getTabAt(birdprediction_tl.selectedTabPosition)
        val sizeOfData = bp_labels_list.size
        val labelsDisplay = if (labels.size == 2) bp_labels else labels

        var bardata: BarData
        var labels_cut : ArrayList<String>
        if (currentType?.text == "Week"){
            bardata = BarData(createBirdPredictionDatasets(labelsDisplay, fromIndex = sizeOfData - countOfWeek))
            labels_cut = ArrayList(bp_labels_list.subList(sizeOfData- countOfWeek,sizeOfData))
        }
        else if (currentType?.text == "2 Weeks")
        {
            bardata = BarData(createBirdPredictionDatasets(labelsDisplay, fromIndex = sizeOfData - countOf2Weeks))
            labels_cut = ArrayList(bp_labels_list.subList(sizeOfData - countOf2Weeks,sizeOfData))
            birdPredictionDataDisplay = BirdPredictionDataChart(labels_cut,bardata)
        }
        else{
            bardata = BarData(createBirdPredictionDatasets(labelsDisplay, fromIndex = 0))
            labels_cut = bp_labels_list
        }
        bardata.setValueFormatter(YBarChartValueFormatter())
        birdPredictionDataDisplay = BirdPredictionDataChart(labels_cut,bardata)
    }
    private fun createBirdPredictionDatasets(labels: List<String>, fromIndex:Int):MutableList<IBarDataSet>{
        var datasets = mutableListOf<IBarDataSet>()
        birdpredictiondata?.let {
            val prediction_cut = ArrayList(it.prediction.subList(fromIndex,it.prediction.size))
            val prediction2_cut = ArrayList(it.prediction2.subList(fromIndex,it.prediction2.size))
            for(i in 0 until labels.size){
                val dataset = when(labels[i]){
                    bp_labels[0] -> {createBarDataset(prediction_cut,
                        bp_labels[0], resources.getColor(R.color.rouge), 11.0f)}
                    bp_labels[1] -> {createBarDataset(prediction2_cut,
                        bp_labels[1], resources.getColor(R.color.forest_green), 11.0f) }
                    else -> {null}
                }
                if (dataset != null) {
                    datasets.add(dataset)
                }
            }
        }
        return datasets
    }
    private fun createBarDataset(data: ArrayList<Int>, type:String, color:Int, size:Float):BarDataSet{
        val entries = data.mapIndexed { index, value -> BarEntry(index.toFloat()+0.5f,value.toFloat()) }
        val dataset = BarDataSet(entries,type)
        dataset.color = color
        dataset.valueTextSize = size
        return dataset
    }
    // Ending block handling bar chart


    private fun fetchData(houseId:Long){
        val url = WebApi.getRecordUrl(houseId)

        CoroutineScope(Dispatchers.IO).launch {
            val response_data = Utils.makeGetRequest(url)
            response_data?.let {
                if (it == WebApi.UNAUTHENTICATED_MESSAGE){
                    withContext(Dispatchers.Main) {
                        redirect2login(requireActivity())
                    }
                }else {
                    generaldata = DataManager.json.decodeFromString<General>(it)
                    flyindata = DataManager.json.decodeFromString<Flyin>(it)
                    flyoutdata = DataManager.json.decodeFromString<Flyout>(it)
                    birdpredictiondata = DataManager.json.decodeFromString<Birdprediction>(it)
                    weatherdata = DataManager.json.decodeFromString<WeatherLabel>(it)
                    //weatherdata = WeatherLabel(arrayListOf("0"),arrayListOf("2"),arrayListOf("3"))

                    withContext(Dispatchers.Main) {
                        drawGeneralChart(generaldata)
                        drawFlyinChart(flyindata)
                        drawFlyoutChart(flyoutdata)
                        drawBirdPredictionChart(birdpredictiondata)
                    }
                }
            }
        }

    }

    companion object {
        val lchartLabels = listOf("Today","Day 1","Day 2")
        val bp_labels = listOf("Prediction","Prediction2")
        val nameLChart = listOf("General","Fly in","Fly out")
        val countOfWeek=  7
        val countOf2Weeks = 14
        // Custom ValueFormatter
        class YBarChartValueFormatter : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry?): String {
                return if (barEntry != null && barEntry.y != 0f) {
                    barEntry.y.toInt().toString()
                } else {
                    ""
                }
            }
        }

    }
}

class CustomXAxisRenderer(
    viewPortHandler: ViewPortHandler,
    xAxis: XAxis,
    trans: Transformer,
    private val iconLabels: List<String>,
    private val iconLabels1: List<String>,
    private val iconLabels2: List<String>,
    private val context: Context
) : XAxisRenderer(viewPortHandler, xAxis, trans) {

    override fun drawLabels(c: Canvas, pos: Float, anchor: MPPointF) {
        // Draw string labels first (default behavior)
        super.drawLabels(c, pos, anchor)
        drawWeatherIcons(context,c,0,iconLabels)
        drawWeatherIcons(context,c,1,iconLabels1)
        drawWeatherIcons(context,c,2,iconLabels2)
    }

    fun drawWeatherIcons(context: Context, c: Canvas, day: Int, iconLabels: List<String>){

        val iconSize = sizeOfWeatherIcon.toInt() // Define icon size
        var isDrawIcon = false
        var icon:Drawable = ContextCompat.getDrawable(context,R.drawable.swiftlet)!!

        val mapIcon = getWeatherIcons(context,day)

        for (i in iconLabels.indices){
            // https://openweathermap.org/weather-conditions
            val weatherCode = iconLabels[i].toInt()
            if (200 <= weatherCode && weatherCode < 260){
                // Thunderstorm
                isDrawIcon = true
                icon = mapIcon["thunderstorm"]!!
            }
            else if (300 <= weatherCode && weatherCode < 330){
                // Drizzle
                isDrawIcon = true
                icon = mapIcon["drizzle"]!!
            }
            else if (500 <= weatherCode && weatherCode < 550)
            {
                // Rain
                isDrawIcon = true
                icon = mapIcon["rain"]!!
            }
            if (isDrawIcon){
                val position:FloatArray = floatArrayOf(i.toFloat(),0f)
                // Calculate positions for icons in the chart
                mTrans.pointValuesToPixel(position)
                // Calculate the position for the top of the icon
                val iconTop = getPositionOfIcons(day)  // Adjust position above the label

                icon.setBounds(
                    (position[0] - iconSize / 2).toInt(),
                    iconTop.toInt(),
                    (position[0] + iconSize / 2).toInt(),
                    (iconTop + iconSize).toInt()
                )
                icon.draw(c)
                isDrawIcon = false
            }
        }
    }
    fun getPositionOfIcons(day: Int):Float{
        return when (day){
            0 -> 0f
            1 -> sizeOfWeatherIcon
            2 -> sizeOfWeatherIcon*2
            else -> 0f
        }
    }
    fun getWeatherIcons(context: Context, day:Int):Map<String,Drawable?>{
        when(day)
        {
            0 -> return mapOf(
                "drizzle" to ContextCompat.getDrawable(context, R.drawable.drizzle0),
                "rain" to ContextCompat.getDrawable(context, R.drawable.rain0),
                "thunderstorm" to ContextCompat.getDrawable(context, R.drawable.thunderstorm0)
            )

            1 -> return mapOf(
                "drizzle" to ContextCompat.getDrawable(context, R.drawable.drizzle1),
                "rain" to ContextCompat.getDrawable(context, R.drawable.rain1),
                "thunderstorm" to ContextCompat.getDrawable(context, R.drawable.thunderstorm1)
            )

            2 -> return mapOf(
                "drizzle" to ContextCompat.getDrawable(context, R.drawable.drizzle2),
                "rain" to ContextCompat.getDrawable(context, R.drawable.rain2),
                "thunderstorm" to ContextCompat.getDrawable(context, R.drawable.thunderstorm2)
            )
            else -> return mapOf()
        }
    }
}