package com.example.eduapp.screen

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eduapp.viewmodel.AppViewModel

/**
 * A simple screen for testing the database.
 * Reuses the application's shared ViewModel to ensure data consistency.
 */
@Composable
fun TestDBScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val users by viewModel.users.collectAsState(initial = emptyList())

    var name by remember { mutableStateOf("") }
    
    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row {
            Button(onClick = {
                if (name.isNotBlank()) {
                    viewModel.addUser(name)
                    name = ""
                }
            }) {
                Text("Add User")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                viewModel.clearUsers()
            }) {
                Text("Clear All")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Database Content:", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(users) { user ->
                Text(
                    text = "ID: ${user.id}, ${user.username}, Score: ${user.score}, Level: ${user.level}",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
