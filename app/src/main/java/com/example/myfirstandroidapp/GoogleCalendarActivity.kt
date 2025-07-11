package com.example.myfirstandroidapp

import android.Manifest
import android.accounts.Account
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class GoogleCalendarActivity : AppCompatActivity() {

    //Google Auth
    private lateinit var credentialManager: CredentialManager

    //Firebase Auth
    private lateinit var auth: FirebaseAuth
    private var currentUser : FirebaseUser? = null

    //Google Calendar
    private var mCredential: GoogleAccountCredential? = null
    private var mService: Calendar? = null

    private val requestCalendarPermissionForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == RESULT_OK) {
            // Re-attempt to create the event after permission granted
            createCalendarEvent()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    //UI
    private lateinit var editTextTaskName: AppCompatEditText
    private lateinit var editTextTaskDesc: AppCompatEditText
    private lateinit var editTextTaskDate: AppCompatEditText
    private lateinit var editTextTaskTime: AppCompatEditText
    private lateinit var btnSaveCalendar: AppCompatButton
    private lateinit var btnSignOut: AppCompatButton

    private var startDate: DateTime? = null
    private var endDate: DateTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.google_calendar_form)

        findView()
        setupData()
        acquireGooglePlayServices()
        setupEvents()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.google_calendar_form)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("GoogleCalendarActivity", "GET_ACCOUNTS permission granted")
                initCalendarServices()
            } else {
                Toast.makeText(this, "Permission required to access calendar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun findView(){
        editTextTaskName = findViewById(R.id.edit_text_task_name)
        editTextTaskDesc = findViewById(R.id.edit_text_task_description)
        editTextTaskDate = findViewById(R.id.edit_text_task_date)
        editTextTaskTime = findViewById(R.id.edit_text_task_time)
        btnSaveCalendar = findViewById(R.id.compatbtn_save)
        btnSignOut = findViewById(R.id.compatbtn_logout)
    }

    private fun setupData(){
        // Initialize Firebase Auth and Credential Manager
        auth = Firebase.auth
        credentialManager = CredentialManager.create(this@GoogleCalendarActivity)
        currentUser = auth.currentUser

        initCalendarServices()

        if(currentUser == null){
            btnSignOut.visibility = View.GONE
        }else{
            btnSignOut.visibility = View.VISIBLE
        }
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
            // TODO: Implement time picker
        }

        btnSaveCalendar.setOnClickListener {
            // Check if user is signed in before creating event
            if (currentUser == null) {
                Toast.makeText(this@GoogleCalendarActivity, "Please sign in first", Toast.LENGTH_SHORT).show()
                launchCredentialManager()
            } else {
                createCalendarEvent()
            }
        }
        btnSignOut.setOnClickListener {
            signOut()
        }
    }

    private fun launchCredentialManager() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .setRequestVerifiedPhoneNumber(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@GoogleCalendarActivity,
                    request = request
                )
                handleSignIn(result.credential)
            } catch (e: GetCredentialException) {
                Log.e("GoogleCalendarActivity", "Couldn't retrieve user's credentials: ${e.localizedMessage}")
                Toast.makeText(this@GoogleCalendarActivity, "Sign in failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignIn(credential: Credential) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val email = googleIdTokenCredential.idToken.let {
                val payload = com.google.api.client.json.webtoken.JsonWebSignature.parse(
                    JacksonFactory.getDefaultInstance(), it
                ).payload
                payload["email"] as? String
            }
            Log.w("GoogleCalendarActivity", "Signed In with $email")

            initCalendarServices()
            btnSignOut.visibility = View.VISIBLE

            firebaseAuthWithGoogle(googleIdTokenCredential)
        } else {
            Log.w("GoogleCalendarActivity", "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(googleIdTokenCredential: GoogleIdTokenCredential) {
        val credential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    currentUser = auth.currentUser
                    Log.d("GoogleCalendarActivity", "signInWithCredential:$currentUser")
                    Log.d("GoogleCalendarActivity", "User: ${currentUser?.displayName}")
                    Log.d("GoogleCalendarActivity", "Email: ${currentUser?.email}")
                    Log.d("GoogleCalendarActivity", "UID: ${currentUser?.uid}")
                    Log.d("GoogleCalendarActivity", "Photo: ${currentUser?.photoUrl}")

                    // Initialize calendar services after successful sign in
                    initCalendarServices()
                    btnSignOut.visibility = View.VISIBLE
                    Toast.makeText(this@GoogleCalendarActivity, "Sign in successful", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("GoogleCalendarActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this@GoogleCalendarActivity, "Sign in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun initCalendarServices() {
        if (currentUser?.email.isNullOrEmpty()) {
            Log.e("GoogleCalendarActivity", "Current user email is null or empty")
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.GET_ACCOUNTS),
                1002
            )
            return
        }

        mCredential = GoogleAccountCredential.usingOAuth2(
            this@GoogleCalendarActivity,
            arrayListOf(CalendarScopes.CALENDAR)
        ).setBackOff(ExponentialBackOff()).apply {
            selectedAccount = currentUser?.email?.let { Account(it,"com.google") }
        }


        Log.d("GoogleCalendarActivity", "Calendar credential account: ${mCredential?.selectedAccountName}")

        // Build the calendar service
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        mService = Calendar.Builder(transport, jsonFactory, mCredential)
            .setApplicationName("MyFirstAndroidApp")
            .build()

    }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this@GoogleCalendarActivity)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            Log.e("GoogleCalendarActivity","No Google Play Services!")
        } else {
            Log.i("GoogleCalendarActivity","Has Google Play Services!")
        }
    }

    private fun createCalendarEvent() {
        if (editTextTaskName.text.isNullOrEmpty() || editTextTaskDesc.text.isNullOrEmpty()) {
            Toast.makeText(this@GoogleCalendarActivity, "Please input value!!", Toast.LENGTH_SHORT).show()
            return
        }

        if (startDate == null || endDate == null) {
            Toast.makeText(this@GoogleCalendarActivity, "Please select a date!", Toast.LENGTH_SHORT).show()
            return
        }

        if (mService == null) {
            Toast.makeText(this@GoogleCalendarActivity, "Calendar service not initialized!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val event = Event()
                    .setSummary(editTextTaskName.text.toString())
                    .setDescription(editTextTaskDesc.text.toString())

                val start = EventDateTime().setDate(startDate)
                event.start = start

                val end = EventDateTime().setDate(endDate)
                event.end = end

                val createdEvent = mService?.events()?.insert("primary", event)?.execute()
                Log.d("GoogleCalendarActivity", "Event: $event")

                withContext(Dispatchers.Main) {
                    Log.d("GoogleCalendarActivity", "Event created: ${createdEvent?.htmlLink}")
                    Toast.makeText(this@GoogleCalendarActivity, "Event created successfully!", Toast.LENGTH_SHORT).show()

                    // Clear the form
                    editTextTaskName.text?.clear()
                    editTextTaskDesc.text?.clear()
                    editTextTaskDate.text?.clear()
                    editTextTaskTime.text?.clear()

                    // Reset date variables
                    startDate = null
                    endDate = null
                }

            } catch (e: UserRecoverableAuthIOException) {
                withContext(Dispatchers.Main) {
                    requestCalendarPermissionForResult.launch(e.intent)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("GoogleCalendarActivity", "Error creating event", e)
                    Toast.makeText(this@GoogleCalendarActivity, "Error creating event: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun signOut() {
        // Firebase sign out
        auth.signOut()
        currentUser = null

        // Clear calendar services
        mCredential = null
        mService = null

        // Clear credential state
        lifecycleScope.launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                Log.d("GoogleCalendarActivity", "User signed out successfully")
            } catch (e: ClearCredentialException) {
                Log.e("GoogleCalendarActivity", "Couldn't clear user credentials: ${e.localizedMessage}")
            }
        }

        btnSignOut.visibility = View.GONE
    }
}