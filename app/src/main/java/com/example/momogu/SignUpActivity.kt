package com.example.momogu

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.airbnb.lottie.LottieDrawable
import com.example.momogu.databinding.ActivitySignUpBinding
import com.example.momogu.utils.Helper.dialogInfoBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.collections.HashMap

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

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

        setRegisterButtonEnabled(false)
        startLoadingView(false)
        enableComponents(true)
        initUI()

    }

    private fun initUI() {
        binding.etFullname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                helperName()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                helperName()
            }
        })

        binding.etWhatsapp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                helperPhone()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                helperPhone()
            }
        })

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

    private fun helperName() {
        binding.tiFullname.helperText = validName()
        val name = binding.etFullname.text.toString()
        val phone = binding.etWhatsapp.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        setRegisterButtonEnabled(
            name.isNotEmpty() &&
                    phone.isNotEmpty() &&
                    email.isNotEmpty() &&
                    password.isNotEmpty()
        )
    }

    private fun helperPhone() {
        binding.tiWhatsapp.helperText = validPhone()
        val name = binding.etFullname.text.toString()
        val phone = binding.etWhatsapp.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        setRegisterButtonEnabled(
            name.isNotEmpty() &&
                    phone.isNotEmpty() &&
                    email.isNotEmpty() &&
                    password.isNotEmpty()
        )
    }

    private fun helperLogin() {
        binding.tiEmail.helperText = validMail()
        val name = binding.etFullname.text.toString()
        val phone = binding.etWhatsapp.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        setRegisterButtonEnabled(
            name.isNotEmpty() &&
                    phone.isNotEmpty() &&
                    email.isNotEmpty() &&
                    password.isNotEmpty()
        )
    }

    private fun helperRegister() {
        binding.tiPassword.helperText = validPass()
        val name = binding.etFullname.text.toString()
        val phone = binding.etWhatsapp.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        setRegisterButtonEnabled(
            name.isNotEmpty() &&
                    phone.isNotEmpty() &&
                    email.isNotEmpty() &&
                    password.isNotEmpty()
        )
    }

    private fun validName(): String? {
        val passText = binding.etFullname.text.toString()
        if (passText.length < 4) {
            return "Nama Lengkap harus berisi minimal 4 karakter"
        }
        return null
    }

    private fun validPhone(): String? {
        val passText = binding.etWhatsapp.text.toString()
        if (passText.length < 8) {
            return "Nomor Handphone harus berisi minimal 8 karakter"
        }
        return null
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

    private fun setRegisterButtonEnabled(isEnabled: Boolean) {
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
            binding.tiFullname.helperText != null -> binding.etFullname.requestFocus()
            binding.tiWhatsapp.helperText != null -> binding.etWhatsapp.requestFocus()
            binding.tiEmail.helperText != null -> binding.etEmail.requestFocus()
            binding.tiPassword.helperText != null -> binding.etPassword.requestFocus()
            else -> {
                startLoadingView(true)
                enableComponents(false)
                setRegisterButtonEnabled(false)

                val mAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveUserInfo(fullName, email, phone)
                            goToMainActivity()
                        } else {
                            showDialog(
                                this,
                                "Terjadi kesalahan saat mendaftar.\n" +
                                        "Email sudah pernah digunakan atau koneksi jaringan tidak stabil.\n" +
                                        "Silakan coba lagi.",
                            )
                            binding.etEmail.requestFocus()
                            startLoadingView(false)
                            enableComponents(true)
                            setRegisterButtonEnabled(false)
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, email: String, phone: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()

        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName
        userMap["email"] = email
        userMap["phone"] = phone

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Akun berhasil terdaftar!", Toast.LENGTH_SHORT).show()
                } else {
                    showDialog(
                        this,
                        "Terjadi kesalahan saat mendaftar.\n" +
                                "Email sudah pernah digunakan atau koneksi jaringan tidak stabil.\n" +
                                "Silakan coba lagi.",
                    )
                    binding.etEmail.requestFocus()
                    startLoadingView(false)
                    enableComponents(true)
                    setRegisterButtonEnabled(false)
                }
            }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun showDialog(context: Context, message: String, alignment: Int = Gravity.CENTER) {
        runOnUiThread {
            val dialog = dialogInfoBuilder(context, message, alignment)
            dialog.findViewById<Button>(R.id.button_ok).setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
    }


}