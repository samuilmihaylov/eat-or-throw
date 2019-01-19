package com.example.samuilmihaylov.eatorthrow.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.samuilmihaylov.eatorthrow.Enums.ImageCaptureActionType;
import com.example.samuilmihaylov.eatorthrow.Enums.MessageType;
import com.example.samuilmihaylov.eatorthrow.Fragments.DatePickerFragment;
import com.example.samuilmihaylov.eatorthrow.R;
import com.example.samuilmihaylov.eatorthrow.Utils.NotificationLogger;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TextRecognitionActivity extends FragmentActivity {

    private static final int REQUEST_IMAGE_CAPTURE_EXPIRE_DATE = 1;

    private static final List<String> formatStrings = Arrays.asList(
            "MM/yy",
            "MM/dd/yy",
            "MM-dd-yy",
            "M yyyy",
            "MM yyyy",
            "MM/yyyy",
            "MM-dd-yyyy",
            "dd-MM-yy",
            "dd.MM.yy",
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "dd.MM.yyyy",
            "dd MM yyyy",
            "ddMMyyyy");

    private ImageView mImageView;
    private EditText mTextView;
    private Spinner mSpinner;

    public TextRecognitionActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_text_recognition);

        mImageView = findViewById(R.id.photo_image_id);
//        mTextView = findViewById(R.id.edit_text_view_id);
        mSpinner = findViewById(R.id.expire_date_options_id);

        ImageButton mSnapExpireDateButton = findViewById(R.id.snap_expire_date_btn_id);
        mSnapExpireDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearTextView();
                dispatchTakePictureIntent(ImageCaptureActionType.EXPIRE_DATE);
            }
        });
    }

    private void dispatchTakePictureIntent(ImageCaptureActionType imageCaptureActionType) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageCaptureActionType == ImageCaptureActionType.EXPIRE_DATE) {
            if (takePictureIntent.resolveActivity(Objects.requireNonNull(this).getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_EXPIRE_DATE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE_EXPIRE_DATE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = null;
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }
            mImageView.setImageBitmap(imageBitmap);

            runTextRecognition(imageBitmap);
        }
    }

    private void runTextRecognition(Bitmap imageBitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processTextRecognitionResult(firebaseVisionText);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                NotificationLogger.showToast(getApplicationContext(), "Error: Could not run the text recognition", MessageType.ERROR);
            }
        });
    }

    private String parseStringToDate(String dateAsString) {

        LocalDate localDate;

        for (String formatString : formatStrings) {
            try {

                DateTimeFormatter formatter;

                // We check if the date format pattern contains days
                // If true, we have to define a default value for the day because LocalDate needs the day, month and year to be built.
                if (!formatString.contains("d")) {
                    formatter = new DateTimeFormatterBuilder()
                            .appendPattern(formatString)
                            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                            .toFormatter();

                } else {
                    formatter = DateTimeFormatter.ofPattern(formatString);
                }

                localDate = LocalDate.parse(dateAsString, formatter);

                NotificationLogger.showToast(getApplicationContext(), "Successfully recognition of the expire date", MessageType.SUCCESS);

                return localDate.format(
                        DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy"));

            } catch (DateTimeParseException e) {
                Log.e("Expire date parsing", e.getParsedString());
            }
        }

        return null;
    }

    private void processTextRecognitionResult(FirebaseVisionText text) {
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();

        if (blocks.isEmpty()) {
            NotificationLogger.showToast(getApplicationContext(), "No text found", MessageType.ERROR);
            return;
        }

        List<String> dateOptions = new ArrayList<>();

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {

                String date = this.parseStringToDate(lines.get(j).getText());

                if (date != null && !dateOptions.contains(date)) {
                    dateOptions.add(date);
                }

                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {

                    date = this.parseStringToDate(elements.get(k).getText());

                    if (date != null && !dateOptions.contains(date)) {
                        dateOptions.add(date);
                    }
                }
            }
        }

        if (!dateOptions.isEmpty()) {
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(this), android.R.layout.simple_spinner_item, dateOptions);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            mSpinner.setAdapter(adapter);

            if (dateOptions.size() == 1) {
                mTextView.setText(dateOptions.get(0));
            }
        } else {
            NotificationLogger.showToast(getApplicationContext(), "Unsuccessful recognition of the expire date", MessageType.ERROR);
        }
    }

    private void clearTextView() {
        mTextView.setText("");

        if (mSpinner != null) {
            mSpinner.setAdapter(null);
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
}
