package com.leopc.speakup.ui.auth

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.leopc.speakup.MainActivity
import com.leopc.speakup.R
import com.leopc.speakup.databinding.ActivityLoginBinding
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.leopc.speakup.utils.ValidationUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false
    private var isLoginTab = true
    
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("LoginActivity", "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupUI()
        setupListeners()
        animateEntrance()
        
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
             // create intent to main activity
             startActivity(Intent(this@LoginActivity, MainActivity::class.java))
             finish()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    // ... code truncated for brevity, jumping to startGoogleSignIn ...
    
    // Note: I will use a separate replacement for startGoogleSignIn to avoid replacing too much content if possible, 
    // but the instruction implies fixing the file. 
    // I need to be careful with line numbers.
    // Let's do imports and onNewIntent first with this chunk.

    private fun setupUI() {
        // Set initial tab state
        updateTabUI(isLogin = true)
        
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Tab switching
        binding.tabLogin.setOnClickListener {
            if (!isLoginTab) {
                isLoginTab = true
                updateTabUI(isLogin = true)
                animateTabSwitch()
            }
        }

        binding.tabSignUp.setOnClickListener {
            if (isLoginTab) {
                isLoginTab = false
                updateTabUI(isLogin = false)
                animateTabSwitch()
                Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
            }
        }

        // Password visibility toggle
        binding.btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Login button
        binding.btnLogin.setOnClickListener {
            validateAndLogin()
        }

        // Forgot password
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
        }

        // Google sign in
        binding.btnGoogleSignIn.setOnClickListener {
            startGoogleSignIn()
        }

        // Sign up link
        binding.tvSignUpLink.setOnClickListener {
            isLoginTab = false
            updateTabUI(isLogin = false)
            animateTabSwitch()
            Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTabUI(isLogin: Boolean) {
        if (isLogin) {
            // Login tab is selected
            binding.tabLogin.apply {
                setBackgroundResource(R.drawable.bg_tab_selected)
                setTextColor(ContextCompat.getColor(context, R.color.primary))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            binding.tabSignUp.apply {
                setBackgroundResource(android.R.color.transparent)
                setTextColor(ContextCompat.getColor(context, R.color.text_gray))
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
            
            // Update headline
            binding.tvWelcome.text = getString(R.string.welcome_back)
            binding.btnLogin.text = getString(R.string.login_now)
        } else {
            // Sign up tab is selected
            binding.tabSignUp.apply {
                setBackgroundResource(R.drawable.bg_tab_selected)
                setTextColor(ContextCompat.getColor(context, R.color.primary))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            binding.tabLogin.apply {
                setBackgroundResource(android.R.color.transparent)
                setTextColor(ContextCompat.getColor(context, R.color.text_gray))
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
            
            // Update headline
            binding.tvWelcome.text = "Create Account"
            binding.btnLogin.text = "Sign Up Now"
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        
        if (isPasswordVisible) {
            // Show password
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            // Hide password
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility)
        }
        
        // Move cursor to end of text
        binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
    }

    private fun validateAndLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Reset errors
        binding.etEmail.error = null
        binding.etPassword.error = null

        // Validate email
        when {
            email.isEmpty() -> {
                binding.etEmail.error = getString(R.string.error_empty_email)
                binding.etEmail.requestFocus()
                return
            }
            !ValidationUtils.isValidEmail(email) -> {
                binding.etEmail.error = getString(R.string.error_invalid_email)
                binding.etEmail.requestFocus()
                return
            }
        }

        // Validate password
        when {
            password.isEmpty() -> {
                binding.etPassword.error = getString(R.string.error_empty_password)
                binding.etPassword.requestFocus()
                return
            }
            !ValidationUtils.isValidPassword(password) -> {
                binding.etPassword.error = getString(R.string.error_short_password)
                binding.etPassword.requestFocus()
                return
            }
        }

        // All validations passed - proceed with login
        performLogin(email, password)
    }

    private fun performLogin(email: String, password: String) {
        // TODO: Implement actual authentication logic here
        // For now, just show success and navigate to MainActivity
        
        Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
        
        // Navigate to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun animateEntrance() {
        // Fade in animation for main content
        val views = listOf(
            binding.iconBackground,
            binding.iconBook,
            binding.tvWelcome,
            binding.tvSubtitle,
            binding.tabContainer
        )

        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 30f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((index * 50).toLong())
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        // Slide up animation for form fields
        val formViews = listOf(
            binding.emailContainer,
            binding.passwordContainer,
            binding.cbRememberMe,
            binding.btnLogin,
            binding.btnGoogleSignIn
        )

        formViews.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((300 + index * 60).toLong())
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun animateTabSwitch() {
        // Subtle bounce animation when switching tabs
        val scaleX = ObjectAnimator.ofFloat(
            if (isLoginTab) binding.tabLogin else binding.tabSignUp,
            "scaleX",
            0.95f,
            1.05f
        )
        val scaleY = ObjectAnimator.ofFloat(
            if (isLoginTab) binding.tabLogin else binding.tabSignUp,
            "scaleY",
            0.95f,
            1.05f
        )
        
        scaleX.repeatMode = android.animation.ValueAnimator.REVERSE
        scaleX.repeatCount = 1
        scaleY.repeatMode = android.animation.ValueAnimator.REVERSE
        scaleY.repeatCount = 1
        
        scaleX.duration = 200
        scaleY.duration = 200
        scaleX.start()
        scaleY.start()
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
}
