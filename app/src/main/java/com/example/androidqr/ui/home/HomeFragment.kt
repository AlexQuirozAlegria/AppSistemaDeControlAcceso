package com.example.androidqr.ui.home

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // Import for coroutines
import com.example.androidqr.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController
import com.example.androidqr.R
import com.example.androidqr.network.QrDataRequest
import com.example.androidqr.network.RetrofitClient
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.launch // Import for coroutines
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var qrBitmap: Bitmap? = null
    private lateinit var invitationTypeSpinner: Spinner
    private lateinit var dateEditText: EditText
    // private var displayedText: String = "" // May not be needed if API returns QR directly or text for it

    // Get the API service instance
    private val apiService by lazy {
        RetrofitClient.instance
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val generateQrButton: Button = binding.genQR
        val qrCodeImageView: ImageView = binding.imageView
        val shareButton: Button = binding.button2
        val logout: Button = binding.logout
        invitationTypeSpinner = binding.invitationTypeSpinner // Inicializar el Spinner
        dateEditText = binding.editTextDate

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.invitation_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        invitationTypeSpinner.adapter = adapter

        invitationTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem == "PorFecha") {
                    dateEditText.visibility = View.VISIBLE
                } else {
                    dateEditText.visibility = View.GONE
                    dateEditText.setText("") // Limpiar la fecha si se cambia la opción
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }

        // Listener para el EditText de la fecha: Abre el DatePickerDialog
        dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        generateQrButton.setOnClickListener {
            val text2 = binding.editTextText2.text.toString()
            val text3 = binding.editTextText3.text.toString()
            val invitationType = binding.invitationTypeSpinner.selectedItem.toString()

            var fechaValidez: Date? = null
            if (invitationType == "PorFecha") {
                val dateString = dateEditText.text.toString()
                if (dateString.isBlank()) {
                    Toast.makeText(requireContext(), "Por favor, seleccione una fecha para la invitación 'PorFecha'.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Convertir el String de la fecha a un objeto Date
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    fechaValidez = dateFormat.parse(dateString)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Formato de fecha inválido. Use YYYY-MM-DD.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            if (text2.isBlank() || text3.isBlank()) {
                Toast.makeText(requireContext(), "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show a loading indicator (optional, but good UX)

            qrCodeImageView.visibility = View.GONE

            // Launch a coroutine for the network call
            viewLifecycleOwner.lifecycleScope.launch {

                val sharedPref = requireContext().getSharedPreferences("mi_app_prefs", Context.MODE_PRIVATE)
                val jwtToken = sharedPref.getString("jwt_token", null)

                if (jwtToken == null) {
                    Toast.makeText(requireContext(), "No hay sesión iniciada. Por favor, inicie sesión.", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_nh_to_login)
                    return@launch
                }

                // Prepara el token con el prefijo "Bearer "
                val authHeader = "Bearer $jwtToken"

                try {
                    val requestData = QrDataRequest(
                        nombre = text2,
                        apellidos = text3,
                        tipoInvitacion = invitationType,
                        fechaValidez = fechaValidez
                    )

                    val response = apiService.generateQrCode(authHeader, requestData)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null) {
                            // Option A: API returned text to be encoded
                            qrBitmap = generateQrCode(apiResponse.qrCode) // Use your existing function
                            qrBitmap?.let {
                                qrCodeImageView.setImageBitmap(it)
                                qrCodeImageView.visibility = View.VISIBLE
                            } ?: run {
                                Toast.makeText(requireContext(), "Error al generar QR localmente.", Toast.LENGTH_SHORT).show()
                                qrCodeImageView.setImageBitmap(null)
                                qrCodeImageView.visibility = View.GONE
                            }

                        } else {
                            // Handle empty or malformed successful response
                            val errorMsg = apiResponse?.message ?: "Respuesta de API vacía o incorrecta."
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                            qrCodeImageView.setImageBitmap(null)
                            qrCodeImageView.visibility = View.GONE
                        }
                    } else {
                        // Handle API error (e.g., 4xx or 5xx errors)
                        val errorBody = response.errorBody()?.string() ?: "Error desconocido de API."
                        Log.e("HomeFragment", "API Error: ${response.code()} - $errorBody")
                        Toast.makeText(requireContext(), "Error de API: ${response.code()}", Toast.LENGTH_LONG).show()
                        qrCodeImageView.setImageBitmap(null)
                        qrCodeImageView.visibility = View.GONE
                    }

                } catch (e: Exception) {
                    // Handle network errors (e.g., no internet) or other exceptions

                    Log.e("HomeFragment", "Network/Request Exception: ${e.message}", e)
                    Toast.makeText(requireContext(), "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                    qrCodeImageView.setImageBitmap(null)
                    qrCodeImageView.visibility = View.GONE
                }
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
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "YourAppNameQR")
                    }
                    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                } else {
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "YourAppNameQR"
                    val imageDirFile = File(imagesDir)
                    if (!imageDirFile.exists()) {
                        imageDirFile.mkdirs()
                    }
                    val image = File(imagesDir, imageFileName)
                    fos = FileOutputStream(image)
                }

                fos?.use {
                    val success = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
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

        logout.setOnClickListener {
            findNavController().navigate(R.id.action_nh_to_login)
        }
        return root
    }

    private fun getTomorrowDateString(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Add one day
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // API standard format
        return dateFormat.parse(dateFormat.format(calendar.time)) ?: Date()
    }


    // Función para mostrar el DatePickerDialog
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Formatear la fecha seleccionada a YYYY-MM-DD
                val formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                dateEditText.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
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
            Log.e("HomeFragment", "Error generating QR code bitmap", e)
        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
