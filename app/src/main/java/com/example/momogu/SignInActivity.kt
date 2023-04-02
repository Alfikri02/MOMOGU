@file:Suppress("DEPRECATION")

package com.example.momogu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.momogu.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth


@Suppress("DEPRECATION")
class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        buttonOnClickListener()
        textFieldValidationChecking()

        setLoginButtonEnabled(false)
        startLoadingView(false)
        enableComponents(true)
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun buttonOnClickListener() {
        binding.signupLinkBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            binding.etEmail.text = null
            binding.etPassword.text = null
            binding.lblInvalidEmail.visibility = View.GONE
        }

        binding.loginBtn.setOnClickListener {
            enableComponents(false)
            setLoginButtonEnabled(false)
            startLoadingView(true)
            loginUser()
        }

        binding.layoutSignInRelative.setOnTouchListener { _, _ ->
            if (binding.etEmail.text.toString() != "" && !isEmailValid(binding.etEmail.text.toString())) {
                binding.lblInvalidEmail.visibility = View.VISIBLE
            } else {
                binding.lblInvalidEmail.visibility = View.GONE
            }

            hideSoftKeyboard(this)
            binding.etEmail.clearFocus()
            binding.etPassword.clearFocus()
            false
        }
    }

    private fun textFieldValidationChecking() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!isEmailValid(binding.etEmail.text.toString())) {
                    binding.lblInvalidEmail.visibility = View.VISIBLE
                } else {
                    binding.lblInvalidEmail.visibility = View.GONE
                    if (binding.etEmail.text.toString() != "" && binding.etPassword.text.toString() != "") {
                        setLoginButtonEnabled(true)
                    } else {
                        setLoginButtonEnabled(false)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etEmail.text.toString() != "" && binding.etPassword.text.toString() != "") {
                    setLoginButtonEnabled(true)
                } else {
                    setLoginButtonEnabled(false)
                }
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.etEmail.text.toString() != "" && binding.etPassword.text.toString() != "") {
                    setLoginButtonEnabled(true)
                } else {
                    setLoginButtonEnabled(false)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.etEmail.text.toString() != "" && binding.etPassword.text.toString() != "") {
                    setLoginButtonEnabled(true)
                } else {
                    setLoginButtonEnabled(false)
                }
            }
        })
    }

    fun isEmailValid(email: CharSequence): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setLoginButtonEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            binding.loginBtn.isEnabled = true
            binding.loginBtn.isClickable = true
            binding.loginBtn.background = resources.getDrawable(R.drawable.rounded_corner_ijo)
        } else {
            binding.loginBtn.isEnabled = false
            binding.loginBtn.isClickable = false
            binding.loginBtn.background = resources.getDrawable(R.drawable.rounded_corner_light_gray)
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

    private fun startLoadingView(start: Boolean) {
        if (start) {
            binding.layoutLoadingView.visibility = View.VISIBLE
            binding.animLoadingView.setAnimation("paperplane.json")
            binding.animLoadingView.playAnimation()
            binding.animLoadingView.loop(true)
        } else {
            binding.layoutLoadingView.visibility = View.GONE
            binding.animLoadingView.cancelAnimation()
        }
    }

    private fun enableComponents(isEnabled: Boolean) {
        if (isEnabled) {
            binding.signupLinkBtn.isEnabled = true
            binding.etEmail.isEnabled = true
            binding.etPassword.isEnabled = true

        } else {
            binding.signupLinkBtn.isEnabled = false
            binding.etEmail.isEnabled = false
            binding.etPassword.isEnabled = false
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        when {
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

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startLoadingView(false)
                        enableComponents(true)
                        setLoginButtonEnabled(true)

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
                        setLoginButtonEnabled(true)
                    }
                }
            }
        }
    }
}