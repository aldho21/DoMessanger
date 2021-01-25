package com.example.domessanger.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.domessanger.ChatActivity
import com.example.domessanger.MainActivity
import com.example.domessanger.Model.User
import com.example.domessanger.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

class UserAdapter(
    mContext: Context,
    mUser: List<User>,
    isChatCheck: Boolean
) : RecyclerView.Adapter<UserAdapter.ViewHolder?>() {
    private val mContext: Context
    private val mUser: List<User>
    private var isChatCheck: Boolean

    init {
        this.mUser = mUser
        this.mContext = mContext
        this.isChatCheck = isChatCheck
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext)
            .inflate(R.layout.user_search_item_layout, viewGroup, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val user: User? = mUser[i]

        holder.userNameTxt.text = user!!.getUserName()

        Picasso.get().load(user.getProfile()).placeholder(R.drawable.profile)
            .into(holder.profileImaView)
        holder.itemView.setOnClickListener() {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile",
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("What do you want to do?")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, position ->
                if (position == 0) {
                    val intent = Intent(mContext, ChatActivity::class.java)
                    intent.putExtra("visit_id", user.getUID())
                    mContext.startActivity(intent)

                }
                if (position == 1) {

                }
            })
            builder.show()
        }
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userNameTxt: TextView
        var profileImaView: CircleImageView
        var onlineImaView: CircleImageView
        var offlineeImaView: CircleImageView
        var lastMessageTxt: TextView

        init {

            userNameTxt = itemView.findViewById(R.id.username)
            profileImaView = itemView.findViewById(R.id.profile_image)
            onlineImaView = itemView.findViewById(R.id.image_online)
            offlineeImaView = itemView.findViewById(R.id.image_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)
        }
    }


}