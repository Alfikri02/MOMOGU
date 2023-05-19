package com.example.momogu

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
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
            enableComponents(true)
            startLoadingView(false)
            createAccount()
        }

        setSignUpButtonEnabled(false)
        startLoadingView(false)
        enableComponents(true)
        initUI()

        nameFocus()
        phoneFocus()
        mailFocus()
        passFocus()

    }

    private fun nameFocus() {
        binding.etFullname.setOnFocusChangeListener { _, focus ->
            if (!focus) {
                binding.tiFullname.helperText = validName()
            }
        }
    }

    private fun validName(): String? {
        val passText = binding.etFullname.text.toString()
        if (passText.length < 4) {
            return "Nama Lengkap harus berisi minimal 4 karakter"
        }
        return null
    }

    private fun phoneFocus() {
        binding.etWhatsapp.setOnFocusChangeListener { _, focus ->
            if (!focus) {
                binding.tiWhatsapp.helperText = validPhone()
            }
        }
    }

    private fun validPhone(): String? {
        val passText = binding.etWhatsapp.text.toString()
        if (passText.length < 8) {
            return "Nomor Handphone harus berisi minimal 8 karakter"
        }
        return null
    }

    private fun mailFocus() {
        binding.etEmail.setOnFocusChangeListener { _, focus ->
            if (!focus) {
                binding.tiEmail.helperText = validMail()
            }
        }
    }

    private fun validMail(): String? {
        val emailText = binding.etEmail.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            return "Format email tidak valid!"
        }
        return null
    }

    private fun passFocus() {
        binding.etPassword.setOnFocusChangeListener { _, focus ->
            if (!focus) {
                binding.tiPassword.helperText = validPass()
            }
        }
    }

    private fun validPass(): String? {
        val passText = binding.etPassword.text.toString()
        if (passText.length < 6) {
            return "Password harus berisi minimal 6 karakter"
        }
        return null
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

    private fun createAccount() {
        val fullName = binding.etFullname.text.toString()
        val phone = binding.etWhatsapp.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()


        when {

            binding.tiFullname.helperText != null ->
            binding.etFullname.error = "Nama lengkap masih terdapat peringatan!"

            binding.tiWhatsapp.helperText != null ->
            binding.etWhatsapp.error = "Nomor Handphone masih terdapat peringatan!"

            binding.tiEmail.helperText != null ->
            binding.etEmail.error = "Email masih terdapat peringatan!"

            binding.tiPassword.helperText != null ->
                Toast.makeText(this, "Kata sandi masih terdapat peringatan!", Toast.LENGTH_LONG)
                    .show()

            else -> {

                startLoadingView(true)
                enableComponents(false)
                setSignUpButtonEnabled(false)

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveUserInfo(fullName, email, phone)
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


    private fun saveUserInfo(fullName: String, email: String, phone: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()

        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName
        userMap["email"] = email
        userMap["phone"] = phone

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
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