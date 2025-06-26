package com.example.biofaqih

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Bundle
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ==== Root Layout dengan Background Hijau Bergambar ====
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getDrawable(R.drawable.background) // background.png
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(32, 64, 32, 32)
        }

        // ==== Logo Yatsimadani dibungkus lingkaran putih ====
        val logoContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(500, 500).apply {
                gravity = Gravity.CENTER
                setMargins(0, 0, 0, 48)
            }
            background = ShapeDrawable(OvalShape()).apply {
                paint.color = Color.WHITE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = 8f
            }
            setPadding(24, 24, 24, 24)
        }

        val logo = ImageView(this).apply {
            setImageResource(R.drawable.yatsimadani)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        logoContainer.addView(logo)
        rootLayout.addView(logoContainer)

        // ==== Fungsi Membuat Card Button ====
        fun createDashboardCard(nama: String, onClick: () -> Unit): MaterialCardView {
            val card = MaterialCardView(this).apply {
                radius = 32f
                cardElevation = 16f
                setCardBackgroundColor(Color.parseColor("#4CAF50")) // hijau
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 24, 0, 0)
                }

                val text = TextView(this@MainActivity).apply {
                    text = nama
                    textSize = 20f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    setPadding(48, 48, 48, 48)
                }

                addView(text)
            }

            addAnimation(card, onClick)
            return card
        }

        // ==== Tambahkan Tombol ke Root Layout ====
        rootLayout.addView(createDashboardCard("Faqih Baidowi") {
            val intent = Intent(this, DashboardFaqihActivity::class.java)
            startActivity(intent)
        })

        rootLayout.addView(createDashboardCard("Sabrina Umi Hayati") {
            val intent = Intent(this, DashboardUmiActivity::class.java)
            startActivity(intent)
        })

        setContentView(rootLayout)
    }

    // ==== Fungsi Reusable untuk Animasi Klik + Action ====
    private fun addAnimation(view: View, onClick: () -> Unit) {
        view.setOnClickListener {
            val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.05f)
            val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.05f)
            val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.05f, 1f)
            val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.05f, 1f)

            scaleUpX.duration = 150
            scaleUpY.duration = 150
            scaleDownX.duration = 150
            scaleDownY.duration = 150

            scaleUpX.interpolator = AccelerateDecelerateInterpolator()
            scaleUpY.interpolator = AccelerateDecelerateInterpolator()
            scaleDownX.interpolator = AccelerateDecelerateInterpolator()
            scaleDownY.interpolator = AccelerateDecelerateInterpolator()

            scaleUpX.start()
            scaleUpY.start()

            scaleUpX.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    scaleDownX.start()
                    scaleDownY.start()
                    onClick() // Jalankan aksi setelah animasi
                }
            })
        }
    }
}
