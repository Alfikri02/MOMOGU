package com.example.momogu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.airbnb.lottie.LottieDrawable
import com.example.momogu.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.HashMap

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.signinLinkBtn.setOnClickListener {
            finish()
        }

        binding.signupBtn.setOnClickListener {
            enableComponents(false)
            startLoadingView(true)
            createAccount()
        }

        binding.layoutSignUpRelative.setOnTouchListener { _, _ ->

            hideSoftKeyboard(this)
            binding.etFullname.clearFocus()
            binding.etWhatsapp.clearFocus()
            binding.etEmail.clearFocus()
            binding.etPassword.clearFocus()

            false
        }

        setSignUpButtonEnabled(false)
        startLoadingView(false)
        enableComponents(true)
        initUI()
    }

    private fun initUI() {
        binding.etFullname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.etFullname.text.toString() != "" &&
                    binding.etWhatsapp.text.toString() != "" &&
                    binding.etEmail.text.toString() != "" &&
                    binding.etPassword.text.toString() != ""
                ) {
                    setSignUpButtonEnabled(true)
                } else {
                    setSignUpButtonEnabled(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etFullname.text.toString() != "" &&
                    binding.etWhatsapp.text.toString() != "" &&
                    binding.etEmail.text.toString() != "" &&
                    binding.etPassword.text.toString() != ""
                ) {
                    setSignUpButtonEnabled(true)
                } else {
                    setSignUpButtonEnabled(false)
                }
            }
        })

        binding.etWhatsapp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.etFullname.text.toString() != "" &&
                    binding.etWhatsapp.text.toString() != "" &&
                    binding.etEmail.text.toString() != "" &&
                    binding.etPassword.text.toString() != ""
                ) {
                    setSignUpButtonEnabled(true)
                } else {
                    setSignUpButtonEnabled(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etFullname.text.toString() != "" &&
                    binding.etWhatsapp.text.toString() != "" &&
                    binding.etEmail.text.toString() != "" &&
                    binding.etPassword.text.toString() != ""
                ) {
                    setSignUpButtonEnabled(true)
                } else {
                    setSignUpButtonEnabled(false)
                }
            }
        })

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isEmailValid(binding.etEmail.text.toString())) {
                    binding.lblInvalidEmail.visibility = View.GONE
                } else {
                    binding.lblInvalidEmail.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etFullname.text.toString() != "" &&
                    binding.etWhatsapp.text.toString() != "" &&
                    binding.etEmail.text.toString() != "" &&
                    binding.etPassword.text.toString() != ""
                ) {
                    setSignUpButtonEnabled(true)
                } else {
                    setSignUpButtonEnabled(false)
                }
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.etFullname.text.toString() != "" &&
                    binding.etWhatsapp.text.toString() != "" &&
                    binding.etEmail.text.toString() != "" &&
                    binding.etPassword.text.toString() != ""
                ) {
                    setSignUpButtonEnabled(true)
                } else {
                    setSignUpButtonEnabled(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etFullname.text.toString() != "" &&
                    binding.etWhatsapp.text.toString() != "" &&
                    binding.etEmail.text.toString() != "" &&
                    binding.etPassword.text.toString() != ""
                ) {
                    setSignUpButtonEnabled(true)
                } else {
                    setSignUpButtonEnabled(false)
                }
            }
        })
    }

    private fun setSignUpButtonEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            binding.signupBtn.isEnabled = true
            binding.signupBtn.isClickable = true
            binding.signupBtn.setBackgroundResource(R.drawable.rounded_corner_ijo)
        } else {
            binding.signupBtn.isEnabled = false
            binding.signupBtn.isClickable = false
            binding.signupBtn.setBackgroundResource(R.drawable.rounded_corner_light_gray)
        }
    }

    private fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun startLoadingView(start: Boolean) {
        if (start) {
            binding.loadingRegister.visibility = View.VISIBLE
            binding.svRegister.visibility = View.GONE
            binding.animLoadingRegister.setAnimation("paperplane.json")
            binding.animLoadingRegister.playAnimation()
            binding.animLoadingRegister.repeatCount = LottieDrawable.INFINITE
        } else {
            binding.loadingRegister.visibility = View.GONE
            binding.svRegister.visibility = View.VISIBLE
            binding.animLoadingRegister.cancelAnimation()
        }
    }

    private fun enableComponents(isEnabled: Boolean) {
        if (isEnabled) {
            binding.signinLinkBtn.isEnabled = true
            binding.signupBtn.isEnabled = true
            binding.etFullname.isEnabled = true
            binding.etWhatsapp.isEnabled = true
            binding.etEmail.isEnabled = true
            binding.etPassword.isEnabled = true
        } else {
            binding.signinLinkBtn.isEnabled = false
            binding.signupBtn.isEnabled = false
            binding.etFullname.isEnabled = false
            binding.etWhatsapp.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.etPassword.isEnabled = false
        }
    }

    private fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager: InputMethodManager = activity.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            activity.currentFocus?.windowToken, 0
        )
    }

    private fun createAccount() {
        val fullName = binding.etFullname.text.toString()
        val whatsapp = binding.etWhatsapp.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        when {
            TextUtils.isEmpty(fullName) -> Toast.makeText(
                this,
                "Full Name is required.",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(whatsapp) -> Toast.makeText(
                this,
                "Phone Number is required.",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(email) -> Toast.makeText(
                this,
                "Email is required.",
                Toast.LENGTH_LONG
            ).show()
            TextUtils.isEmpty(password) -> Toast.makeText(
                this,
                "Password is required.",
                Toast.LENGTH_LONG
            ).show()

            else -> {

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveUserInfo(fullName, email)

                        } else {
                            val message = task.exception!!.toString()
                            Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            startLoadingView(false)
                            enableComponents(true)

                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, email: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()

        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName
        userMap["email"] = email
        userMap["phone"] = ""
        userMap["city"] = ""
        userMap["address"] = ""
        userMap["image"] = ""

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //progressDialog.dismiss()
                    startLoadingView(false)
                    enableComponents(true)
                    setSignUpButtonEnabled(true)

                    Toast.makeText(
                        this,
                        "Account has been created successfully.",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    startLoadingView(false)
                    enableComponents(true)
                    setSignUpButtonEnabled(true)
                }
            }
    }
}