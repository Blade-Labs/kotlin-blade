package io.bladewallet.kotlin_sdk_demo.token

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.bladewallet.bladesdk.Blade
import io.bladewallet.bladesdk.models.KeyRecord
import io.bladewallet.bladesdk.models.KeyType
import io.bladewallet.bladesdk.models.NFTStorageConfig
import io.bladewallet.bladesdk.models.NFTStorageProvider
import io.bladewallet.kotlin_sdk_demo.Config
import io.bladewallet.kotlin_sdk_demo.databinding.FragmentTokenBinding
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

private const val REQUEST_CODE_IMAGE_PERMISSION = 101

class TokenFragment : Fragment() {

    private var _binding: FragmentTokenBinding? = null
    private var startOperation: Long = System.currentTimeMillis()
    private val binding get() = _binding
    private lateinit var base64Image: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTokenBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun toggleElements(enable: Boolean): Boolean {
            binding!!.tokenNameEditText.isEnabled = enable
            binding!!.tokenSymbolEditText.isEnabled = enable
            binding!!.buttonCreateToken.isEnabled = enable

            return enable
        }

        binding!!.tokenNameEditText.setText(Config.tokenName)
        binding!!.tokenSymbolEditText.setText(Config.tokenSymbol)

        @SuppressLint("SetTextI18n")
        fun output(text: String) {
            if (text == "") {
                startOperation = System.currentTimeMillis()
                binding?.textTitleOutput?.text = "Output:"
                binding?.progressBar?.visibility = View.VISIBLE
            } else {
                println(text)
                binding?.textTitleOutput?.text = "Output (${System.currentTimeMillis() - startOperation}ms):"
                binding?.progressBar?.visibility = View.GONE
            }
            binding?.outputTextView?.text = text
        }

        Blade.getInfo { infoData, bladeJSError ->
            lifecycleScope.launch {
                if (infoData != null) {
                    toggleElements(true)
                    output("$infoData")
                } else {
                    toggleElements(false)
                    output("$bladeJSError")
                }
            }
        }

        binding!!.buttonCreateToken.setOnClickListener {
            output("")

            val keys = listOf(
                KeyRecord(Config.privateKey2, KeyType.admin)
            )
            Blade.createToken(
                    tokenName = binding?.tokenNameEditText?.text.toString(),
                    tokenSymbol = binding?.tokenSymbolEditText?.text.toString(),
                    isNft = true,
                    keys,
                    decimals = 0,
                    initialSupply = 0,
                    maxSupply = 250
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}")
                    if (result != null) {
                        Config.tokenIdToMint = result.tokenId
                        binding?.tokenIdEditText?.isEnabled = true
                        binding?.tokenIdEditText?.setText(result.tokenId)
                        binding?.buttonPickImage?.isEnabled = true
                    }
                }
            }
        }

        binding!!.buttonPickImage.setOnClickListener {
            if (binding != null) {
                if (Build.VERSION.SDK_INT >= 33) {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.READ_MEDIA_IMAGES
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(),
                                Manifest.permission.READ_MEDIA_IMAGES
                            )
                        ) {
                            requestPermissions(
                                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                                REQUEST_CODE_IMAGE_PERMISSION
                            )
                        } else {
                            requestPermissions(
                                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                                REQUEST_CODE_IMAGE_PERMISSION
                            )
                        }
                    } else {
                        // Permission already granted, start image picker
                        val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        try {
                            i.putExtra("return-data", true)
                            startActivityForResult(
                                Intent.createChooser(i, "Select Picture"), 0
                            )
                        } catch (ex: ActivityNotFoundException) {
                            ex.printStackTrace()
                        }
                    }
                } else {
                    output("You need to have Android API version at least 33")
                }
            }
        }

        binding!!.buttonMintToken.setOnClickListener {
            output("")
            val metaData = mapOf<String, Any>(
                "name" to "NFTitle",
                "score" to 10,
                "power" to 4,
                "intelligence" to 6,
                "speed" to 10
            )

            Blade.nftMint(
                tokenAddress = binding!!.tokenIdEditText.text.toString(),
                file = base64Image,
                metadata = metaData,
                storageConfig = NFTStorageConfig(
                    provider = NFTStorageProvider.nftStorage,
                    apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkaWQ6ZXRocjoweDZFNzY0ZmM0ZkZFOEJhNjdCNjc1NDk1Q2NEREFiYjk0NTE4Njk0QjYiLCJpc3MiOiJuZnQtc3RvcmFnZSIsImlhdCI6MTcwNDQ2NDUxODQ2MiwibmFtZSI6IkJsYWRlU0RLLXRlc3RrZXkifQ.t1wCiEuiTvcYOwssdZgiYaug4aF8ZrvMBdkTASojWGU"
                ),
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}")
                    if (result != null) {
                        binding?.buttonAssociateToken?.isEnabled = true
                        binding?.buttonSendToken?.isEnabled = true
                    }
                }
            }
        }

        binding!!.buttonAssociateToken.setOnClickListener {
            output("")
            Blade.associateToken(
                tokenIdOrCampaign = binding?.tokenIdEditText?.text.toString(),
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}")
                }
            }
        }

        binding!!.buttonSendToken.setOnClickListener {
            output("")
            Blade.transferTokens(
                tokenAddress = binding?.tokenIdEditText?.text.toString(),
                receiverAddress = Config.privateKey2Account,
                amountOrSerial = "0.01",
                memo = "Send NFT from SDK",
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            val selectedImage: Uri? = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor? = selectedImage?.let { activity?.contentResolver?.query(it, filePathColumn, null, null, null) }

            cursor?.use {
                it.moveToFirst()
                val columnIndex: Int = it.getColumnIndex(filePathColumn[0])
                val picturePath: String = it.getString(columnIndex)

                val bitmap = BitmapFactory.decodeFile(picturePath)
                // Check if the bitmap is not null
                if (bitmap != null) {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

                    // Convert the compressed image to Base64
                    val byteArray = byteArrayOutputStream.toByteArray()
                    base64Image = "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)

                    // println(base64Image)
                    this.binding?.buttonMintToken?.isEnabled = true

                    bitmap.recycle()
                } else {
                    // Handle the case where the bitmap is null
                    println("Bitmap is null")
                }
            }

            cursor?.close()
        } else {
            println("try again")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}