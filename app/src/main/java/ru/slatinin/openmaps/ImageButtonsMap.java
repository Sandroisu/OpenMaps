package ru.slatinin.openmaps;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ImageButtonsMap extends ConstraintLayout implements View.OnClickListener {
    private final ArrayList<ImageButtonMap> buttons = new ArrayList<>();
    private final ArrayList<String> activeTiles = new ArrayList<>();

    public ImageButtonsMap(@NonNull Context context, AttributeSet attr) {
        super(context, attr);
        this.addView(((LayoutInflater) this.getContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.image_button_map, null));
        ImageButtonMap piece_1 = findViewById(R.id.piece_1);
        ImageButtonMap piece_2 = findViewById(R.id.piece_2);
        ImageButtonMap piece_3 = findViewById(R.id.piece_3);
        ImageButtonMap piece_4 = findViewById(R.id.piece_4);
        ImageButtonMap piece_5 = findViewById(R.id.piece_5);
        ImageButtonMap piece_6 = findViewById(R.id.piece_6);
        ImageButtonMap piece_7 = findViewById(R.id.piece_7);
        ImageButtonMap piece_8 = findViewById(R.id.piece_8);
        ImageButtonMap piece_9 = findViewById(R.id.piece_9);
        ImageButtonMap piece_10 = findViewById(R.id.piece_10);
        ImageButtonMap piece_11 = findViewById(R.id.piece_11);
        ImageButtonMap piece_12 = findViewById(R.id.piece_12);
        ImageButtonMap piece_13 = findViewById(R.id.piece_13);
        ImageButtonMap piece_14 = findViewById(R.id.piece_14);
        ImageButtonMap piece_15 = findViewById(R.id.piece_15);
        ImageButtonMap piece_16 = findViewById(R.id.piece_16);
        piece_1.setTileNumber(1);
        piece_2.setTileNumber(2);
        piece_3.setTileNumber(3);
        piece_4.setTileNumber(4);
        piece_5.setTileNumber(5);
        piece_6.setTileNumber(6);
        piece_7.setTileNumber(7);
        piece_8.setTileNumber(8);
        piece_9.setTileNumber(9);
        piece_10.setTileNumber(10);
        piece_11.setTileNumber(11);
        piece_12.setTileNumber(12);
        piece_13.setTileNumber(13);
        piece_14.setTileNumber(14);
        piece_15.setTileNumber(15);
        piece_16.setTileNumber(16);
        buttons.add(piece_1);
        buttons.add(piece_2);
        buttons.add(piece_3);
        buttons.add(piece_4);
        buttons.add(piece_5);
        buttons.add(piece_6);
        buttons.add(piece_7);
        buttons.add(piece_8);
        buttons.add(piece_9);
        buttons.add(piece_10);
        buttons.add(piece_11);
        buttons.add(piece_12);
        buttons.add(piece_13);
        buttons.add(piece_14);
        buttons.add(piece_15);
        buttons.add(piece_16);
        piece_1.setOnClickListener(this);
        piece_2.setOnClickListener(this);
        piece_3.setOnClickListener(this);
        piece_4.setOnClickListener(this);
        piece_5.setOnClickListener(this);
        piece_6.setOnClickListener(this);
        piece_7.setOnClickListener(this);
        piece_8.setOnClickListener(this);
        piece_9.setOnClickListener(this);
        piece_10.setOnClickListener(this);
        piece_11.setOnClickListener(this);
        piece_12.setOnClickListener(this);
        piece_13.setOnClickListener(this);
        piece_14.setOnClickListener(this);
        piece_15.setOnClickListener(this);
        piece_16.setOnClickListener(this);

        for (int i = 1; i < buttons.size()+1; i++) {
            activeTiles.add(String.valueOf(i));
        }
    }



    @Override
    public void onClick(View v) {
        ImageButtonMap ibm = (ImageButtonMap) v;
        ibm.setIsSelected(!ibm.getIsSelected());
        if (ibm.isSelected) {
            ibm.setColorFilter(Color.argb(150, 155, 155, 255), PorterDuff.Mode.SRC_ATOP);
        } else {
            ibm.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
        }
    }

    public void setMarked(int mark) {
        for (ImageButtonMap ibm:buttons) {
            if (ibm.getTileNumber()==mark){
                ibm.setColorFilter(Color.argb(150, 255, 155, 155), PorterDuff.Mode.SRC_ATOP);
                ibm.setIsSelected(false);
                ibm.setEnabled(false);
            }
        }
    }

    public ArrayList<Integer> getSelectedTiles() {
        ArrayList<Integer> selectedTiles = new ArrayList<>();
        for (ImageButtonMap ibm : buttons) {
            if (ibm.getIsSelected()) {
                selectedTiles.add(ibm.getTileNumber());
            }
        }
        return selectedTiles;
    }

    public ArrayList<String> getActiveTiles(){
        return activeTiles;
    }
}
