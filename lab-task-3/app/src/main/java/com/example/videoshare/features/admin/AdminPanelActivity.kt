package com.example.videoshare.features.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videoshare.R
import com.example.videoshare.data.remote.dto.requests.UpdateRoleRequest
import com.example.videoshare.data.remote.network.RetrofitClient
import kotlinx.coroutines.launch

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var adapter: AdminUserAdapter
    private val adminApi = RetrofitClient.adminApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)

        rvUsers = findViewById(R.id.rvAdminUsers)
        rvUsers.layoutManager = LinearLayoutManager(this)

        adapter = AdminUserAdapter(
            users = emptyList(),
            onRoleChanged = { userId, newRole -> handleRoleChange(userId, newRole) },
            onDeleteClicked = { userId -> deleteUser(userId) }
        )
        rvUsers.adapter = adapter

        fetchUsers()
    }

    private fun fetchUsers() {
        lifecycleScope.launch {
            try {
                val response = adminApi.fetchUsers()
                if (response.isSuccessful && response.body() != null) {
                    adapter.updateData(response.body()!!)
                } else {
                    Toast.makeText(this@AdminPanelActivity, "Access denied or server error", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminPanelActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleRoleChange(userId: String, newRole: String) {
        lifecycleScope.launch {
            try {
                val response = adminApi.updateRole(userId, UpdateRoleRequest(newRole))
                if (response.isSuccessful) {
                    Toast.makeText(this@AdminPanelActivity, "Role updated", Toast.LENGTH_SHORT).show()
                    fetchUsers()
                } else {
                    Toast.makeText(this@AdminPanelActivity, "Failed to update role", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminPanelActivity, "Network Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteUser(userId: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this user?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response = adminApi.deleteUser(userId)
                        if (response.isSuccessful) {
                            Toast.makeText(this@AdminPanelActivity, "User deleted", Toast.LENGTH_SHORT).show()
                            fetchUsers()
                        } else {
                            Toast.makeText(this@AdminPanelActivity, "Failed to delete user", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@AdminPanelActivity, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}