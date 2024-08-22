package com.example.tpsembedding.homeTab
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tpsembedding.HouseManager.WebApi
import com.example.tpsembedding.R
import com.example.tpsembedding.Utils
import com.example.tpsembedding.data.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class FetchDaily : Fragment() {
    private lateinit var mrecordsTl:TableLayout

    private lateinit var fdaily_ScV: ScrollView
    private lateinit var fdaily_swipe: SwipeRefreshLayout

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
        val t = inflater.inflate(R.layout.fragment_fetch_daily, container, false)
        return t
    }

    private fun setupListeners(view: View){
        mrecordsTl = view.findViewById(R.id.recordsTl)

        fdaily_ScV = view.findViewById(R.id.fetch_daily_SrV)
        fdaily_swipe = view.findViewById(R.id.fetch_daily_swipe)

        fdaily_ScV.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            // Check if the user has scrolled to the top
            if (scrollY == 0) {
                fdaily_swipe.isEnabled = true // Enable SwipeRefreshLayout
            } else {
                fdaily_swipe.isEnabled = false // Disable SwipeRefreshLayout
            }
        }

        fdaily_swipe.setOnRefreshListener {
            // Handle refresh action
            refreshDataRecord()
            fdaily_swipe.isRefreshing = false // Hide the refreshing indicator when done
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        containerView = view.findViewById(R.id.fetch_daily_view_container)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        // Observe LiveData for changes
        sharedViewModel.noDataAvailable.observe(viewLifecycleOwner, Observer { noDataAvailable ->
            updateView(noDataAvailable)
        })
        sharedViewModel.currentHouseId.observe(viewLifecycleOwner, Observer { currentHouseId ->
            if (sharedViewModel.noDataAvailable.value!=true) {
                fetchDailyData(currentHouseId)
            }
        })
    }

    private fun updateView(noDataAvailable:Boolean){
        containerView.removeAllViews()
        if (noDataAvailable){
            val newView = LayoutInflater.from(requireContext()).inflate(R.layout.no_data_available,containerView, false)
            containerView.addView(newView)
        }
        else{
            val newView = LayoutInflater.from(requireContext()).inflate(R.layout.fetch_daily, containerView, false)
            containerView.addView(newView)
            // Reinitialize listeners on the new view
            setupListeners(newView)
        }
    }

    private fun refreshDataRecord(){
        val selectedHouseId = sharedViewModel.currentHouseId.value
        if (selectedHouseId != null) {
            fetchDailyData(selectedHouseId)
        }
    }
    private fun creatCellOfTableRow(layoutParams:TableRow.LayoutParams, text:String, gravity: Int = Gravity.CENTER):TextView {
        val cell = TextView(requireContext())
        cell.text = text
        cell.setPadding(8, 8, 8, 8)
        cell.setBackgroundResource(R.drawable.cell_border)
        cell.layoutParams = layoutParams
        cell.gravity = gravity
        return cell
    }
    private fun fetchDailyData(houseId:Long){
        val url = WebApi.getRecordDailyUrl(houseId)

        mrecordsTl.removeAllViews()
        val layoutParams = TableRow.LayoutParams()
        layoutParams.weight = 1.0f // set layout_weight

        CoroutineScope(Dispatchers.IO).launch {
            val response = Utils.makeGetRequest(url)
            response?.let { it ->
                if (it == WebApi.UNAUTHENTICATED_MESSAGE) {
                    withContext(Dispatchers.Main) {
                        Utils.redirect2login(requireActivity())
                    }
                } else {
                    var values: JSONArray?
                    withContext(Dispatchers.Default) {
                        val jsonObject = JSONObject(it)
                        values = jsonObject.getJSONArray("recorddaily")
                    }
                    withContext(Dispatchers.Main) {
                        var newRow = TableRow(requireContext())
                        var cell = creatCellOfTableRow(layoutParams, "Date")
                        cell.setTypeface(cell.typeface, Typeface.BOLD)
                        newRow.addView(cell)
                        cell = creatCellOfTableRow(layoutParams, "Window")
                        cell.setTypeface(cell.typeface, Typeface.BOLD)
                        newRow.addView(cell)
                        cell = creatCellOfTableRow(layoutParams, "Count")
                        cell.setTypeface(cell.typeface, Typeface.BOLD)
                        newRow.addView(cell)
                        mrecordsTl.addView(newRow)

                        values?.let {
                            for (i in 0 until it.length()) {
                                val date = formatTime(it.getJSONObject(i).getString("date"))
                                val holeId = it.getJSONObject(i).getString("holeId")
                                val count = it.getJSONObject(i).getString("count")
                                // Create a new row
                                newRow = TableRow(requireContext())
                                newRow.addView(creatCellOfTableRow(layoutParams, date))
                                newRow.addView(creatCellOfTableRow(layoutParams, holeId))
                                newRow.addView(creatCellOfTableRow(layoutParams, count))
                                // Add the row to the table layout
                                mrecordsTl.addView(newRow)
                            }
                        }
                    }
                }
            }
        }
    }
    private fun formatTime(time:String):String{
        // time: YYYY-MM-DDTxx:xx:xx
        val ymd = time.removeSuffix("T00:00:00")
        val year = ymd.substring(0,4).toInt()
        val month = ymd.substring(5,7).toInt()
        val day = ymd.substring(8,10).toInt()
        return "${months[month - 1]} $day, $year"
    }

    companion object {
        val months = mutableListOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }
}