package com.example.myfirstandroidapp

import android.Manifest
import android.accounts.AccountManager
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.Locale

class GoogleCalendarActivity : AppCompatActivity() {

    //Google Calendar
    private var mCredential: GoogleAccountCredential? = null
    private var mService: Calendar? = null
    private var accountName: String? = null

    //UI
    private lateinit var editTextTaskName: AppCompatEditText
    private lateinit var editTextTaskDesc: AppCompatEditText
    private lateinit var editTextTaskDate: AppCompatEditText
    private lateinit var editTextTaskTime: AppCompatEditText
    private lateinit var btnSaveCalendar: AppCompatButton

    companion object {
        private const val REQUEST_ACCOUNT_PICKER = 1000
        private const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    }


    private val startSelectGoogleAccountForResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val name = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (name != null) {
                    accountName = name
                    mCredential?.selectedAccountName = name
                    initCalendarBuild(mCredential)
                }
            }
        }

    private var startDate: DateTime? = null
    private var endDate: DateTime? = null

    //Checks if the device supports google play service
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this@GoogleCalendarActivity)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            Log.e("GoogleCalendarActivity","No Google Play Services!")
        }else{
            Log.i("GoogleCalendarActivity","Has Google Play Services!")
        }
    }

    //Init Google Calendar
    private fun initCredential(){
        acquireGooglePlayServices()
        mCredential = GoogleAccountCredential.usingOAuth2(
            this@GoogleCalendarActivity,
            arrayListOf(CalendarScopes.CALENDAR)
        )
            .setBackOff(ExponentialBackOff())
        Log.d("GoogleCalendarActivity",mCredential?.selectedAccountName.toString())
        initCalendarBuild(mCredential)
        setupEvents()
    }

    private fun initCalendarBuild(credential: GoogleAccountCredential?) {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        mService = Calendar.Builder(
            transport, jsonFactory, credential
        )
            .setApplicationName("MyFirstAndroidApp")
            .build()
    }

    //Check if there is a google account logged into the application
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this@GoogleCalendarActivity, Manifest.permission.GET_ACCOUNTS
            )
        ) {
            if (accountName != null) {
                mCredential!!.selectedAccountName = accountName
            } else {
                // Start a dialog from which the user can choose an account
                startSelectGoogleAccountForResult.launch(mCredential!!.newChooseAccountIntent())
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                this,
                "This app needs to access your Google account (via Contacts).",
                REQUEST_PERMISSION_GET_ACCOUNTS,
                Manifest.permission.GET_ACCOUNTS
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.google_calendar_form)

        findView()
        initCredential()
        chooseAccount()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.google_calendar_form))
        {
                v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun findView(){
        editTextTaskName = findViewById(R.id.edit_text_task_name)
        editTextTaskDesc = findViewById(R.id.edit_text_task_description)
        editTextTaskDate = findViewById(R.id.edit_text_task_date)
        editTextTaskTime = findViewById(R.id.edit_text_task_time)
        btnSaveCalendar = findViewById(R.id.compatbtn_save)
    }

    private fun setupEvents(){
        //Get Current Date&Time
        val calendar = java.util.Calendar.getInstance()

        editTextTaskDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this@GoogleCalendarActivity,
                { _, y, m, d ->
                    val selectedDate = java.util.Calendar.getInstance().apply {
                        set(y,m,d,0,0,0)
                        set(java.util.Calendar.MILLISECOND,0)
                    }
                    val formattedDate = d.toString().padStart(2,'0') + "/" + (m+1).toString().padStart(2,'0') + "/" + y.toString()
                    editTextTaskDate.setText(formattedDate)

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    startDate = DateTime(dateFormat.format(selectedDate.time))
                    val temp = selectedDate.clone() as java.util.Calendar
                    temp.add(java.util.Calendar.DAY_OF_MONTH,1)
                    endDate = DateTime(dateFormat.format(temp.time))
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
                )
            datePickerDialog.show()
        }
        editTextTaskTime.setOnClickListener {

        }
        btnSaveCalendar.setOnClickListener {
            createCalendarEvent()
        }
    }

    private fun createCalendarEvent() {
        if(editTextTaskName.text.isNullOrEmpty() || editTextTaskDesc.text.isNullOrEmpty()){
            Toast.makeText(this@GoogleCalendarActivity, "Please input value!!",Toast.LENGTH_SHORT).show()
        }
        Thread {
            try {
                val event = Event()
                    .setSummary(editTextTaskName.text.toString())
                    .setDescription(editTextTaskDesc.text.toString())


                val start = EventDateTime()
                    .setDate(startDate)
                event.start = start

                val end = EventDateTime()
                    .setDate(endDate)
                event.end = end

                val createdEvent = mService?.events()?.insert("primary", event)?.execute()
                Log.d("GoogleCalendarActivity", "Event created: ${createdEvent?.htmlLink}")
                editTextTaskName.text = null
                editTextTaskDesc.text = null
                editTextTaskDate.text = null

            } catch (e: UserRecoverableAuthIOException){
                startActivityForResult(e.intent, 1001)
            }

            catch (e: Exception) {
                Log.e("GoogleCalendarActivity", "Error creating event", e)
            }
        }.start()
    }

}