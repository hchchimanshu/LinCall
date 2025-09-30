package com.himanshuhc.lincall.presentation.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.himanshuhc.lincall.R
import com.himanshuhc.lincall.data.linphone.LinphoneManager

class CallHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CallHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate your fragment layout
        return inflater.inflate(R.layout.fragment_call_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvCallHistory)

        // Fake data for now
        val sampleData = listOf(
            CallHistory("9876543210", CallType.Outgoing, System.currentTimeMillis()),
            CallHistory("9123456789", CallType.Incoming, System.currentTimeMillis() - 3600000),
            CallHistory("9988776655", CallType.Missed, System.currentTimeMillis() - 7200000)
        )

//        adapter = CallHistoryAdapter(sampleData)
        adapter = CallHistoryAdapter(LinphoneManager.getCallHistory())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

}