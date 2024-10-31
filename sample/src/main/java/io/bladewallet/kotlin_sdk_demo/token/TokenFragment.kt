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
import io.bladewallet.bladesdk.KeyRecord
import io.bladewallet.bladesdk.KeyType
import io.bladewallet.bladesdk.IPFSProviderConfig
import io.bladewallet.bladesdk.IPFSProvider
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
                KeyRecord(Config.accountPrivateKey2, KeyType.admin)
            )
            Blade.createToken(
                    treasuryAccountId = Config.accountAddress,
                    supplyPrivateKey = Config.accountPrivateKey,
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
                tokenId = binding!!.tokenIdEditText.text.toString(),
                supplyAccountId = Config.accountAddress,
                supplyPrivateKey = Config.accountPrivateKey,
                file = base64Image,
                metadata = metaData,
                storageConfig = IPFSProviderConfig(
                    provider = IPFSProvider.pinata,
                    token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mb3JtYXRpb24iOnsiaWQiOiIwYzJiMGM2Yi0zNzI2LTQ5YmMtYjgxZi0yOGIxMjViM2EzMTYiLCJlbWFpbCI6InRoZS5nYXJ5LmR1QGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJwaW5fcG9saWN5Ijp7InJlZ2lvbnMiOlt7ImRlc2lyZWRSZXBsaWNhdGlvbkNvdW50IjoxLCJpZCI6IkZSQTEifSx7ImRlc2lyZWRSZXBsaWNhdGlvbkNvdW50IjoxLCJpZCI6Ik5ZQzEifV0sInZlcnNpb24iOjF9LCJtZmFfZW5hYmxlZCI6ZmFsc2UsInN0YXR1cyI6IkFDVElWRSJ9LCJhdXRoZW50aWNhdGlvblR5cGUiOiJzY29wZWRLZXkiLCJzY29wZWRLZXlLZXkiOiI0NjZjZDlkMDUwNWUzNDAyYjk2YSIsInNjb3BlZEtleVNlY3JldCI6IjY0ODM1MDhlM2Q3OTgzNDlkYzUzNWJiMDRkYWViMWFlZmU4NjdlZjJiMDhhNjhhNzlkNWYwZDRlOTU5YTUxZTciLCJleHAiOjE3NTYzMTQ5MjJ9.yK8QpXW4aVIwCGOfVwbSlNM4GVHcGdH1W8YD3adGua4"
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
                accountId = Config.accountAddress2,
                accountPrivateKey = Config.accountPrivateKey2
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}")
                }
            }
        }

        binding!!.buttonSendToken.setOnClickListener {
            output("")
            Blade.transferTokens(
                tokenId = binding?.tokenIdEditText?.text.toString(),
                accountId = Config.accountAddress,
                accountPrivateKey = Config.accountPrivateKey,
                receiverId = Config.accountAddress2,
                amountOrSerial = 1.0,
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