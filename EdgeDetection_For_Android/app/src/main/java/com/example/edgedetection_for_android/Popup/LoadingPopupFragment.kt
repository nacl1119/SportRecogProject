package com.example.edgedetection_for_android.Popup


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.edgedetection_for_android.R
import kotlin.properties.Delegates


object LoadingPopupFragment : DialogFragment() {

    private var mView : View? = null
    var titleString : String by Delegates.observable("잠시만 기다려주세요") { property, oldValue, newValue ->
        if(mView != null){
            var tv = mView?.findViewById<View>(R.id.fragment_loading_popup_message_txt) as TextView
            tv.text = newValue
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mView = inflater.inflate(R.layout.fragment_loading_popup, container, false)
        // Inflate the layout for this fragment
        dialog!!.window!!.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.corner_radius_16dp))
        //mView!!.background =
        return mView
    }

    fun setMessage(msg : String){

        //var tv = mView?.findViewById<View>(R.id.fragment_loading_popup_message_txt) as TextView
        //tv.text = msg

        //val v: View = inflater.inflate(R.layout.fragment_loading_popup, container, false)
        //val tv = v.findViewById<View>(R.id.fragment_loading_popup_message_txt)
        //(tv as TextView).text = "This is an instance of MyDialogFragment"

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}