package com.example.playrecordaudio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.playrecordaudio.model.ModelAudio
import java.lang.ClassCastException

class AdapterFiles(var list: MutableList<ModelAudio>, val activity: FragmentHead, val showCheckBox: Boolean): RecyclerView.Adapter<AdapterFiles.MyViewHolder>(){

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.name_item)
        val date = itemView.findViewById<TextView>(R.id.date_item)
        val checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rec_sort, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (showCheckBox){
            holder.checkBox.visibility = View.VISIBLE
        }
        holder.name.text = list[position].name
        holder.date.text = list[position].date
        holder.itemView.setOnClickListener {
            try{
                activity.getModelFromSortDialog(list[position])
            }catch (c: ClassCastException){
                Toast.makeText(activity.context, "Error", Toast.LENGTH_LONG).show()
            }
        }
    }
}