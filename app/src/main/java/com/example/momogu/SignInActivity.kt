package com.example.momogu

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieDrawable
import com.example.momogu.databinding.ActivitySignInBinding
import com.example.momogu.utils.Helper
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.signupLinkBtn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            binding.etEmail.text = null
            binding.tiEmail.helperText = null
            binding.etPassword.text = null
            binding.tiPassword.helperText = null
        }

        binding.loginBtn.setOnClickListener {
            setLoginButtonEnabled(false)
            enableComponents(true)
            startLoadingView(false)
            loginUser()
        }

        setLoginButtonEnabled(false)
        startLoadingView(false)
        enableComponents(true)
        initUI()
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

    private fun initUI() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                helperLogin()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                helperLogin()
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                helperRegister()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                helperRegister()
            }
        })

    }

    private fun helperLogin() {
        binding.tiEmail.helperText = validMail()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        setLoginButtonEnabled(email.isNotEmpty() && password.isNotEmpty())
    }

    private fun helperRegister() {
        binding.tiPassword.helperText = validPass()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        setLoginButtonEnabled(email.isNotEmpty() && password.isNotEmpty())
    }

    private fun validMail(): String? {
        val emailText = binding.etEmail.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            return "Format email tidak valid!"
        }
        return null
    }

    private fun validPass(): String? {
        val passText = binding.etPassword.text.toString()
        if (passText.length < 6) {
            return "Password harus berisi minimal 6 karakter"
        }
        return null
    }

    private fun setLoginButtonEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            binding.loginBtn.isEnabled = true
            binding.loginBtn.isClickable = true
            binding.loginBtn.setBackgroundResource(R.drawable.rounded_corner_ijo)
        } else {
            binding.loginBtn.isEnabled = false
            binding.loginBtn.isClickable = false
            binding.loginBtn.setBackgroundResource(R.drawable.rounded_corner_light_gray)
        }
    }

    private fun startLoadingView(start: Boolean) {
        if (start) {
            binding.loadingLogin.visibility = View.VISIBLE
            binding.svLogin.visibility = View.GONE
            binding.animLoadingLogin.setAnimation("paperplane.json")
            binding.animLoadingLogin.playAnimation()
            binding.animLoadingLogin.repeatCount = LottieDrawable.INFINITE
        } else {
            binding.loadingLogin.visibility = View.GONE
            binding.svLogin.visibility = View.VISIBLE
            binding.animLoadingLogin.cancelAnimation()
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
            binding.tiEmail.helperText != null ->
                binding.etEmail.requestFocus()

            binding.tiPassword.helperText != null ->
                binding.etPassword.requestFocus()

            else -> {
                startLoadingView(true)
                enableComponents(false)
                setLoginButtonEnabled(false)

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        Helper.showDialogInfo(
                            this,
                            "Terjadi kesalahan saat masuk.\n" +
                                    "Email atau kata sandi salah!\n" +
                                    "Silakan coba lagi.",
                            Gravity.CENTER
                        )
                        binding.etEmail.requestFocus()
                        startLoadingView(false)
                        enableComponents(true)
                        setLoginButtonEnabled(false)
                    }
                }
            }
        }
    }
}