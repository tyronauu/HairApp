package com.example.besthairstyleapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;


import com.example.visionfaceapi.ui.CameraSourcePreview;
import com.example.visionfaceapi.ui.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.material.snackbar.Snackbar;

import static android.view.View.GONE;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 255;

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private boolean mIsFrontFacing = true;
    private int filterType = 0;

    private static final int FILTER[] = {
        R.id.hair1,
        R.id.hair2,
        R.id.hair3,
        R.id.hair4,
        R.id.hair5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        final ImageButton flipButton = (ImageButton) findViewById(R.id.flipButton);

        flipButton.setOnClickListener(mSwitchCameraButtonListener);
        if (savedInstanceState != null) {
            mIsFrontFacing = savedInstanceState.getBoolean("IsFrontFacing");
        }


        //scrollView
        ImageButton filterButton = (ImageButton)findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findViewById(R.id.scrollView).getVisibility() == GONE){
                    findViewById(R.id.scrollView).setVisibility(View.VISIBLE);

                    ((ImageButton)findViewById(R.id.filterButton)).setImageResource(R.drawable.switch_cameras);
                }else{
                    findViewById(R.id.scrollView).setVisibility(GONE);
                    ((ImageButton)findViewById(R.id.filterButton)).setImageResource(R.drawable.switch_cameras);
                }
            }
        });

        //hair filters selection
        ImageButton hair1 = (ImageButton)findViewById(R.id.hair1);
        hair1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(FILTER[filterType]).setBackgroundResource(R.drawable.round);
                filterType = 0;
                findViewById(FILTER[filterType]).setBackgroundResource(R.drawable.round_select);
            }
        });

        ImageButton hair2 = (ImageButton)findViewById(R.id.hair2);
        hair2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(FILTER[filterType]).setBackgroundResource(R.drawable.round);
                filterType = 1;
                findViewById(FILTER[filterType]).setBackgroundResource(R.drawable.round_select);
            }
        });

        ImageButton hair3 = (ImageButton)findViewById(R.id.hair3);
        hair3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(FILTER[filterType]).setBackgroundResource(R.drawable.round);
                filterType = 2;
                findViewById(FILTER[filterType]).setBackgroundResource(R.drawable.round_select);
            }
        });

        ImageButton hair4 = (ImageButton)findViewById(R.id.hair4);
        hair4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(FILTER[filterType]).setBackgroundResource(R.drawable.round);
                filterType = 3;
                findViewById(FILTER[filterType]).setBackgroundResource(R.drawable.round_select);
            }
        });

        ImageButton hair5 = (ImageButton)findViewById(R.id.hair5);
        hair5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(FILTER[filterType]).setBackgroundResource(R.drawable.round);
                filterType = 4;
                findViewById(FILTER[filterType]).setBackgroundResource(R.drawable.round_select);
            }
        });

        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    private View.OnClickListener mSwitchCameraButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            mIsFrontFacing = !mIsFrontFacing;

            if (mCameraSource != null) {
                mCameraSource.release();
                mCameraSource = null;
            }
            createCameraSource();
            startCameraSource();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("IsFrontFacing", mIsFrontFacing);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission not acquired. Requesting permission.");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_CAMERA_PERM);
            }
        };
        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // We have permission to access the camera, so create the camera source.
            Log.d(TAG, "Camera permission granted - initializing camera source.");
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.disappointed_ok, listener)
                .show();
    }

    // Camera source
    private void createCameraSource() {
        Log.d(TAG, "createCameraSource called.");

        Context context = getApplicationContext();
        FaceDetector detector = createFaceDetector(context);

        int facing = CameraSource.CAMERA_FACING_FRONT;
        if (!mIsFrontFacing) {
            facing = CameraSource.CAMERA_FACING_BACK;
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setFacing(facing)
                .setRequestedPreviewSize(640, 480)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    private void startCameraSource() {
        Log.d(TAG, "startCameraSource called.");

        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    // Face detector
    @NonNull
    private FaceDetector createFaceDetector(final Context context) {
        Log.d(TAG, "createFaceDetector called.");

        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setTrackingEnabled(true)
                .setMode(FaceDetector.FAST_MODE)
                .build();

        MultiProcessor.Factory<Face> factory = new MultiProcessor.Factory<Face>() {
            @Override
            public Tracker<Face> create(Face face) {
                return new GraphicFaceTracker(mGraphicOverlay);
            }
        };

        Detector.Processor<Face> processor = new MultiProcessor.Builder<>(factory).build();
        detector.setProcessor(processor);

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");

            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Log.w(TAG, getString(R.string.low_storage_error));
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.app_name)
                        .setMessage(R.string.low_storage_error)
                        .setPositiveButton(R.string.disappointed_ok, listener)
                        .show();
            }
        }
        return detector;
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
           mFaceGraphic = new FaceGraphic(overlay,filterType);
        }

        @Override
        public void onNewItem(int id, Face item){
            mFaceGraphic.setId(id);
        }
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face){
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face,filterType);
        }
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults){
            mOverlay.remove(mFaceGraphic);
        }
        @Override
        public void onDone(){
            mOverlay.remove(mFaceGraphic);
        }
    }
}
