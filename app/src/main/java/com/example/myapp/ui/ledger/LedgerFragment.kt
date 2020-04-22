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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.myapp.R
import com.example.myapp.ui.ledger.NotificationsViewModel
import com.example.myapp.ui.ledger.TakeInput
import com.example.myapp.ui.main.Model
import com.example.myapp.ui.main.MyAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton


class LedgerFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel
    var list = mutableListOf<Model>()
    private lateinit var listView: ListView
    private lateinit var root: View
    private lateinit var thisFragment:LedgerFragment
    private lateinit var requiredItems:ArrayList<String>

    init{
        list.add(Model("Yelahanka", "Satellite bus station", img = R.drawable.helpwe))
        list.add(Model("SVIT", "Canteen"))
        list.add(Model("SVIT", "Boys Hostel"))
        Log.d("LedgerFragment", "Init")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProviders.of(this).get(NotificationsViewModel::class.java)
        root = inflater.inflate(R.layout.fragment_ledger, container, false)
        listView = root.findViewById(R.id.listView)
        listView.adapter = MyAdapter(root.context, R.layout.row, list)

        listView.setOnItemClickListener { parent: AdapterView<*>, view: View, position:Int, id:Long ->
            Toast.makeText(root.context, "Clicked on" + list[position].landmarkName, Toast.LENGTH_LONG).show()
            val builder = AlertDialog.Builder(context)
            builder.setTitle(Html.fromHtml("<font size = '18'><b>Help!</b>"))

            val message = StringBuilder()
            if(!requiredItems.isEmpty()) {
                message.append("I am in great need of:").append("\n\n")
                for (item in requiredItems) {
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



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 6969){
            if(resultCode == 7070){
                val locationName = data!!.getStringExtra("Location")
                val landmark = data!!.getStringExtra("Landmark")
                val latLongAcc= data!!.getStringArrayListExtra("LatLongAcc")
                requiredItems = data!!.getStringArrayListExtra("CheckedItems")
                Log.d("LedgerFragment-onActivityResult", "location = $locationName")
                Log.d("LedgerFragment-onActivityResult", "landmark = $landmark")
                Log.d("LedgerFragment-onActivityResult", "landmark = $latLongAcc")
                list.add(Model(locationName, landmark, latLongAcc, requiredItems))
                listView.adapter = MyAdapter(root.context, R.layout.row, list)
            }
        }
    }


}