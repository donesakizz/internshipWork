package com.example.finalpro.ui.view.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.finalpro.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.RegisterButton.setOnClickListener{register(it) }
        binding.SigninButton.setOnClickListener { signin(it) }

        val currentUser = auth.currentUser
        if (currentUser !=null) {
            //İf user signed before
            val action= UserFragmentDirections.actionUserFragmentToFeedFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    fun register(view : View) {

        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    //user is created by firebase
                    val action = UserFragmentDirections.actionUserFragmentToFeedFragment()
                    Navigation.findNavController(view).navigate(action)
                }
            }.addOnFailureListener {exception ->
                Toast.makeText(requireContext(),exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun signin(view: View) {

        val email = binding.emailText.text.toString()
        val password = binding.passwordText.text.toString()
        if(email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                val action = UserFragmentDirections.actionUserFragmentToFeedFragment()
                Navigation.findNavController(view).navigate(action)
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

    }

    //We can able to make binding null with onDestroy function.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}