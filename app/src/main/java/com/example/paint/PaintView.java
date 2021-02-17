package com.example.paint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PaintView extends View {

    private static final String TAG = "234";
    public static int BRUSH_SIZE = 10;
    public static final int DEFAULT_COLOR = Color.RED;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY, dx, dy;
    private float startX, startY, endX, endY;
    private Path mPath;
    private Paint mPaint;
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;
    private String figure;
    private Bitmap mBitmap, image;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    private ArrayList<Draw> paths = new ArrayList<>();
    private Bitmap backBit;
    private boolean isBackBit;
    private PaintView paintView;
    int height=0;
    int width=0;

    public PaintView(Context context) {
        super(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {

        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//сглаживание
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);//при наложении цветов
        mPaint.setAlpha(0xff);

    }

    public void setImageBitmap(Bitmap bitmap) {
        backBit = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        backBit = bitmap;
        isBackBit = true;
    }

    public void initialise(DisplayMetrics displayMetrics) {

         height = displayMetrics.heightPixels;
         width = displayMetrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        currentColor = DEFAULT_COLOR;
        strokeWidth = BRUSH_SIZE;
    }

    public void eraser() {
        currentColor = Color.WHITE;
    }

    public void tiny() {
        strokeWidth = 10;
    }

    public void wide() {
        strokeWidth = 30;
    }

    public void normal() {
        strokeWidth = 20;
    }

    public void line() {
        figure = "line";
    }

    public void circle() {
        figure = "circle";
    }

    public void square() {
        figure = "square";
    }

    @Override
    protected void onDraw(Canvas canvas) {



        mCanvas.drawColor(backgroundColor);
        if (image != null) {
            mCanvas.drawBitmap(image, 0, 0, mBitmapPaint);
        }
        for (Draw draw : paths) {
            mPaint.setColor(draw.color);
            mPaint.setStrokeWidth(draw.strokeWidth);
            mPaint.setMaskFilter(null);
            mCanvas.drawPath(draw.path, mPaint);
            if (figure == "circle") {
                mCanvas.drawCircle(startX, startY,
                        (float) Math.sqrt(Math.pow((startX - endX), 2) + Math.pow((startY - endY), 2)), mPaint);
            }
        }

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);




    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        Draw draw = new Draw(currentColor, strokeWidth, mPath);
        paths.add(draw);

        mPath.reset();

        mPath.moveTo(x, y);

        mX = x;
        mY = y;
        startX = x;
        startY = y;
    }

    private void touchMove(float x, float y) {

        dx = Math.abs(x - mX);
        dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            if (figure == "line") {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            }

            mX = x;
            mY = y;

        }
        endX = x;
        endY = y;
    }

    private void touchUp() {
        if (figure == "line") {
            mPath.lineTo(mX, mY);
        }
        if (figure == "square") {
            mPath.addRect(startX, startY, endX, endY, Path.Direction.CW);
        }
        if (figure == "circle") {
            mPath.addCircle(startX, startY,
                    (float) Math.sqrt(Math.pow((startX - endX), 2) + Math.pow((startY - endY), 2)),
                    Path.Direction.CW);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;

        }

        return true;

    }

    public void clear() {
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        invalidate();
    }


    public void setColor(int color) {
        currentColor = color;
    }

    public void save() {
        int coint = 0;
        File sdDirectory = Environment.getExternalStorageDirectory();
        File subDirectory = new File(sdDirectory.toString() + "/Pictures/Paint");

        if (subDirectory.exists()) {
            File[] existing = subDirectory.listFiles();
            for (File file : existing) {
                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
                    coint++;
                }
            }
        } else {
            subDirectory.mkdir();
        }
        if (subDirectory.exists()) {
            File image = new File(subDirectory, "/drawing" + (coint + 1) + ".png");
            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(image);

                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

                fileOutputStream.flush();
                fileOutputStream.close();

                Toast.makeText(getContext(), "saved", Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
        }
    }

    public void setImage(Bitmap bitmap) {

            image = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
            invalidate();

        Toast.makeText(getContext(), "edit", Toast.LENGTH_LONG).show();
    }
}