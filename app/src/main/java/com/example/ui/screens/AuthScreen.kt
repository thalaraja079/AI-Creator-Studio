package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.CreatorViewModel

enum class AuthMode {
    LOGIN,
    SIGN_UP
}

@Composable
fun AuthScreen(
    viewModel: CreatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    // Form fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Forgot Password dialog state
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var forgotSuccessMessage by remember { mutableStateOf(false) }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                forgotSuccessMessage = false
            },
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔑", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Password",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column {
                    if (forgotSuccessMessage) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF10B981).copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reset link sent! Please check your email inbox to update your password.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    } else {
                        Text(
                            text = "Enter your registered email address and we'll send you instructions to reset your password.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            placeholder = { Text("your.email@domain.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_password_email_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }
            },
            confirmButton = {
                if (!forgotSuccessMessage) {
                    Button(
                        onClick = {
                            if (forgotEmail.isBlank() || !forgotEmail.contains("@")) {
                                Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.requestPasswordReset(forgotEmail)
                                forgotSuccessMessage = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("send_reset_link_button")
                    ) {
                        Text("Send Reset Link", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            showForgotPasswordDialog = false
                            forgotSuccessMessage = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done")
                    }
                }
            },
            dismissButton = {
                if (!forgotSuccessMessage) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Main Auth Screen Content
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo Emblem
            Image(
                painter = painterResource(id = R.drawable.ic_creator_logo),
                contentDescription = "AI Creator Studio Logo",
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .testTag("auth_logo")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "AI Creator Studio",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )

            Text(
                text = if (authMode == AuthMode.LOGIN)
                    "Log in to unleash your creative AI workspace"
                else
                    "Create your account for daily AI creator tools",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Auth Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111827)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF8B5CF6).copy(alpha = 0.4f),
                                Color(0xFF3B82F6).copy(alpha = 0.4f)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("auth_card")
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Mode Switcher Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF1F293D),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (authMode == AuthMode.LOGIN) Color(0xFF8B5CF6) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    authMode = AuthMode.LOGIN
                                    viewModel.clearAuthErrorMessage()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Log In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (authMode == AuthMode.LOGIN) Color.White else Color(0xFF9CA3AF)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (authMode == AuthMode.SIGN_UP) Color(0xFF8B5CF6) else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    authMode = AuthMode.SIGN_UP
                                    viewModel.clearAuthErrorMessage()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign Up",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (authMode == AuthMode.SIGN_UP) Color.White else Color(0xFF9CA3AF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error Message
                    AnimatedVisibility(
                        visible = state.authErrorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .background(
                                    Color(0xFFEF4444).copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.authErrorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Sign Up: Full Name
                    if (authMode == AuthMode.SIGN_UP) {
                        Text(
                            text = "Full Name",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE5E7EB)
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = { Text("e.g. Alex Rivera") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_name_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF374151)
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Email Field
                    Text(
                        text = "Email Address",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5E7EB)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            viewModel.clearAuthErrorMessage()
                        },
                        placeholder = { Text("creator@studio.ai") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8B5CF6),
                            unfocusedBorderColor = Color(0xFF374151)
                        )
                    )

                    if (state.showSignUpSuccessDialog) {
                        AlertDialog(
                            onDismissRequest = { viewModel.finalizeSignUp() },
                            title = { Text("Success!") },
                            text = { Text("Account Created Successfully! Welcome to AI Creator Studio.") },
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.finalizeSignUp() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Let's Go!")
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5E7EB)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8B5CF6),
                            unfocusedBorderColor = Color(0xFF374151)
                        )
                    )

                    // Sign Up: Confirm Password
                    if (authMode == AuthMode.SIGN_UP) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Confirm Password",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE5E7EB)
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = { Text("••••••••") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        },
                            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signup_confirm_password_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8B5CF6),
                                unfocusedBorderColor = Color(0xFF374151)
                            )
                        )
                    }

                    // Login: Forgot Password link
                    if (authMode == AuthMode.LOGIN) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot Password?",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF8B5CF6),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .clickable {
                                        forgotEmail = email
                                        showForgotPasswordDialog = true
                                    }
                                    .padding(vertical = 4.dp)
                                    .testTag("forgot_password_button")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action Button (Login or Sign Up)
                    Button(
                        onClick = {
                            if (authMode == AuthMode.LOGIN) {
                                viewModel.login(email, password)
                            } else {
                                if (password != confirmPassword) {
                                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.signUp(fullName, email, password)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_primary_button")
                    ) {
                        Text(
                            text = if (authMode == AuthMode.LOGIN) "Log In" else "Create Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // OR Divider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF374151)
                        )
                        Text(
                            text = "  OR  ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF374151)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Continue with Google Button UI
                    OutlinedButton(
                        onClick = {
                            viewModel.loginWithGoogle(
                                name = if (fullName.isNotBlank()) fullName else "Google Creator",
                                email = if (email.isNotBlank()) email else "creator@google.com"
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_signin_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFEA4335),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bottom Switch Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (authMode == AuthMode.LOGIN)
                                "Don't have an account? "
                            else
                                "Already have an account? ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF9CA3AF)
                            )
                        )
                        Text(
                            text = if (authMode == AuthMode.LOGIN) "Sign Up" else "Log In",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF8B5CF6),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clickable {
                                    authMode = if (authMode == AuthMode.LOGIN) AuthMode.SIGN_UP else AuthMode.LOGIN
                                    viewModel.clearAuthErrorMessage()
                                }
                                .padding(4.dp)
                                .testTag("auth_switch_mode_button")
                        )
                    }
                }
            }
        }
    }
}
