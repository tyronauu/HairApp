package com.example.besthairstyleapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.media.FaceDetector;

import com.example.visionfaceapi.ui.GraphicOverlay;
import com.google.android.gms.vision.face.Face;

public class FaceGraphic extends GraphicOverlay.Graphic {


    private volatile Face mFace;
    private int mId;
    private Bitmap bitmap;
    private Bitmap bitmap2;

    private static final int FILTER[] = {
            R.drawable.hair,
            R.drawable.hairtestsize,
            R.drawable.hairtestsize2,
            R.drawable.hair4,
            R.drawable.hair5
    };


    FaceGraphic(GraphicOverlay overlay, int i) {
        super(overlay);
        bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(),FILTER[i]);
        bitmap2 = bitmap;
   }

    void setId(int id){
        mId = id;
    }

    void updateFace(Face face, int i){
        mFace = face;
        bitmap = BitmapFactory.decodeResource(getOverlay().getContext().getResources(), FILTER[i]);
        bitmap2 = bitmap;
        bitmap2 = Bitmap.createScaledBitmap(bitmap2,(int)scaleX(face.getWidth()),(int)scaleY(((bitmap.getHeight() * face.getWidth()) / bitmap.getWidth())), false);
        postInvalidate();
    }


    @Override
    public void draw(Canvas canvas){

        Face face = mFace;
        if (face == null) return;

        float x = translateX(face.getPosition().x+face.getWidth() /2);
        float y = translateY(face.getPosition().y+face.getHeight() /2);
        float xOffset = scaleX(face.getWidth() /2);
        float yOffset = scaleY(face.getHeight() /2);
        float left = x - xOffset;
        float top = y - yOffset -220;

        canvas.drawBitmap(bitmap2,left, top,new Paint() );
    }
}
