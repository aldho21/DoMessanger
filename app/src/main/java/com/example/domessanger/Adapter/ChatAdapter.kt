package com.example.domessanger.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.example.domessanger.HomeActivity
import com.example.domessanger.Model.Chat
import com.example.domessanger.R
import com.example.domessanger.ViewImageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private val mContext: Context = mContext
    private val mChatList: List<Chat> = mChatList
    private val imageUrl: String = imageUrl

    var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return if (position == 1) {
            val view: View = LayoutInflater.from(mContext)
                .inflate(com.example.domessanger.R.layout.message_item_right, parent, false)
            ViewHolder(view)

        } else {
            val view: View = LayoutInflater.from(mContext)
                .inflate(com.example.domessanger.R.layout.message_item_left, parent, false)
            ViewHolder(view)

        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImg: CircleImageView? = null
        var showtextMessage: TextView? = null
        var leftImgView: ImageView? = null
        var tSeen: TextView? = null
        var rightImgView: ImageView? = null

        init {
            profileImg = itemView.findViewById(R.id.profileImg)
            showtextMessage = itemView.findViewById(R.id.showtextMessage)
            leftImgView = itemView.findViewById(R.id.leftImgView)
            tSeen = itemView.findViewById(R.id.tSeen)
            rightImgView = itemView.findViewById(R.id.rightImgView)

        }
    }




    override fun getItemCount(): Int {
        return mChatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = mChatList[position]
        Picasso.get().load(imageUrl).into(holder.profileImg)
        if(chat.getMessage().equals("Sending a image")&& !chat.getUrl().equals("")){
            //image rightside
            if(chat.getSender().equals(firebaseUser!!.uid)){
                holder.showtextMessage!!.visibility = View.GONE
                holder.rightImgView!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.rightImgView)
                holder.rightImgView!!.setOnClickListener(){
                    val options = arrayOf<CharSequence>(
                        "View full image",
                        "Delete image",
                        "Cancel"
                    )
                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Are you sure?")

                    builder.setItems(options,DialogInterface.OnClickListener{
                        dialog, which ->
                        if (which == 0){
                            val intent = Intent(mContext, ViewImageActivity::class.java)
                            intent.putExtra("url",chat.getUrl())
                            mContext.startActivity(intent)

                        }else if (which == 1){
                            //deleteMessage(position,holder)

                        }
                    })
                    builder.show()
                }
            }else if(!chat.getSender().equals(firebaseUser!!.uid)){
                holder.showtextMessage!!.visibility = View.GONE
                holder.leftImgView!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.leftImgView)
                holder.leftImgView!!.setOnClickListener(){
                    val options = arrayOf<CharSequence>(
                        "View full image",
                        "Cancel"
                    )
                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Are you sure?")

                    builder.setItems(options,DialogInterface.OnClickListener{
                            dialog, which ->
                        if (which == 0){
                            val intent = Intent(mContext, ViewImageActivity::class.java)
                            intent.putExtra("url",chat.getUrl())
                            mContext.startActivity(intent)

                        }
                    })
                    builder.show()
                }

            }

        }else{
            holder.showtextMessage!!.text = chat.getMessage()
            if (firebaseUser!!.uid == chat.getSender()){
                holder.showtextMessage!!.setOnClickListener(){
                    val options = arrayOf<CharSequence>(
                        "Delete message",
                        "Cancel"
                    )
                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Are you sure?")

                    builder.setItems(options,DialogInterface.OnClickListener{
                            dialog, which ->
                        if (which == 0){
                            deleteMessage(position,holder)

                        }
                    })
                    builder.show()
                }
            }
        }

        //text message
        if (position == mChatList.size-1){
            if(chat.isIsseen()){

                holder.tSeen!!.text = "Seen"
                if(chat.getMessage().equals("haloo")&&chat.getUrl().equals("")){
                    val lp: RelativeLayout.LayoutParams? = holder.tSeen!!.layoutParams as RelativeLayout.LayoutParams
                    lp!!.setMargins(0, 245,10,0)
                    holder.tSeen!!.layoutParams = lp

                }else{
                    holder.tSeen!!.text = "Sent"
                    if(chat.getMessage().equals("haloo")&&!chat.getUrl().equals("")){
                        val lp: RelativeLayout.LayoutParams? = holder.tSeen!!.layoutParams as RelativeLayout.LayoutParams
                        lp!!.setMargins(0, 245,10,0)
                        holder.tSeen!!.layoutParams = lp

                    }
                }
            }

        }else{
            holder.tSeen!!.visibility = View.GONE
        }

    }
    override fun getItemViewType(position: Int): Int {
        return if (mChatList[position].getSender().equals(firebaseUser!!.uid)) {
            1
        } else {
            0
        }

    }

    private fun deleteMessage(position: Int,holder: ChatAdapter.ViewHolder){
        val ref = FirebaseDatabase.getInstance().reference.child("Chat")
            .child(mChatList.get(position).getMessage()!!)
            .removeValue()
            .addOnCompleteListener{task ->
                if (task.isSuccessful){
                    Toast.makeText(holder.itemView.context,"Delete Message", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(holder.itemView.context,"Cant the message",Toast.LENGTH_SHORT).show()
                }
            }
    }


}