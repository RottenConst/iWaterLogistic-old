package ru.iwater.yourwater.iwaterlogistic.ui.fragments;


import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import ru.iwater.yourwater.iwaterlogistic.R;

public class FragmentNumberPicker extends Fragment {

    private static final String ARG_PARAM1 = "value";//для передачи параметров
    private String value="";//переданный параметр

    public FragmentNumberPicker() {
    }

    public static FragmentNumberPicker newInstance(String value) {
        FragmentNumberPicker fragment = new FragmentNumberPicker();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, value);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            value = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_number_picker, container, false);

        final NumberPicker numberPicker = new NumberPicker(getActivity().getApplicationContext());
        FrameLayout frameLayout = (FrameLayout) v.findViewById(R.id.frameWithFrame);
        FrameLayout frame = (FrameLayout) v.findViewById(R.id.frame);
        //final TextView textView = (TextView) getActivity().findViewById(R.id.textView22);

        if(!value.equals(""))
            numberPicker.setValue(Integer.parseInt(value));
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(100);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setDividerDrawable(getResources().getDrawable(R.drawable.cursor));
        setDividerColor(numberPicker, Color.WHITE);
        frame.addView(numberPicker);

        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //textView.setText(String.valueOf(numberPicker.getValue()));
                getFragmentManager().popBackStack();
            }
        });
        return v;
    }

    //region установка цвета разделителей в numberpicker
    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
    //endregion

}
