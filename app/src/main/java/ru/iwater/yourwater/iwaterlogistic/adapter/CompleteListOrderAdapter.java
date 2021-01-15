package ru.iwater.yourwater.iwaterlogistic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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

public class CompleteListOrderAdapter extends RecyclerView.Adapter<CompleteListOrderAdapter.CompleteListHolder> {

    private LayoutInflater inflater;
    private List<Order> orders;

    public CompleteListOrderAdapter(Context context, List<Order> orders){
        this.orders = orders;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public CompleteListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_list_order, parent, false);
        return new CompleteListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompleteListHolder holder, int position) {
        Order order = orders.get(position);
        holder.onBindView(order, position);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class CompleteListHolder extends RecyclerView.ViewHolder {

        private CardView cardOrder;
        private TextView numOrder;
        private TextView textOrder;

        public CompleteListHolder(@NonNull View itemView) {
            super(itemView);
            cardOrder = itemView.findViewById(R.id.order);
            numOrder = itemView.findViewById(R.id.numOrder);
            textOrder = itemView.findViewById(R.id.orderAddress);

        }

        @SuppressLint("ResourceAsColor")
        private void onBindView(final Order order, final int position) {
            numOrder.setText(String.valueOf(position + 1));
            numOrder.setBackgroundResource(R.drawable.gray_circle);
            textOrder.setText(("№ " + order.getId() + ", " + order.getDate() + ", " + order.getPeriod() + ", " + order.getAddress()));
            cardOrder.setCardBackgroundColor(R.color.gray);
            cardOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), AboutOrder.class);
                    intent.putExtra("order_id", order.getId());
                    intent.putExtra("order", "№ " + order.getId() + ", " + order.getDate() + ", " + order.getPeriod() + ", " + order.getAddress());
                    intent.putExtra("coords", order.getCoords());
                    intent.putExtra("order_content", order.getOrder());
                    intent.putExtra("cash", order.getCash());
                    intent.putExtra("name", order.getName());
                    intent.putExtra("contact", order.getContact());
                    intent.putExtra("notice", order.getNotice());
                    intent.putExtra("position", position);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}
