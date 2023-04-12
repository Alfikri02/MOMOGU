package com.example.momogu

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.momogu.Model.PostModel
import com.example.momogu.Model.UserModel
import com.example.momogu.databinding.ActivityReceiptUserBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.DecimalFormat

class ReceiptUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiptUserBinding
    private var postId: String = ""
    private lateinit var profileId: String
    private lateinit var builder: AlertDialog.Builder

    @SuppressLint("DiscouragedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptUserBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        builder = AlertDialog.Builder(this)

        val preferences = this.getSharedPreferences("POST", Context.MODE_PRIVATE)
        if (preferences != null) {
            postId = preferences.getString("postid", "none")!!
        }

        retrievePosts()

        val pref = this.getSharedPreferences("PROFILE", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none").toString()
        }

        userInfo()

        binding.backCheckout.setOnClickListener {
            finish()
        }

        binding.menuReceipt.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.confirm_receipt -> {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Peringatan!")
                            .setMessage("Apakah anda ingin mengkonfirmasi pesanan ini?")
                            .setCancelable(true)
                            .setPositiveButton("Iya") { _, _ ->

                                setStatusConfirm()
                                finish()
                                Toast.makeText(this, "Pesanan telah dikonfirmasi!", Toast.LENGTH_SHORT).show()

                            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                                dialogInterface.cancel()
                            }.show()

                        true
                    }
                    R.id.proses_receipt -> {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Peringatan!")
                            .setMessage("Apakah anda ingin memproses pesanan ini?")
                            .setCancelable(true)
                            .setPositiveButton("Iya") { _, _ ->

                                setStatusProses()
                                finish()
                                Toast.makeText(this, "Pesanan telah diproses!", Toast.LENGTH_SHORT).show()

                            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                                dialogInterface.cancel()
                            }.show()

                        true
                    }
                    R.id.order_receipt -> {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Peringatan!")
                            .setMessage("Apakah anda ingin mengantar pesanan ini?")
                            .setCancelable(true)
                            .setPositiveButton("Iya") { _, _ ->

                                setStatusOrder()
                                finish()
                                Toast.makeText(this, "Pesanan sedang dalam pengantaran!", Toast.LENGTH_SHORT).show()

                            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                                dialogInterface.cancel()
                            }.show()

                        true
                    }
                    R.id.done_receipt -> {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Peringatan!")
                            .setMessage("Apakah anda ingin menyelesaikan pesanan ini?")
                            .setCancelable(true)
                            .setPositiveButton("Iya") { _, _ ->

                                setStatusDone()
                                finish()
                                Toast.makeText(this, "Pesanan telah diselesaikan!", Toast.LENGTH_SHORT).show()

                            }.setNegativeButton("Tidak") { dialogInterface, _ ->
                                dialogInterface.cancel()
                            }.show()

                        true
                    }
                    else -> false
                }
            }
            popupMenu.inflate(R.menu.receipt_menu)

            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popupMenu)
                mPopup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (e: Exception) {
                Log.e("Main", "Error showing menu icons.", e)
            } finally {
                popupMenu.show()
            }

        }
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue(PostModel::class.java)

                    Picasso.get().load(post!!.getPostimage()).placeholder(R.drawable.momogu)
                        .into(binding.imagePost)
                    binding.tvProduct.text = post.getProduct()
                    binding.tvPrice.text = "Rp. ${post.getPrice()}"
                    binding.tvWeight.text = "${post.getWeight()} KG"
                    binding.tvPriceItems.text = "Rp. ${post.getPrice()}"
                    binding.tvPriceShipping.text = "Rp. ${post.getShipping()}"

                    val price = post.getPrice()
                    val getPrice = price?.replace(".", "")?.replace(",", "")?.toInt()

                    val shipping = post.getShipping()
                    val getShipping = shipping?.replace(".", "")?.replace(",", "")?.toInt()

                    val decimalFormat = DecimalFormat("#,##0")
                    val hasil = getPrice!! + getShipping!!
                    val formatNumber = hasil.toString().replace(".", "").replace(",", "")

                    val formatted = decimalFormat.format(formatNumber.toDouble())

                    binding.tvTotal.text = "Rp. $formatted"


                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(UserModel::class.java)

                    binding.tvCity.text = user?.getCity()
                    binding.tvAddress.text = user?.getAddress()
                    binding.tvWhatsapp.text = user?.getWa()
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun setStatusConfirm() {
        val ref = FirebaseDatabase.getInstance().reference.child("Receipt")
        val receiptMap = HashMap<String, Any>()

        receiptMap["status"] = "Dikonfirmasi"

        ref.child(postId).updateChildren(receiptMap)
    }

    private fun setStatusProses() {
        val ref = FirebaseDatabase.getInstance().reference.child("Receipt")
        val receiptMap = HashMap<String, Any>()

        receiptMap["status"] = "Diproses"

        ref.child(postId).updateChildren(receiptMap)
    }

    private fun setStatusOrder() {
        val ref = FirebaseDatabase.getInstance().reference.child("Receipt")
        val receiptMap = HashMap<String, Any>()

        receiptMap["status"] = "Pengantaran"

        ref.child(postId).updateChildren(receiptMap)
    }

    private fun setStatusDone() {
        val ref = FirebaseDatabase.getInstance().reference.child("Receipt")
        val receiptMap = HashMap<String, Any>()

        receiptMap["status"] = "Selesai"

        ref.child(postId).updateChildren(receiptMap)
    }

}