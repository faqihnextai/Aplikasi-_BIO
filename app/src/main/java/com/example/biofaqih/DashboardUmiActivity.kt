// DasnboardUmiActivity.kt

package com.example.biofaqih

import android.graphics.SurfaceTexture
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import android.animation.ObjectAnimator
import android.view.Window
import android.animation.Animator
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import android.widget.FrameLayout
import com.google.android.material.textview.MaterialTextView
import android.media.MediaPlayer
import android.view.TextureView
import android.view.Surface
import android.content.res.AssetFileDescriptor


class DashboardUmiActivity : AppCompatActivity() {
    private var contentFrameId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        // Ganti root ke FrameLayout agar bisa overlap
        val rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // TextureView untuk video background
        val textureView = TextureView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // MediaPlayer manual agar looping reliable
        val mediaPlayer = MediaPlayer()
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                mediaPlayer.setSurface(Surface(surface))
                val afd: AssetFileDescriptor = resources.openRawResourceFd(R.raw.background)
                mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                mediaPlayer.setVolume(0f, 0f)
                mediaPlayer.prepareAsync()

                mediaPlayer.setOnPreparedListener {
                    it.isLooping = true // Pindahkan ke sini
                    it.start()
                }
                mediaPlayer.setOnCompletionListener {
                    it.seekTo(0)
                    it.start()
                }
            }
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                mediaPlayer.release()
                return true
            }
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

        contentFrameId = View.generateViewId()
        val contentFrame = FrameLayout(this).apply {
            id = contentFrameId
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Versi Fragment UMI, bukan Faqih
        val bottomNav = BottomNavigationView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
            }
            labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
            setBackgroundColor(Color.WHITE)
            itemIconTintList = null
            inflateMenu(R.menu.bottom_nav_menu)
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_biodata -> switchToFragment(BiodataFragment1())
                    R.id.nav_game -> switchToFragment(GameFragment1())
                    R.id.nav_contact -> switchToFragment(ContactFragment1())
                }
                true
            }
        }

        // Susun urutan: video (paling bawah), fragment, bottom nav (paling atas)
        rootLayout.addView(textureView)
        rootLayout.addView(contentFrame)
        rootLayout.addView(bottomNav)

        setContentView(rootLayout)
        bottomNav.selectedItemId = R.id.nav_biodata
    }

    private fun switchToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            .replace(contentFrameId, fragment)
            .commit()
    }
}

// ======================= FRAGMENT GAME =======================
class GameFragment1 : Fragment() {

    private lateinit var gameView: GameView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        gameView = GameView(requireContext())
        return gameView
    }
}

// ======================= FRAGMENT KONTAK =======================
class ContactFragment1 : Fragment() {
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()

        // Root layout tanpa background hijau
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            // HAPUS: setBackgroundColor(Color.parseColor("#4CAF50"))
        }

        // List kontak: Label, URL/No, dan ikon
        val contactItems = listOf(
            Triple("Whatsapp", "+62 895-4118-81772", R.drawable.ic_whatsapp),
            Triple("GitHub", "https://github.com/faqihnextai/Aplikasi-_BIO", R.drawable.ic_github),
            Triple("TikTok", "https://www.tiktok.com/@faqihbaidowi", R.drawable.ic_tiktok)
        )

        contactItems.forEach { (label, url, iconRes) ->
            // Box kontak individual
            val box = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(600, 400).apply {
                    setMargins(0, 24, 0, 0)
                }
                gravity = Gravity.CENTER
                setPadding(32)
                background = ContextCompat.getDrawable(context, R.drawable.rounded_button_bg)
                elevation = 8f
                isClickable = true
                isFocusable = true

                // Tambahkan animasi klik + loading screen
                setOnClickListener {
                    it.animate().alpha(0.5f).setDuration(100).withEndAction {
                        it.alpha = 1f

                        // Tampilkan loading dialog
                        val dialog = Dialog(context).apply {
                            setContentView(ProgressBar(context).apply {
                                isIndeterminate = true
                                indeterminateTintList = ContextCompat.getColorStateList(context, android.R.color.holo_blue_light)
                            })
                            window?.setBackgroundDrawableResource(android.R.color.white)
                            setCancelable(false)
                            show()
                        }

                        // Setelah delay 1 detik, buka link dan tutup loading
                        it.postDelayed({
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = if (label == "Whatsapp") {
                                    Uri.parse("https://wa.me/$url")
                                } else {
                                    Uri.parse(url)
                                }
                            }
                            context.startActivity(intent)
                            dialog.dismiss()
                        }, 1000)
                    }.start()
                }
            }

            // Ikon di atas teks
            val icon = ImageView(context).apply {
                setImageResource(iconRes)
                layoutParams = LinearLayout.LayoutParams(128, 128).apply {
                    bottomMargin = 16
                    gravity = Gravity.CENTER_HORIZONTAL
                }
            }

            // Teks label kontak
            val text = TextView(context).apply {
                this.text = label
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            }

            box.addView(icon)
            box.addView(text)

            rootLayout.addView(box)
        }

        return rootLayout
    }
}

// ======================= FRAGMENT BIODATA =======================
class BiodataFragment1 : Fragment() {
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()

        val rootLayout = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Gambar sebagai background
        val imageView = ImageView(context).apply {
            setImageResource(R.drawable.umi_half) // Pastikan ini ada di drawable
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        // Panel tombol menimpa gambar
        val panelLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            tag = "panel_tag"
            layoutParams = FrameLayout.LayoutParams(
                700, // Lebar panel
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            setPadding(32)
            elevation = 12f
        }

        val menuItems = listOf(
            "Nama" to "Faqih Baidowi",
            "Tempat Lahir" to "Tangerang",
            "Hobi" to "Olahraga, Lari, Mobile Legends, Bulu Tangkis",
            "Riwayat Sekolah" to "SD Daarul Huda Tangerang\nSMP & SMA Daarur Rahman Depok"
        )

        menuItems.forEach { (label, value) ->
            val btn = TextView(context).apply {
                text = label
                textSize = 20f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(24, 24, 24, 24)
                background = ContextCompat.getDrawable(context, R.drawable.rounded_button_bg)
                elevation = 8f
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                        it.scaleX = 1f
                        it.scaleY = 1f
                        animateExit(imageView, panelLayout) {
                            showDetailOverlay(context, rootLayout, value)
                        }
                    }.start()
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
            panelLayout.addView(btn, params)
        }

        // Urutan penting: gambar dulu, tombol belakangan
        rootLayout.addView(imageView)
        rootLayout.addView(panelLayout)


        return rootLayout
    }

    private fun animateExit(
        imageView: ImageView,
        panelLayout: View,
        onEnd: () -> Unit
    ) {
        val imgAnim = ObjectAnimator.ofFloat(imageView, "translationX", 0f, -1000f).apply {
            duration = 500
        }
        val panelAnim = ObjectAnimator.ofFloat(panelLayout, "translationY", 0f, -1000f).apply {
            duration = 500
        }
        val fadeOut = ObjectAnimator.ofFloat(panelLayout, "alpha", 1f, 0f).apply {
            duration = 400
        }

        imgAnim.start()
        panelAnim.start()
        fadeOut.start()

        fadeOut.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                onEnd()
            }
        })
    }

    private fun showDetailOverlay(context: android.content.Context, root: FrameLayout, text: String) {
        val overlay = FrameLayout(context).apply {
            setBackgroundColor(Color.parseColor("#B3FFFFFF")) // Putih transparan
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            alpha = 0f
            setOnClickListener {
                ObjectAnimator.ofFloat(this, "alpha", 1f, 0f).apply {
                    duration = 400
                    addListener(object : android.animation.AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            root.removeView(this@apply)
                        }
                    })
                }.start()
            }
        }

        val detailText = TextView(context).apply {
            this.text = text
            textSize = 20f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.WHITE)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            ).apply {
                setMargins(64, 64, 64, 64)
            }
        }

        overlay.addView(detailText)
        root.addView(overlay)

        ObjectAnimator.ofFloat(overlay, "alpha", 0f, 1f).apply {
            duration = 400
        }.start()

        // Setelah overlay muncul, animasikan panel masuk kembali
        val panelLayout = root.findViewWithTag<LinearLayout>("panel_tag")
        if (panelLayout != null) {
            ObjectAnimator.ofFloat(panelLayout, "translationY", -1000f, 0f).apply {
                duration = 500
            }.start()
            ObjectAnimator.ofFloat(panelLayout, "alpha", 0f, 1f).apply {
                duration = 400
            }.start()
        }
    }
}

private fun Any.removeView(objectAnimator: ObjectAnimator?) {

}

// ======================= GAME VIEW =======================
class GameView1(context: Context) : View(context) {
    private val paint = Paint()
    private val board = Array(3) { Array(3) { "" } }
    private var currentPlayer = "X"
    private var gameOver = false

    init {
        paint.color = Color.BLACK
        paint.textSize = 200f
        paint.textAlign = Paint.Align.CENTER
        setBackgroundColor(Color.TRANSPARENT) // Ganti jadi transparan
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBoard(canvas)
        drawSymbols(canvas)
        if (gameOver) {
            drawGameOver(canvas)
        }
    }

    private fun drawBoard(canvas: Canvas) {
        val width = width / 3
        val height = height / 3
        for (i in 1 until 3) {
            canvas.drawLine(i * width.toFloat(), 0f, i * width.toFloat(), height * 3f, paint)
            canvas.drawLine(0f, i * height.toFloat(), width * 3f, i * height.toFloat(), paint)
        }
    }

    private fun drawSymbols(canvas: Canvas) {
        val width = width / 3
        val height = height / 3
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] != "") {
                    paint.color = if (board[i][j] == "X") Color.BLUE else Color.YELLOW
                    canvas.drawText(board[i][j], (j * width + width / 2).toFloat(), (i * height + height / 2 + 50).toFloat(), paint)
                }
            }
        }
    }

    private fun drawGameOver(canvas: Canvas) {
        val text = "$currentPlayer Menang!"
        paint.color = Color.WHITE
        paint.textSize = 100f
        canvas.drawText(text, width / 2f, height / 2f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !gameOver) {
            val x = event.x.toInt() / (width / 3)
            val y = event.y.toInt() / (height / 3)
            if (board[y][x] == "") {
                board[y][x] = currentPlayer
                if (checkWin()) {
                    gameOver = true
                } else {
                    currentPlayer = if (currentPlayer == "X") "O" else "X"
                }
                invalidate()
            }
        }
        return true
    }

    private fun checkWin(): Boolean {
        // Cek baris, kolom, dan diagonal
        for (i in 0..2) {
            if (board[i][0] == currentPlayer && board[i][1] == currentPlayer && board[i][2] == currentPlayer) return true
            if (board[0][i] == currentPlayer && board[1][i] == currentPlayer && board[2][i] == currentPlayer) return true
        }
        if (board[0][0] == currentPlayer && board[1][1] == currentPlayer && board[2][2] == currentPlayer) return true
        if (board[0][2] == currentPlayer && board[1][1] == currentPlayer && board[2][0] == currentPlayer) return true
        return false
        return false
    }
}

