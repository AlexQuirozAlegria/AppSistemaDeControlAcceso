package com.example.androidqr.ui.home

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView // Import ImageView
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.fragment.app.Fragment
import com.example.androidqr.databinding.FragmentHomeBinding

// ZXing imports (ensure you have the dependency in build.gradle)
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // This will hold the generated QR code Bitmap
    private var qrBitmap: Bitmap? = null
    // This will hold the text encoded in the QR code
    private var displayedText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ViewModel setup (if you are using it for other purposes)
        // val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root



        val generateQrButton: Button = binding.genQR //
        val qrCodeImageView: ImageView = binding.imageView //
        val shareButton: Button = binding.button2 //
        generateQrButton.setOnClickListener {
            displayedText = binding.editTextText3.text.toString() + "/" +
                    binding.editTextText2.text.toString() + " / " +
                    binding.invitationTypeSpinner.selectedItem.toString();
            qrBitmap = generateQrCode(displayedText) // Generate the QR code bitmap

            // Update the UI
            qrBitmap?.let {
                qrCodeImageView.setImageBitmap(it)

                qrCodeImageView.visibility = View.VISIBLE

            } ?: run {
                // Handle case where QR code generation failed
                qrCodeImageView.setImageBitmap(null) // Clear previous QR
                qrCodeImageView.visibility = View.GONE
            }
        }
        fun saveQrCodeToGallery(bitmap: Bitmap) {
            val imageFileName = "QR_${System.currentTimeMillis()}.jpg"
            var fos: OutputStream? = null

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = requireActivity().contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "YourAppNameQR") // Saves in Pictures/YourAppNameQR
                    }
                    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                } else {
                    // For older versions, this might require WRITE_EXTERNAL_STORAGE permission
                    // which you should request at runtime if you haven't.
                    // However, saving to app-specific directory is often better.
                    // For simplicity, this example targets the public Pictures directory.
                    // Be mindful of storage permissions on API < 29.
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "YourAppNameQR"
                    val imageDirFile = File(imagesDir)
                    if (!imageDirFile.exists()) {
                        imageDirFile.mkdirs()
                    }
                    val image = File(imagesDir, imageFileName)
                    fos = FileOutputStream(image)
                }

                fos?.use { // 'use' will auto-close the stream
                    val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) // 90 is quality
                    if (success) {
                        Toast.makeText(requireContext(), "Código QR guardado en Galería.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error al guardar imagen.", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(requireContext(), "Error al crear archivo de imagen.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al guardar imagen: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        shareButton.setOnClickListener {
            if (qrBitmap != null) {
                saveQrCodeToGallery(qrBitmap!!)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Primero genere un código QR.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        return root
    }

    /**
     * Generates a QR Code Bitmap from the given text.
     * @param text The text to encode in the QR Code.
     * @return A Bitmap object of the QR Code, or null if generation failed.
     */
    private fun generateQrCode(text: String): Bitmap? {
        val width = 512
        val height = 512
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix: BitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace() // Log the error
        }
        return null // Return null if there was an exception
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Important to avoid memory leaks
    }
}