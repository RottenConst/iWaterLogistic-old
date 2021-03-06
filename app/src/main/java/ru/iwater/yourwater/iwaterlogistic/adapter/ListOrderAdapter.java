package ru.iwater.yourwater.iwaterlogistic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.iwater.yourwater.iwaterlogistic.R;
import ru.iwater.yourwater.iwaterlogistic.domain.Order;
import ru.iwater.yourwater.iwaterlogistic.ui.activities.AboutOrder;
import ru.iwater.yourwater.iwaterlogistic.ui.activities.IWaterActivity;

public class ListOrderAdapter extends RecyclerView.Adapter<ListOrderAdapter.ListOrderHolder> {

    private LayoutInflater inflater;
    private List<Order> orders;
    private List<String> coord;
    private List<String> times;
    private List<String> period;
//    private DateFormat formatTime;
//    private NotificationSender notificationSender;

    public ListOrderAdapter(Context context, List<Order> orders) {
        this.orders = orders;
        this.inflater = LayoutInflater.from(context);
//        formatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
//        notificationSender = new NotificationSender(context);
        coord = new ArrayList<>();
        times = new ArrayList<>();
        period = new ArrayList<>();
        for (Order order: orders) {
            coord.add(order.getCoords());
            times.add(order.getTime());
            period.add(order.getPeriod());
        }
    }

    @NonNull
    @Override
    public ListOrderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_order_test, parent, false);
        return new ListOrderHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListOrderHolder holder, int position) {
        Order order = orders.get(position);

        holder.onBindView(order, position, coord, times, period);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ListOrderHolder extends RecyclerView.ViewHolder {

        private TextView numOrder;
        private TextView textOrder;
        private CardView cardOrder;
        private String[] splitPeriod;//разделение временного периода, например 9:00-12:00 по "-"
        private String[] formatedDate;

        public ListOrderHolder(@NonNull View itemView) {
            super(itemView);
            cardOrder = itemView.findViewById(R.id.card_order);
            numOrder = itemView.findViewById(R.id.num_order);
            textOrder = itemView.findViewById(R.id.order_info);
        }



        private void onBindView(final Order order, final int position, final List<String> coords, List<String> times, List<String> period){
            numOrder.setText(String.valueOf(position + 1));
            textOrder.setText("№ " + order.getId() + ", " + order.getDate() + ", " + order.getTime() + ", " + order.getAddress());
            splitPeriod = order.getTime().split("-");
            formatedDate = order.getDate().replaceAll("\\s+", "").split("\\.");
            Log.d("Order", "split period " + splitPeriod[0] + " " + splitPeriod[1]);
            if (splitPeriod.length > 1 && timeDifference(splitPeriod[1], formatedDate) < 3800) {
                cardOrder.setBackgroundResource(R.color.red_morning);
                cardOrder.setPreventCornerOverlap(true);
            }

            if (splitPeriod.length > 1 && timeDifference(splitPeriod[1], formatedDate) < 0) {
                cardOrder.setBackgroundResource(R.color.gray);
            }

            cardOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), AboutOrder.class);
                    intent.putExtra("order_id", order.getId());
                    intent.putExtra("order", "№ " + order.getId() + ", " + order.getDate() + ", " + order.getTime() + ", " + order.getAddress());
                    intent.putExtra("coords", order.getCoords());
                    intent.putExtra("order_content", order.getOrder());
                    intent.putExtra("cash", order.getCash());
                    intent.putExtra("cashb", order.getCash_b());
                    intent.putExtra("time", order.getCash());
                    intent.putExtra("name", order.getName());
                    intent.putExtra("contact", order.getContact());
                    intent.putExtra("notice", order.getNotice());
                    intent.putExtra("position", position);
                    intent.putExtra("status", order.getStatus());
                    //данные для карты
                    intent.putExtra("coord", coords.toString());
                    intent.putExtra("times", times.toString());
                    intent.putExtra("period", period.toString());
                    v.getContext().startActivity(intent);
                }
            });
        }

        private long timeDifference(String time, String[] formatedDate) {

            long diff = 0;
            String date = "";

            if (time.replaceAll("\\s+", "").equals("00:00"))
                time = "24:00";

            date += formatedDate[2] + "-" + formatedDate[1] + "-" + formatedDate[0];
            String orderTime = date.replaceAll("\\s+", "") + " " + time.replaceAll("\\s+", "");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                Date date1 = dateFormat.parse(orderTime);
                diff = (date1.getTime() - System.currentTimeMillis()) / 1000;
                Log.d("Date", "date = " + diff);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return diff;
        }
    }
}
