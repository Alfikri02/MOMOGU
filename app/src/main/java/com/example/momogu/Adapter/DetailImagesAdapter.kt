package com.example.momogu.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.momogu.DetailPostActivity
import com.example.momogu.Model.PostModel
import com.example.momogu.R
import com.squareup.picasso.Picasso

class DetailImagesAdapter(private val mContext: Context, mPost: List<PostModel>) :
    RecyclerView.Adapter<DetailImagesAdapter.ViewHolder?>() {
    private var mPost: List<PostModel>? = null

    init {
        this.mPost = mPost
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postImage: ImageView

        init {
            postImage = itemView.findViewById(R.id.post_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.images_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: PostModel = mPost!![position]
        Picasso.get().load(post.getPostimage()).into(holder.postImage)


        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("POST", Context.MODE_PRIVATE).edit()

            editor.putString("postid", post.getPostid())
            editor.apply()
            mContext.startActivity(Intent(mContext, DetailPostActivity::class.java))
        }

    }
}