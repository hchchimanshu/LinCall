package com.himanshuhc.lincall.presentation.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.himanshuhc.lincall.databinding.ItemCallHistoryBinding
import java.text.DateFormat
import java.util.Date

class CallHistoryAdapter(
    private val history: List<CallHistory>
) : RecyclerView.Adapter<CallHistoryAdapter.CallViewHolder>() {

    inner class CallViewHolder(val binding: ItemCallHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        val binding = ItemCallHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CallViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        val item = history[position]
        holder.binding.tvNumber.text = item.phoneNumber
        holder.binding.tvType.text = when (item.callType) {
            is CallType.Incoming -> "Incoming"
            is CallType.Outgoing -> "Outgoing"
            is CallType.Missed -> "Missed"
        }
        holder.binding.tvTime.text = DateFormat.getDateTimeInstance().format(Date(item.timestamp))
    }

    override fun getItemCount() = history.size
}
