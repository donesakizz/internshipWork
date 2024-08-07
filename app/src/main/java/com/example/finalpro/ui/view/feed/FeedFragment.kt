package com.example.finalpro.ui.view.feed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalpro.R
import com.example.finalpro.databinding.FragmentFeedBinding
import com.example.finalpro.model.Post
import com.example.finalpro.ui.adapter.PostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FeedFragment : Fragment() , PopupMenu.OnMenuItemClickListener{
    private var _binding: FragmentFeedBinding? = null

    private val binding get() = _binding!!
    private lateinit var popup: PopupMenu
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    val postList : ArrayList<Post> = arrayListOf()
    private var adapter : PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { floatingButtonClicked(it) }

        // We connect the menu to the code with PopupMenu. It takes the context and which view it will be connected to as parameters.
         popup = PopupMenu(requireContext(),binding.floatingActionButton)
        //We connect the menu xml to the code with MenuInflater
        val inflater = popup.menuInflater
        //inflate takes  the resources and  which  menu it will be connected to as parameters.
        inflater.inflate(R.menu.popup_menu,popup.menu)
        popup.setOnMenuItemClickListener(this)  // Anymore feedFragment is Onmenuıtemclicklistener

        GetDataFromFireStore()

        adapter = PostAdapter(postList)
        binding.feedRecylerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecylerView.adapter = adapter

    }

    private fun GetDataFromFireStore(){
        db.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()
            }else {
                if (value != null){
                    if (!value.isEmpty){
                        //if isn't empty
                        postList.clear()
                        val documents = value.documents
                        for (document in documents){
                            val comment = document.get("comment") as String //casting
                            // as come like any we make casting process using as
                            val email = document.get("email") as String
                            val downloadUrl = document.get("downloadUrl") as String

                           // println(comment)

                            val post = Post(email,comment,downloadUrl )
                            postList.add(post)
                        }
                        adapter?.notifyDataSetChanged()//New datas came to me , begin adapter reagain so ı can see adapter's contents
                    }
                }
            }
        }

    }

    fun floatingButtonClicked(view: View) {
        //I moved codes about showing popupmenu to here from onViewCreated.Because of when ever clicked to
        //to it iniliaze at every clicked.Move to onViewCreated this and
        //İf you use onViewCreated remove val variables and define a lateinit var instead of val.
        //After then this function's task will be just popup.show
        popup.show()
    }


    //We can able to make binding null with onDestroy function.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
               if (item?.itemId== R.id.uploadItem){
                   val action = FeedFragmentDirections.actionFeedFragmentToUploadFragment()
                   Navigation.findNavController(requireView()).navigate(action)
               }
        else if (item?.itemId == R.id.logoutItem) {
            //logout process
            auth.signOut()
            val action = FeedFragmentDirections.actionFeedFragmentToUserFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
        return true

    }


}