package com.example.myfirstandroidapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy

class RecyclerAdapter(private var itemLists: MutableList<TravelData>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    //private var itemLists: MutableList<TravelData>? = null

    //fun setItemLists(List: MutableList<TravelData>){
    //    this.itemLists = List
   //     notifyDataSetChanged()
   // }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mylistview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = itemLists[position]
        holder.img
        holder.title?.text = item.title
        holder.desc?.text = item.desc
        holder.glide?.load(item.imgUrl)
            ?.skipMemoryCache(false)
            ?.diskCacheStrategy(DiskCacheStrategy.ALL)
            ?.placeholder(R.drawable.ic_launcher_foreground)
            ?.into(holder.img!!)
    }

    override fun getItemCount(): Int {
        return itemLists.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img: ImageView? = itemView.findViewById<ImageView>(R.id.itemTravel_img)
        var title: TextView? = itemView.findViewById<TextView>(R.id.itemTravel_title)
        var desc: TextView? = itemView.findViewById<TextView>(R.id.itemTravel_desc)

        var glide: RequestManager? = Glide.with(itemView.context)



    }
}