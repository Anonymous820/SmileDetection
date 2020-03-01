package com.example.smiledetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import com.example.smiledetection.model.SuitcaseFaceDetection;
import com.example.smiledetection.ui.RecyclerFacedetectAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.theartofdev.edmodo.cropper.CropImage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements FrameProcessor {

    private static final String TAG = "ZZZZ";
    FloatingActionButton togglebtn;
//    Facing cameraFacing = Facing.FRONT;
    ImageView imageView;
    CameraView faceDetectionCameraView;
    RecyclerView bottomSheetRecyclerView;
    BottomSheetBehavior bottomSheetBehavior;
    ArrayList<SuitcaseFaceDetection> faceDetectionArrayList;
    FrameLayout bottomSheetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        faceDetectionArrayList = new ArrayList<>();
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomsheet));
        imageView = findViewById(R.id.face_detect_image_view);
        togglebtn = findViewById(R.id.face_detect_camera_button);
//        faceDetectionCameraView = findViewById(R.id.face_detect_camera_view);
        bottomSheetButton = findViewById(R.id.bottom_sheet_button);
        bottomSheetRecyclerView =findViewById(R.id.bottom_sheet_recycler_view);


//        faceDetectionCameraView.setFacing(cameraFacing);
//        faceDetectionCameraView.setLifecycleOwner(MainActivity.this);
//        faceDetectionCameraView.addFrameProcessor(MainActivity.this);


//        togglebtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//             cameraFacing=(cameraFacing==Facing.FRONT)?Facing.BACK:Facing.FRONT;
//
//             faceDetectionCameraView.setFacing(cameraFacing);
//            }
//        });

        bottomSheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().start(MainActivity.this);
            }
        });


        bottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        bottomSheetRecyclerView.setAdapter(new RecyclerFacedetectAdapter(MainActivity.this, faceDetectionArrayList));


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                assert result != null;
                Uri imageuri = result.getUri();

                try {
                    analyzeImage(MediaStore.Images.Media.getBitmap(getContentResolver(), imageuri));
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    private void analyzeImage(final Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "There was an error", Toast.LENGTH_SHORT).show();
        }

            imageView.setImageBitmap(null);
            faceDetectionArrayList.clear();

            Objects.requireNonNull(bottomSheetRecyclerView.getAdapter()).notifyDataSetChanged();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showProgress();

            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

            FirebaseVisionFaceDetectorOptions options =
                    new FirebaseVisionFaceDetectorOptions.Builder()
                            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                            .build();


            FirebaseVisionFaceDetector faceDetector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);

            faceDetector.detectInImage(firebaseVisionImage)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                            Bitmap mutableImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);


                            detectFaces(firebaseVisionFaces, mutableImage);

                            imageView.setImageBitmap(mutableImage);

                            hideProgress();
                            bottomSheetRecyclerView.getAdapter().notifyDataSetChanged();
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(MainActivity.this, "There was Some  Error", Toast.LENGTH_SHORT).show();
                        hideProgress();
                        }
                    });
    }

    private void detectFaces(List<FirebaseVisionFace> firebaseVisionFaces, Bitmap bitmap) {

        if (firebaseVisionFaces == null || bitmap == null) {
            Toast.makeText(this, "There was an error", Toast.LENGTH_SHORT).show();
            return;
        }

        Canvas canvas = new Canvas(bitmap);

        Paint facepaint = new Paint();
        facepaint.setColor(Color.GREEN);
        facepaint.setStyle(Paint.Style.STROKE);
        facepaint.setStrokeWidth(5f);


        Paint facetextPaint = new Paint();
        facepaint.setColor(Color.BLUE);
        facepaint.setTextSize(30f);
        facepaint.setTypeface(Typeface.SANS_SERIF);


        Paint landmarkpaint = new Paint();
        landmarkpaint.setColor(Color.RED);
        landmarkpaint.setStyle(Paint.Style.FILL);
        landmarkpaint.setStrokeWidth(8f);

        for (int i = 0; i < firebaseVisionFaces.size(); i++) {
            canvas.drawRect(firebaseVisionFaces.get(i).getBoundingBox(), facepaint);

            canvas.drawText("Face: "+i,
                    (firebaseVisionFaces.get(i).getBoundingBox().centerX())
                            - (firebaseVisionFaces.get(i).getBoundingBox().width() >> 1) + 8f
                    ,
                    (firebaseVisionFaces.get(i).getBoundingBox().centerY())
                            + (firebaseVisionFaces.get(i).getBoundingBox().height() >> 1) - 8f,
                    facetextPaint);

            FirebaseVisionFace face = firebaseVisionFaces.get(i);
            //get 1 Face

            if (face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE) != null) {

                FirebaseVisionFaceLandmark lefteye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);

                assert lefteye != null;
                canvas.drawCircle(lefteye.getPosition().getX(),
                        lefteye.getPosition().getY(),
                        8f,
                        landmarkpaint);

            }
            if (face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE) != null) {

                FirebaseVisionFaceLandmark righteye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                assert righteye != null;
                canvas.drawCircle(righteye.getPosition().getX(),
                        righteye.getPosition().getY(),
                        8f,
                        landmarkpaint);

            }

            if (face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE) != null) {

                FirebaseVisionFaceLandmark noseBase = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);

                assert noseBase != null;
                canvas.drawCircle(noseBase.getPosition().getX(),
                        noseBase.getPosition().getY(),
                        8f,
                        landmarkpaint);

            }

            if (face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR) != null) {

                FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);

                assert leftEar != null;
                canvas.drawCircle(leftEar.getPosition().getX(),
                        leftEar.getPosition().getY(),
                        8f,
                        landmarkpaint);

            }

            if (face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR) != null) {

                FirebaseVisionFaceLandmark rightEar = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR);

                assert rightEar != null;
                canvas.drawCircle(rightEar.getPosition().getX(),
                        rightEar.getPosition().getY(),
                        8f,
                        landmarkpaint);

            }

            if (face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM) != null &&
                    face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT) != null &&
                    face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT) != null) {

                FirebaseVisionFaceLandmark mouthBottom = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
                FirebaseVisionFaceLandmark mouthLeft = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
                FirebaseVisionFaceLandmark mouthRight = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT);

                assert mouthBottom != null;
                assert mouthLeft != null;
                canvas.drawLine(mouthLeft.getPosition().getX(),
                        mouthLeft.getPosition().getY(),
                        mouthBottom.getPosition().getX(),
                        mouthBottom.getPosition().getY(), landmarkpaint);

                assert mouthRight != null;
                canvas.drawLine(mouthBottom.getPosition().getX(),
                        mouthBottom.getPosition().getY(),
                        mouthRight.getPosition().getX(),
                        mouthRight.getPosition().getY(), landmarkpaint);
            }

            int smile= (int) (face.getSmilingProbability()*100);
            int lefteye= (int) (face.getLeftEyeOpenProbability()*100);
            int righteye= (int) (face.getLeftEyeOpenProbability()*100);

            faceDetectionArrayList.add(new SuitcaseFaceDetection(i, "SmilingProbability : " + smile));
            faceDetectionArrayList.add(new SuitcaseFaceDetection(i, "LeftEyeOpenProbability : " + lefteye));
            faceDetectionArrayList.add(new SuitcaseFaceDetection(i, "RightEyeOpenProbability : " + righteye));



            Objects.requireNonNull(bottomSheetRecyclerView.getAdapter()).notifyDataSetChanged();


            Log.d(TAG, "detectFaces: "+faceDetectionArrayList.toString());


        }//end for Loop


    }

    private void showProgress() {
        findViewById(R.id.bottom_sheet_image_camera).setVisibility(View.GONE);
        findViewById(R.id.bottom_sheet_progress_bar).setVisibility(View.VISIBLE);
    }


    private void hideProgress() {
        findViewById(R.id.bottom_sheet_image_camera).setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_sheet_progress_bar).setVisibility(View.GONE);
    }

    @Override
    public void process(@NonNull Frame frame) {
        int width=frame.getSize().getWidth();
        int height=frame.getSize().getHeight();


        FirebaseVisionImageMetadata metadata=new FirebaseVisionImageMetadata.Builder()
                .setWidth(width)
                .setHeight(height)
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
//                .setRotation((cameraFacing==Facing.FRONT)
//                            ?FirebaseVisionImageMetadata.ROTATION_270:
//                            FirebaseVisionImageMetadata.ROTATION_90)
                .build();
    }
}
