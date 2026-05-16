package com.example.videoshare.features.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.videoshare.R
import com.example.videoshare.data.remote.dto.responses.UserResponse

class AdminUserAdapter(
    private var users: List<UserResponse>,
    private val onRoleChanged: (String, String) -> Unit,
    private val onDeleteClicked: (String) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    fun updateData(newUsers: List<UserResponse>) {
        this.users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val spRole: Spinner = itemView.findViewById(R.id.spRole)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDeleteUser)
        private val roles = arrayOf("user", "moderator", "admin")

        fun bind(user: UserResponse) {
            tvUsername.text = user.username

            val adapter =
                ArrayAdapter(itemView.context, R.layout.spinner_item, roles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spRole.adapter = adapter

            val defaultPosition = roles.indexOf(user.role)
            if (defaultPosition != -1) {
                spRole.setSelection(defaultPosition, false)
            }

            spRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    val selectedRole = roles[pos]
                    if (selectedRole != user.role) {
                        onRoleChanged(user._id, selectedRole)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            btnDelete.setOnClickListener {
                onDeleteClicked(user._id)
            }
        }
    }
}