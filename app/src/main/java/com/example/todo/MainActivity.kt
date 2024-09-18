package com.example.todo

import android.app.ActionBar.LayoutParams
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.marginBottom
import androidx.core.view.setMargins
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.datepicker.MaterialTextInputPicker
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter


class MainActivity : ComponentActivity() {
    companion object{
        private const val GET_REQUEST_CODE = 1
        private const val SAVE_REQUEST_CODE = 2
    }

    lateinit var filestr : String
    lateinit var fileName : String
    lateinit var resultStr: String

    var ticketArr : MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainlayout)

        findViewById<Button>(R.id.add_ticket_btn).setOnClickListener {
            val el = mutableMapOf(
                "title" to "...",
                "description" to "...",
                "state" to "doing"
            )
            val id = ticketArr.keys.size + 1
            ticketArr.put(id.toString(), el)

            addTicket(el, id.toString(), findViewById(R.id.tickets))
        }

        findViewById<MaterialButton>(R.id.save).setOnClickListener {
            val resJson = JSONObject()
            val doneTickets = ticketArr.filter { (key, value) ->
                value["state"] == "done"
            }
            val doingTickets = ticketArr.filter { (key, value) ->
                value["state"] == "doing"
            }

            Log.i("ASL:KFJ:OQIWJF:LIAJWFPOAIWJF", doneTickets.values.joinToString(separator = "\n"))

            val doneJson = JSONArray(doneTickets.values)
            val doingJson = JSONArray(doingTickets.values)

            resJson.put("done", doneJson)
            resJson.put("doing", doingJson)

            resultStr = resJson.toString()
            Log.i("resStr", resultStr)

            val saveFileIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "file/json"
                putExtra(Intent.EXTRA_TITLE, "newBoard.json")
            }
            this.startActivityForResult(saveFileIntent, SAVE_REQUEST_CODE)

            setStateEnd()
        }

        findViewById<MaterialButton>(R.id.newWorkplaceBtn).setOnClickListener {
            setStateWorking()
        }

        findViewById<MaterialButton>(R.id.get_btn).setOnClickListener{
            val getFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            this.startActivityForResult(getFileIntent, GET_REQUEST_CODE)
        }
    }

    fun setStateEnd(){
        findViewById<MaterialButton>(R.id.get_btn).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.add_ticket_btn).visibility = View.GONE
        findViewById<MaterialButton>(R.id.newWorkplaceBtn).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.save).visibility = View.GONE

        findViewById<LinearLayout>(R.id.tickets).removeAllViews()
        ticketArr = mutableMapOf()
    }

    fun setStateWorking(){
        findViewById<MaterialButton>(R.id.get_btn).visibility = View.GONE
        findViewById<MaterialButton>(R.id.add_ticket_btn).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.newWorkplaceBtn).visibility = View.GONE
        findViewById<MaterialButton>(R.id.save).visibility = View.VISIBLE

        Toast.makeText(this, "working", Toast.LENGTH_SHORT).show()

        val container = findViewById<LinearLayout>(R.id.tickets)

        for (el in ticketArr.keys){
            addTicket(ticketArr[el] as MutableMap<String, String>, "done", container)
        }
    }



    fun addTicket(content: MutableMap<String, String>, id: String, container : LinearLayout){
        val card = MaterialCardView(this)
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        layoutParams.setMargins(16)
        card.layoutParams = layoutParams
        val mainLinearLayout = LinearLayout(this)

        val textLinearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val label = TextView(this).apply {
            text = content.get("title").toString()
        }
        val desc = TextView(this).apply {
            text = content.get("description").toString()
        }


        val doneCheck = MaterialCheckBox(this).apply {
            checkedState = if (content["state"]=="done") MaterialCheckBox.STATE_CHECKED
            else MaterialCheckBox.STATE_UNCHECKED
        }

        doneCheck.setOnClickListener {
            ticketArr[id]?.put("state", if (ticketArr[id]?.get("state") == "done") "doing" else "done")
        }

        var clicked = false

        card.setOnClickListener{
            if (!clicked){
                clicked = true
                val newCard = MaterialCardView(this)

                Toast.makeText(this, content["title"], Toast.LENGTH_SHORT).show()

                val titleField = EditText(this).apply {
                    hint = "Title"
                }
                val descField = EditText(this).apply {
                    hint = "Description"
                }

                val delBtn = Button(this).apply {
                    text="DELETE"
                }
                val submitBtn = Button(this).apply {
                    text="SUBMIT"
                }

                val newMainLineLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                }

                val btnLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                submitBtn.setOnClickListener {
                    ticketArr.put(id, mutableMapOf(
                        "title" to titleField.text.toString(),
                        "description" to descField.text.toString(),
                        "state" to content["state"] as String
                    ))
                    container.removeView(newCard)
                    label.text = titleField.text.toString()
                    desc.text = descField.text.toString()
                    clicked = false
                    card.invalidate()
                }

                delBtn.setOnClickListener {
                    ticketArr.remove(id)
                    container.removeView(card)
                    container.removeView(newCard)
                }

                newMainLineLayout.addView(titleField)
                newMainLineLayout.addView(descField)
                btnLayout.addView(delBtn)
                btnLayout.addView(submitBtn)
                newMainLineLayout.addView(btnLayout)
                newCard.addView(newMainLineLayout)

                container.addView(newCard, container.indexOfChild(card) + 1)
            }
        }

        textLinearLayout.addView(label)
        textLinearLayout.addView(desc)
        mainLinearLayout.addView(textLinearLayout)
        mainLinearLayout.addView(doneCheck)
        card.addView(mainLinearLayout)
        container.addView(card)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GET_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                val uri = data?.data
                if (uri != null){
                    val file = File(uri.path)
                    fileName = file.name.split(".")[0]


                    val stringBuilder = StringBuilder()
                    val inputStream = contentResolver.openInputStream(uri).use {inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            var line : String? = reader.readLine()
                            while (line != null){
                                stringBuilder.append(line)
                                line = reader.readLine()
                            }
                        }
                    }

                    val jsonObject = JSONObject(stringBuilder.toString())
                    val done : JSONArray = jsonObject.getJSONArray("done")
                    val doing: JSONArray = jsonObject.getJSONArray("doing")

                    for (i in 0..<done.length()){
                        ticketArr.put(i.toString(), mutableMapOf(
                            "title" to (done.get(i) as JSONObject).get("title") as String,
                            "description" to (done.get(i) as JSONObject).get("description") as String,
                            "state" to "done"
                        ))
                    }

                    for (i in done.length()..<done.length() + doing.length()){
                        ticketArr.put(i.toString(), mutableMapOf(
                            "title" to (doing.get(i - done.length()) as JSONObject).get("title") as String,
                            "description" to (doing.get(i - done.length()) as JSONObject).get("description") as String,
                            "state" to "doing"
                        ))
                    }

                    setStateWorking()
                }
            }
            else{
                Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT)
            }
        }
        else if (requestCode == SAVE_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                val uri = data?.data
                if (uri != null){
                    val outputStream = OutputStreamWriter(contentResolver.openOutputStream(uri))
                    outputStream.write(resultStr)
                    outputStream.close()
                }
            }
        }
        else{
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT)
        }
    }
}


@Composable
fun Ticket(
    content: MutableMap<String, String>,
    id: String,
){
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Text(text = content["title"]!!)
    }
}

@Composable
fun EditTicket(){
    Dialog(
        onDismissRequest = { /*TODO*/ },
    ) {

    }
}