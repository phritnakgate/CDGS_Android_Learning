package com.example.myfirstandroidapp

import android.Manifest
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class GoogleCalendarActivity : AppCompatActivity() {

    //Google Calendar
    private var mCredential: GoogleAccountCredential? = null
    private var mService: Calendar? = null
    private var accountName: String? = null

    //UI
    private lateinit var editTextTaskName: AppCompatEditText
    private lateinit var editTextTaskDesc: AppCompatEditText
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
        btnSaveCalendar = findViewById(R.id.compatbtn_save)
    }

    private fun setupEvents(){
        btnSaveCalendar.setOnClickListener {
            createCalendarEvent()
        }
    }

    private fun createCalendarEvent() {
        Thread {
            try {
                val event = Event()
                    .setSummary(editTextTaskName.text.toString())
                    .setDescription(editTextTaskDesc.text.toString())

                val startDateTime = DateTime("2025-07-08T09:00:00")
                val start = EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("Asia/Bangkok")
                event.start = start

                val endDateTime = DateTime("2025-07-08T10:00:00")
                val end = EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("Asia/Bangkok")
                event.end = end

                val createdEvent = mService?.events()?.insert("primary", event)?.execute()
                Log.d("GoogleCalendarActivity", "Event created: ${createdEvent?.htmlLink}")

            } catch (e: UserRecoverableAuthIOException){
                startActivityForResult(e.intent, 1001)
            }

            catch (e: Exception) {
                Log.e("GoogleCalendarActivity", "Error creating event", e)
            }
        }.start()
    }

}