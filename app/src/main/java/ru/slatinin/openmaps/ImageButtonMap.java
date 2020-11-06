package ru.slatinin.openmaps;

import android.content.Context;
import android.util.AttributeSet;

public class ImageButtonMap extends androidx.appcompat.widget.AppCompatImageButton {
    int tileNumber;
    boolean isSelected = false;

    public ImageButtonMap(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setTileNumber(int tileNumber) {
        this.tileNumber = tileNumber;
    }

    public int getTileNumber() {
        return tileNumber;
    }

}
