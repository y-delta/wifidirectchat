package com.example.myapp.ui.ledger

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapp.MainActivity
import com.example.myapp.MainActivity.Companion.NETWORK_USERID
import com.example.myapp.R
import com.example.myapp.databinding.ActivityChatListingBinding.inflate
import com.example.myapp.databinding.FragmentDirectmessageBinding
import com.example.myapp.databinding.FragmentLedgerBinding
import com.example.myapp.db.AppDatabase
import com.example.myapp.db.DatabaseUtil
import com.example.myapp.db.entity.GroupChatEntity
import com.example.myapp.db.entity.LedgerEntity
import com.example.myapp.ui.adapter.GroupMessageAdapter
import com.example.myapp.ui.adapter.LedgerAdapter
import com.example.myapp.ui.groupmessage.GroupMessageFragment
import com.example.myapp.ui.main.Model
import com.example.myapp.ui.main.MyAdapter
import com.example.myapp.utils.Constants
import com.example.myapp.utils.NPALinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class LedgerFragment : Fragment() {

    var list = mutableListOf<Model>()
    private lateinit var listView: ListView
    private lateinit var root: View
    private var requiredItems:ArrayList<String> = ArrayList()
    var binding: FragmentLedgerBinding? = null
    var appDatabase: AppDatabase? = null

    var mLedgerList: MutableList<LedgerEntity>? = null
    private lateinit var pullToRefresh: SwipeRefreshLayout

    init{
        Log.d("com.example.myapp.ui.ledger.LedgerFragment", "Init")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_ledger, container, false)
        listView = root.findViewById(R.id.listView) as ListView
        listView.adapter = MyAdapter(root.context, R.layout.row, list)
        listView.emptyView = root.findViewById(R.id.empty)
        ledgerFragmentCompanion = this

        appDatabase = AppDatabase.getDatabase(activity?.application)
        mLedgerList = ArrayList()
        chatHistory
        fetchLedgers()

        pullToRefresh = root.findViewById(R.id.swiperefresh)
        pullToRefresh.setOnRefreshListener { refresh() }

        listView.setOnItemClickListener { _: AdapterView<*>, _: View, position:Int, _:Long ->
            //Toast.makeText(root.context, "Clicked on" + list[position].landmarkName, Toast.LENGTH_LONG).show()
            val builder = AlertDialog.Builder(context)
            builder.setTitle(Html.fromHtml("<font size = '18'><b>Help!</b>"))

            val message = StringBuilder()
            if(!list[position].requiredItems.isNullOrEmpty()) {
                message.append("I am in great need of:").append("\n\n")
                for (item in list[position].requiredItems) {
                    message.append("     ○  ").append(item).append("\n\n")
                }
                message.delete(message.length - 2, message.length)

                builder.setMessage(message.toString())
            }
            else
            {
                builder.setMessage("")
            }
            builder.setPositiveButton("Show location") { _, id ->
                val latitude = list[position].latLongAcc[0].toDouble()
                val longitude = list[position].latLongAcc[1].toDouble()
                val gmmIntentUri: Uri = Uri.parse("geo:%f,%f?q=%f,%f".format(latitude, longitude, latitude, longitude)) //first 2 for map_view, next 2 for dropping pin
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
             builder.create().show()
        }

        val add = root.findViewById<FloatingActionButton>(R.id.add)
        add.setOnClickListener{
            val intent = Intent(root.context, TakeInput::class.java)
            intent.putExtra("key", "value")
            startActivityForResult(intent, 6969)
        }
        return root
    }

    private fun fetchLedgers()
    {
        list.clear()
        var i = 0
        while(i< mLedgerList?.size!!)
        {
            val fetchedData = mLedgerList!![i]

            val fetchedLocation = fetchedData.location
            val fetchedLandmark = fetchedData.landmark
            val fetchedLatitude = fetchedData.latitude
            val fetchedLongitude = fetchedData.longitude
            val fetchedAccuracy = fetchedData.accuracy
            val fetchedDate = fetchedData.date
            val fetchedSender = fetchedData.sender
            val fetchedNeeds = ArrayList(fetchedData.needs.split(","))

            Log.d("fetchedLocation", fetchedLocation)
            Log.d("fetchedLandmark", fetchedLandmark)
            Log.d("fetchedLatitude", fetchedLatitude)
            Log.d("fetchedLongitude", fetchedLongitude)
            Log.d("fetchedAccuracy", fetchedAccuracy)
            Log.d("fetchedNeeds", fetchedNeeds.toString())
            Log.d("fetchedDate", fetchedDate.toString())
            Log.d("fetchedSender", fetchedSender)

            list.add(Model(fetchedLocation, fetchedLandmark, arrayListOf(fetchedLatitude, fetchedLongitude, fetchedAccuracy), fetchedNeeds))
            i++
        }
        listView.adapter = MyAdapter(root.context, R.layout.row, list)
    }

    private val chatHistory: Unit
        private get() {
            val ledger = appDatabase?.ledgerDao()?.loadAllChatHistory()
            ledger?.observe(
                this,
                androidx.lifecycle.Observer <MutableList<LedgerEntity>>{
                        ledger ->
                    if (ledger != null) {
                        mLedgerList = ledger
                        fetchLedgers() //updates the list for every change in db
                        //but it's called every time the fragment loads up
                    }
                }
            )
        }

    private fun refresh ()
    {
        //clears the existing list and then fetches from db and updates the list
        //right now only current db entries show up
        //merging db between devices might be the solution
        refreshCount++
        Log.d("refresh()", "refresh called, now updating list")
        fetchLedgers()

        /*if(refreshCount%5==0){
            MainActivity.broadcastMessage("", Constants.REQUEST_TYPE_LEDGER_LIST)
        }*/

        Toast.makeText(root.context, "Updated", Toast.LENGTH_LONG).show()
        pullToRefresh.isRefreshing = false;
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 6969){
            if(resultCode == 7070){
                val ledgerEntity = LedgerEntity()
                val locationName = data!!.getStringExtra("Location")
                val landmark = data!!.getStringExtra("Landmark")
                val latLongAcc= data!!.getStringArrayListExtra("LatLongAcc")
                requiredItems = data!!.getStringArrayListExtra("CheckedItems")
                ledgerEntity.location = locationName.replace("\n", " ")
                ledgerEntity.landmark = landmark.replace("\n", " ")
                ledgerEntity.needs = requiredItems.joinToString(separator=",", transform = {it.toLowerCase().trim()})
                val dateAdded = Date(Date().toString())
                ledgerEntity.date = dateAdded // date is added here
                Log.d("DATE SECONDS", dateAdded.seconds.toString())
                ledgerEntity.sender = NETWORK_USERID
                ledgerEntity.latitude = latLongAcc[0]
                ledgerEntity.longitude = latLongAcc[1]
                ledgerEntity.accuracy = latLongAcc[2]

                Log.d("com.example.myapp.ui.ledger.LedgerFragment-onActivityResult", "location = $locationName")
                Log.d("com.example.myapp.ui.ledger.LedgerFragment-onActivityResult", "landmark = $landmark")
                Log.d("com.example.myapp.ui.ledger.LedgerFragment-onActivityResult", "landmark = $latLongAcc")
                DatabaseUtil.addNewLedgerToDataBase(appDatabase,ledgerEntity) //entry into db here

                //this code will broadcast the newly added
//                Log.d("Ledger list items", ledgerItem.needs)
                var preparedMsg = ""
                preparedMsg += dateAdded.toString() + "\n"        //date, landmark, location, needs, latitude, longitude, accuracy
                Log.d("DATE SECONDS", dateAdded.seconds.toString())
                preparedMsg += landmark.replace("\n", " ") + "\n"
                preparedMsg += locationName.replace("\n", " ") + "\n"
                preparedMsg += requiredItems.joinToString(separator=",", transform = {it.toLowerCase().trim()}) + "\n"
                preparedMsg += latLongAcc[0] + "\n"
                preparedMsg += latLongAcc[1] + "\n"
                preparedMsg += latLongAcc[2] + "\n"
                preparedMsg += NETWORK_USERID + "\n"
                Log.d("PreparedMessageLedger", preparedMsg)
                Log.d("TakeInput", "broadcasting this prepared message")
                MainActivity.broadcastMessage(preparedMsg, Constants.MESSAGE_TYPE_LEDGER)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("onDestroy()","yes")
        ledgerFragmentCompanion = null
    }

    companion object{
        var ledgerFragmentCompanion:LedgerFragment? = null
        var refreshCount = 0
    }


}