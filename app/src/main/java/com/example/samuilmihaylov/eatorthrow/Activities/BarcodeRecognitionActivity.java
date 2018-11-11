package com.example.samuilmihaylov.eatorthrow.Activities;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.samuilmihaylov.eatorthrow.Enums.ImageCaptureActionType;
import com.example.samuilmihaylov.eatorthrow.Enums.MessageType;
import com.example.samuilmihaylov.eatorthrow.R;
import com.example.samuilmihaylov.eatorthrow.Utils.NotificationLogger;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.List;
import java.util.Objects;

public class BarcodeRecognitionActivity extends Activity {

    private static final int REQUEST_IMAGE_CAPTURE_BARCODE = 2;
    private TextView mBarcodeTitleTextView;
    private TextView mBarcodeUrlTextView;
    private TextView mBarcodeContactInfoTextView;
    private TextView mBarcodeEmailTextView;

    public BarcodeRecognitionActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_barcode_recognition);

        mBarcodeTitleTextView = findViewById(R.id.barcode_title);
        mBarcodeUrlTextView = findViewById(R.id.barcode_url);
        mBarcodeContactInfoTextView = findViewById(R.id.barcode_contact_info);
        mBarcodeEmailTextView = findViewById(R.id.barcode_email);

        Button snapBarcodeButton = findViewById(R.id.snap_barcode_btn_id);
        snapBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(ImageCaptureActionType.BARCODE);
            }
        });
    }

    private void dispatchTakePictureIntent(ImageCaptureActionType imageCaptureActionType) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageCaptureActionType == ImageCaptureActionType.BARCODE) {
            if (takePictureIntent.resolveActivity(Objects.requireNonNull(this).getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_BARCODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE_BARCODE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = null;
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }

            runBarcodeRecognition(imageBitmap);
        }
    }

    private void runBarcodeRecognition(Bitmap imageBitmap) {

        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_UPC_A,
                                FirebaseVisionBarcode.FORMAT_UPC_E,
                                FirebaseVisionBarcode.FORMAT_AZTEC,
                                FirebaseVisionBarcode.FORMAT_EAN_8,
                                FirebaseVisionBarcode.FORMAT_EAN_13
                        )
                        .build();

        imageBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);

//        FirebaseVisionBarcodeDetector firebaseVisionBarcodeDetector = FirebaseVision.getInstance()
//                .getVisionBarcodeDetector(options);

        FirebaseVisionBarcodeDetector firebaseVisionBarcodeDetector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector();

        firebaseVisionBarcodeDetector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        // Task completed successfully
                        for (FirebaseVisionBarcode barcode : barcodes) {
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                            String rawValue = barcode.getRawValue();

                            int valueType = barcode.getValueType();

                            switch (valueType) {
                                case FirebaseVisionBarcode.TYPE_URL:
                                    String title = Objects.requireNonNull(barcode.getUrl()).getTitle();
                                    mBarcodeTitleTextView.setText(title);

                                    String url = barcode.getUrl().getUrl();
                                    mBarcodeUrlTextView.setText(url);

                                case FirebaseVisionBarcode.TYPE_CONTACT_INFO:
                                    String contactInfo = Objects.requireNonNull(barcode.getContactInfo()).getTitle();
                                    mBarcodeContactInfoTextView.setText(contactInfo);

                                case FirebaseVisionBarcode.TYPE_EMAIL:
                                    String email = Objects.requireNonNull(barcode.getEmail()).getAddress();
                                    mBarcodeEmailTextView.setText(email);

                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        NotificationLogger.showToast(getApplicationContext(), "Error: Could not run the barcode recognition", MessageType.ERROR);
                    }
                });
    }
}
