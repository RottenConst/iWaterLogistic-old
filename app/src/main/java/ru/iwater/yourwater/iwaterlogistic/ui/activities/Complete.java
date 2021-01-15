package ru.iwater.yourwater.iwaterlogistic.ui.activities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

import ru.iwater.yourwater.iwaterlogistic.R;

public class Complete extends AppCompatActivity {

    private String order = "";//данные заказа
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete);

        Intent intent = getIntent();
        //id заказа
        String order_id = intent.getStringExtra("order_id");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            order = Objects.requireNonNull(intent.getStringExtra("order")).replaceAll("№"+ order_id +",\\s","");
        }
        position = intent.getIntExtra("position",0);
        //возврат к текущему путевому листу
        Button returnToList = (Button) findViewById(R.id.button);
        //надпись Заказ №... выполнен!
        TextView orderComplete = (TextView) findViewById(R.id.orderComplete);
        //надпись с номером и адресом заказа
        TextView address = (TextView) findViewById(R.id.address);

        orderComplete.setText("Заказ №"+ order_id +" выполнен!");
        address.setText(order);

        returnToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Complete.this, IWaterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });

    }
}
