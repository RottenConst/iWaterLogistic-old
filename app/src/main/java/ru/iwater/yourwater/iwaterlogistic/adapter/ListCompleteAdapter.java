package ru.iwater.yourwater.iwaterlogistic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.domain.Order;
import ru.iwater.yourwater.iwaterlogistic.ui.activities.AboutOrder;

public class ListCompleteAdapter extends RecyclerView.Adapter<ListCompleteAdapter.ListCompleteHolder> {

    private LayoutInflater inflater;
    private List<Order> orders;

    public ListCompleteAdapter(Context context, List<Order> orders) {
        this.orders = orders;
        this.inflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public ListCompleteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_complete_order, parent, false);
        return new ListCompleteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListCompleteHolder holder, int position) {
        Order order = orders.get(position);
        holder.onBindView(order, position);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ListCompleteHolder extends RecyclerView.ViewHolder {

        private TextView numOrder;
        private TextView textOrder;
        private CardView cardOrder;

        public ListCompleteHolder(@NonNull View itemView) {
            super(itemView);
            numOrder = itemView.findViewById(R.id.num_complete_order);
            textOrder = itemView.findViewById(R.id.complete_order_info);
            cardOrder = itemView.findViewById(R.id.card_complete_order);
        }

        @SuppressLint("SetTextI18n")
        private void onBindView(Order order, int position) {
            numOrder.setText(String.valueOf(position + 1));
            textOrder.setText("№" + order.getId() + ", " + order.getDate() + ", " + order.getName() + ", " + order.getAddress());
            cardOrder.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), AboutOrder.class);
                intent.putExtra("order_id", order.getId());
                intent.putExtra("order", "№ " + order.getId() + ", " + order.getDate() + ", " + order.getTime() + ", " + order.getAddress());
                intent.putExtra("coords", order.getCoords());
                intent.putExtra("address", order.getAddress());
                intent.putExtra("order_content", order.getOrder());
                intent.putExtra("cash", order.getCash());
                intent.putExtra("cashb", order.getCash_b());
                intent.putExtra("time", order.getTime());
                intent.putExtra("name", order.getName());
                intent.putExtra("contact", order.getContact());
                intent.putExtra("notice", order.getNotice());
                intent.putExtra("status", order.getStatus());
                v.getContext().startActivity(intent);
            });
        }
    }
}
