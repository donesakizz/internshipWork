package com.example.finalpro.ui.view.upload

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.finalpro.databinding.FragmentUploadBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID


class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedPicture : Uri? = null
    var selectedBitmap : Bitmap? = null

    private lateinit var auth : FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore

        registerLauncher()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentUploadBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.UploadButton.setOnClickListener { uploadClicked(it) }
        binding.imageView.setOnClickListener { imageSelect(it) }
    }

    fun uploadClicked(view: View) {

        val uuid =UUID.randomUUID()
        val imageName = "${uuid}.jpg"

        val reference = storage.reference
        val imageReference = reference.child("images").child(imageName)
        if (selectedPicture != null){
            imageReference.putFile(selectedPicture!!).addOnSuccessListener { uploadTask ->
                //Want url of uploaded file
                imageReference.downloadUrl.addOnSuccessListener { uri ->
                    if (auth.currentUser != null) {
                        val downloadUrl = uri.toString()
                        println(downloadUrl)

                        //We need to record in the  database

                        val postMap = hashMapOf<String, Any>()
                        postMap.put("downloadUrl", downloadUrl)
                        postMap.put("email", auth.currentUser!!.email.toString())
                        postMap.put("comment",binding.commentText.text.toString())
                        postMap.put("date",Timestamp.now())

                        db.collection("Posts").add(postMap).addOnSuccessListener { documentReference ->
                            //data is uploaded to the database
                            val action = UploadFragmentDirections.actionUploadFragmentToFeedFragment()
                            Navigation.findNavController(view).navigate(action)

                        }.addOnFailureListener { exception->
                            Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }


            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

    }

    fun imageSelect(view: View) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // read media images
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
            ) {
                //permission haven't
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(), android.Manifest.permission.READ_MEDIA_IMAGES)) {
                    //Show permission logic to user
                     Snackbar.make(view,"Galeriye gitmek için izin vermeniz gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction("İzin ver",
                    View.OnClickListener {
                        //We need to ask for permission
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                         }).show()
                } else{
                    //we need to ask for permission again
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                //permission have
                //Write a code of going to gallery
                val intenToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intenToGallery)
                }

            } else {
                // read external images
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                //permission haven't
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //Show permission logic to user
                    Snackbar.make(view,"Galeriye gitmek için izin vermeniz gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction("İzin ver",
                        View.OnClickListener {
                            //We need to ask for permission
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else{
                    //we need to ask for permission again
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                //permission have
                //Write a code of going to gallery
                val intenToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intenToGallery)
            }

        }

    }


    private fun registerLauncher() {
        activityResultLauncher  = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK)  {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    selectedPicture = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedPicture!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)

                        } else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedPicture)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if (result) {
                //allowed by user
                val intenToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intenToGallery)
            } else {
                //deny by user
                Toast.makeText(requireContext(), "İzni reddettiniz, izne ihtiyacınız var ", Toast.LENGTH_LONG).show()
            }
        }

    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}