package com.example.samuilmihaylov.eatorthrow.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.example.samuilmihaylov.eatorthrow.R;
import com.example.samuilmihaylov.eatorthrow.enums.ImageCaptureActionType;
import com.example.samuilmihaylov.eatorthrow.enums.MessageType;
import com.example.samuilmihaylov.eatorthrow.models.Product;
import com.example.samuilmihaylov.eatorthrow.notifications.NotificationPublisher;
import com.example.samuilmihaylov.eatorthrow.utils.NotificationLogger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AddProductActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE_PRODUCT = 1;
    private static final int REQUEST_IMAGE_CAPTURE_EXPIRE_DATE = 2;

    private static final String BASE_DATE_FORMAT = "dd/MM/yyyy";

    private static final List<String> formatStrings = Arrays.asList(
            BASE_DATE_FORMAT,
            "MM/yy",
            "MM/dd/yy",
            "MM-dd-yy",
            "M yyyy",
            "MM yyyy",
            "MM/yyyy",
            "MM-dd-yyyy",
            "dd-MM-yy",
            "dd.MM.yy",
            "dd-MM-yyyy",
            "dd.MM.yyyy",
            "dd MM yyyy",
            "ddMMyyyy");

    private Date mPurchaseDate;
    private Date mExpiryDate;
    private String mPurchaseDateAsString;
    private String mExpiryDateAsString;
    private ImageView mProductPhotoImage;
    private TextView mSelectedAlternativeDateTextView;
    private EditText mPurchaseDateEditTextView;
    private EditText mExpiryDateEditTextView;
    private Spinner mRecognizedDateOptionsSpinner;
    private Spinner mProductCategoryOptionsSpinner;
    private Bitmap mProductImageBitmap;
    private String mProductName;
    private String mProductCategory;
    private String mAdditionalNote;
    private EditText mEditProductNameView;
    private EditText mEditAdditionalNoteView;
    private DateTimeFormatter mFormatter;
    private SimpleDateFormat mSimpleDateFormat;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private Product mProductToEdit;
    private DatePickerDialog mExpiryDatePickerDialog;

    private Calendar mExpiryDateCalendar;
    private Calendar mPurchaseDateCalendar;

    public AddProductActivity() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);

        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        Toolbar myToolbar = findViewById(R.id.action_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView productNameLabelView = findViewById(R.id.edit_product_name_text_label_id);
        this.setColor(productNameLabelView, productNameLabelView.getText().toString(), "*");

        TextView productCategoryLabelView = findViewById(R.id.product_category_text_label);
        this.setColor(productCategoryLabelView, productCategoryLabelView.getText().toString(), "*");

        TextView purchaseDateLabelView = findViewById(R.id.purchase_date_label);
        this.setColor(purchaseDateLabelView, purchaseDateLabelView.getText().toString(), "*");

        TextView expiryDateLabelView = findViewById(R.id.expiry_date_label);
        this.setColor(expiryDateLabelView, expiryDateLabelView.getText().toString(), "*");

        mEditProductNameView = findViewById(R.id.edit_product_name_text_id);
        mEditAdditionalNoteView = findViewById(R.id.product_note_text_id);
        mProductPhotoImage = findViewById(R.id.product_photo_image_id);
        mSelectedAlternativeDateTextView = findViewById(R.id.to_date_alternative_id);
        mRecognizedDateOptionsSpinner = findViewById(R.id.expire_date_options_id);
        mProductCategoryOptionsSpinner = findViewById(R.id.product_categories_options_id);
        mPurchaseDateEditTextView = findViewById(R.id.from_date_id);
        mExpiryDateEditTextView = findViewById(R.id.to_date_id);

        mPurchaseDateCalendar = Calendar.getInstance();
        mExpiryDateCalendar = Calendar.getInstance();

        mFormatter = new DateTimeFormatterBuilder().appendPattern(BASE_DATE_FORMAT).toFormatter();
        mSimpleDateFormat = new SimpleDateFormat(BASE_DATE_FORMAT);

        this.initializeProductIfExistsInIntent();
        this.initializeProductCategories();

        ImageButton snapProductButton = findViewById(R.id.snap_product_btn_id);
        snapProductButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(ImageCaptureActionType.REQUEST_IMAGE_CAPTURE_PRODUCT);
            }
        });

        ImageButton snapExpireDateButton = findViewById(R.id.snap_expire_date_btn_id);
        snapExpireDateButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent(ImageCaptureActionType.REQUEST_IMAGE_CAPTURE_EXPIRE_DATE);
            }
        });

        final DatePickerDialog.OnDateSetListener purchaseDate = new DatePickerDialog.OnDateSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                mPurchaseDateCalendar.set(Calendar.YEAR, year);
                mPurchaseDateCalendar.set(Calendar.MONTH, monthOfYear);
                mPurchaseDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                mPurchaseDate = mPurchaseDateCalendar.getTime();
                mPurchaseDateAsString = LocalDate.of(year, monthOfYear + 1, dayOfMonth).format(mFormatter);

                mPurchaseDateEditTextView.setText(mPurchaseDateAsString);
            }
        };

        mPurchaseDateEditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddProductActivity.this, purchaseDate,
                        mPurchaseDateCalendar.get(Calendar.YEAR),
                        mPurchaseDateCalendar.get(Calendar.MONTH),
                        mPurchaseDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final DatePickerDialog.OnDateSetListener expiryDate = new DatePickerDialog.OnDateSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                mExpiryDateCalendar.set(Calendar.YEAR, year);
                mExpiryDateCalendar.set(Calendar.MONTH, monthOfYear);
                mExpiryDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                mExpiryDate = mExpiryDateCalendar.getTime();
                mExpiryDateAsString = LocalDate.of(year, monthOfYear + 1, dayOfMonth).format(mFormatter);

                mExpiryDateEditTextView.setText(mExpiryDateAsString);
            }
        };

        mExpiryDateEditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpiryDatePickerDialog = new DatePickerDialog(AddProductActivity.this, expiryDate,
                        mExpiryDateCalendar.get(Calendar.YEAR),
                        mExpiryDateCalendar.get(Calendar.MONTH),
                        mExpiryDateCalendar.get(Calendar.DAY_OF_MONTH));

                mExpiryDatePickerDialog.getDatePicker().setMinDate(mExpiryDateCalendar.getTimeInMillis());
                mExpiryDatePickerDialog.show();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initializeProductIfExistsInIntent() {
        Product product = (Product) getIntent().getSerializableExtra("product");

        if (product != null) {

            mProductToEdit = product;

            mEditProductNameView.setText(product.getProductName());
            mEditAdditionalNoteView.setText(product.getAdditionalNote());
            mPurchaseDateEditTextView.setText(product.getPurchaseDate());
            mExpiryDateEditTextView.setText(product.getExpiryDate());

            mProductName = product.getProductName();
            mPurchaseDateAsString = product.getPurchaseDate();
            mExpiryDateAsString = product.getExpiryDate();
            mAdditionalNote = product.getAdditionalNote();


            try {
                mPurchaseDate = mSimpleDateFormat.parse(product.getPurchaseDate());
                mExpiryDate = mSimpleDateFormat.parse(product.getExpiryDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String[] expiryDateSplit = product.getExpiryDate().split("/");
            int expiryDate = Integer.parseInt(expiryDateSplit[0]);
            int expiryMonth = Integer.parseInt(expiryDateSplit[1]);
            int expiryYear = Integer.parseInt(expiryDateSplit[2]);
            mExpiryDateCalendar.set(expiryYear, expiryMonth, expiryDate);

            String[] purchaseDateSplit = product.getPurchaseDate().split("/");
            int purchaseDate = Integer.parseInt(purchaseDateSplit[0]);
            int purchaseMonth = Integer.parseInt(purchaseDateSplit[1]);
            int purchaseYear = Integer.parseInt(purchaseDateSplit[2]);
            mPurchaseDateCalendar.set(purchaseYear, purchaseMonth, purchaseDate);

            if (product.getProductImageUrl() != null) {
                final StorageReference storageRef = mStorage.getReference();
                storageRef.child(product.getProductImageUrl()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageURL = uri.toString();
                        Glide.with(getApplicationContext()).load(imageURL).into(mProductPhotoImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("Product Image load", "Could not load the product image from firebase storage");
                    }
                });
            }
        } else {
            clearTextView();
            mPurchaseDate = mPurchaseDateCalendar.getTime();
            mPurchaseDateAsString = LocalDate.now().format(mFormatter);
            mPurchaseDateEditTextView.setText(mPurchaseDateAsString);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        menu.findItem(R.id.action_add_new_product).setVisible(false);

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_save_product:

                boolean isValid = true;

                if (TextUtils.isEmpty(mEditProductNameView.getText())) {
                    mEditProductNameView.setError("Product name is required");
                    isValid = false;
                }
                if (TextUtils.isEmpty(mPurchaseDateEditTextView.getText())) {
                    mPurchaseDateEditTextView.setError("Purchase date is required");
                    isValid = false;
                }
                if (TextUtils.isEmpty(mExpiryDateEditTextView.getText())) {
                    mExpiryDateEditTextView.setError("Expiry date is required");
                    isValid = false;
                }

                if (isValid) {

                    mProductName = mEditProductNameView.getText().toString().trim();
                    mProductCategory = mProductCategoryOptionsSpinner.getSelectedItem().toString().trim();
                    mAdditionalNote = mEditAdditionalNoteView.getText().toString().trim();

                    this.saveProduct();
                }

                return true;

            case R.id.log_out:
                GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
                FirebaseAuth.getInstance().signOut();
                mGoogleSignInClient.signOut();

                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void setColor(TextView view, String fulltext, String subtext) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE);
        Spannable str = (Spannable) view.getText();
        int i = fulltext.indexOf(subtext);
        str.setSpan(new ForegroundColorSpan(Color.RED), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void saveProduct() {

        // TODO: Check if alternative date is chosen

        final DatabaseReference databaseReference = mDatabase
                .getReference("products")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance()
                        .getUid()));

        final String key = mProductToEdit == null ? databaseReference.push().getKey() : mProductToEdit.getId();

        if (key != null) {

            if (mProductImageBitmap != null) {
                final StorageReference storageRef = mStorage.getReference("products").child(FirebaseAuth.getInstance().getUid()).child(key + ".jpg");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                mProductImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] data = byteArrayOutputStream.toByteArray();

                UploadTask uploadTask = storageRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // TODO: Handle this on failure case
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        final Product product = new Product(key, mProductName, mProductCategory, mPurchaseDateAsString, mExpiryDateAsString, mAdditionalNote, storageRef.getPath());

                        databaseReference.child(key).setValue(product, new DatabaseReference.CompletionListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                if (databaseError != null) {
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.add_product_layout), "Product could not be saved", Snackbar.LENGTH_SHORT);
                                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                                    snackbar.show();
                                } else {
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.add_product_layout), "Product saved", Snackbar.LENGTH_SHORT);
                                    snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

                                    long delay = mExpiryDate.getTime() - mPurchaseDate.getTime();
                                    scheduleNotification(getNotification(product.getProductName()), delay);

                                    snackbar.show();
                                }
                            }
                        });
                    }
                });
            } else {

                final Product product = new Product(key, mProductName, mProductCategory, mPurchaseDateAsString, mExpiryDateAsString, mAdditionalNote, null);

                databaseReference.child(key).setValue(product, new DatabaseReference.CompletionListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        if (databaseError != null) {
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.add_product_layout), "Product could not be saved", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                            snackbar.show();
                        } else {
                            Snackbar snackbar = Snackbar.make(findViewById(R.id.add_product_layout), "Product saved", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

                            long delay = mExpiryDate.getTime() - mPurchaseDate.getTime();
                            scheduleNotification(getNotification(product.getProductName()), delay);

                            snackbar.show();
                        }
                    }
                });
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void dispatchTakePictureIntent(ImageCaptureActionType imageCaptureActionType) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageCaptureActionType == ImageCaptureActionType.REQUEST_IMAGE_CAPTURE_PRODUCT) {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_PRODUCT);
            }

        } else if (imageCaptureActionType == ImageCaptureActionType.REQUEST_IMAGE_CAPTURE_EXPIRE_DATE) {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_EXPIRE_DATE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE_PRODUCT && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            mProductImageBitmap = null;
            if (extras != null) {
                mProductImageBitmap = (Bitmap) extras.get("data");
            }

            mProductPhotoImage.setImageBitmap(mProductImageBitmap);
            mProductPhotoImage.setImageAlpha(255);

        } else if (requestCode == REQUEST_IMAGE_CAPTURE_EXPIRE_DATE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = null;
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }

            runTextRecognition(imageBitmap);
        }
    }

    private void runTextRecognition(Bitmap imageBitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        firebaseVisionTextRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String parseStringToDate(String dateAsString) {

        LocalDate localDate;

        for (String formatString : formatStrings) {
            try {
                DateTimeFormatter formatter;
                // We check if the date format pattern contains days
                // If not, we have to define a default value for the day because LocalDate needs the day, month and year to be built.
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
                                BASE_DATE_FORMAT));

            } catch (DateTimeParseException e) {
                Log.e("Expire date parsing", e.getParsedString());
            }
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void processTextRecognitionResult(FirebaseVisionText text) {
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();

        if (blocks.isEmpty()) {
            NotificationLogger.showToast(getApplicationContext(), "No expiry date found", MessageType.ERROR);
            return;
        }

        List<String> dateOptions = new ArrayList<>();

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {

                String date = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    date = this.parseStringToDate(lines.get(j).getText());
                }

                if (date != null && !dateOptions.contains(date)) {
                    dateOptions.add(date);
                    setRecognizedExpiryDate(date);
                }

                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        date = this.parseStringToDate(elements.get(k).getText());
                    }

                    if (date != null && !dateOptions.contains(date)) {
                        dateOptions.add(date);
                        setRecognizedExpiryDate(date);
                    }
                }
            }
        }

        if (dateOptions.size() > 1) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dateOptions);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mRecognizedDateOptionsSpinner.setAdapter(adapter);
            mSelectedAlternativeDateTextView.setText(dateOptions.get(0));
        }

        if (dateOptions.isEmpty()) {
            NotificationLogger.showToast(getApplicationContext(), "Unsuccessful recognition of the expire date", MessageType.ERROR);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setRecognizedExpiryDate(String date) {
        mExpiryDateEditTextView.setText(date);
        mExpiryDateAsString = date;

        try {
            mExpiryDate = mSimpleDateFormat.parse(date);
        } catch (ParseException e) {
            Log.e("Expiry date parsing", "Could not parse the given string to date");
        }

        String[] split = date.split("/");
        int day = Integer.parseInt(split[0]);
        int month = Integer.parseInt(split[1]);
        int year = Integer.parseInt(split[2]);

        mExpiryDateCalendar.set(year, month, day);
    }

    private void initializeProductCategories() {

        List<String> productCategories = new ArrayList<>(
                Arrays.asList(
                        "Dairy products",
                        "Fats and oils",
                        "Fruits and vegetables",
                        "Bakery wares",
                        "Meat and meat products",
                        "Fish and fish products",
                        "Eggs and egg products",
                        "Salts, spices, soups, sauces, salads, protein products",
                        "Beverages"
                ));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productCategories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProductCategoryOptionsSpinner.setAdapter(adapter);
    }

    private void scheduleNotification(Notification notification, long delay) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = System.currentTimeMillis() + delay;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String productName) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Product " + productName + " is expiring today");
        builder.setContentText("Type to suggest fast recipes for this product");
        builder.setSmallIcon(R.drawable.ic_groceries_placeholder);

        Intent notifyIntent = new Intent(Intent.ACTION_WEB_SEARCH);
        notifyIntent.putExtra(SearchManager.QUERY, "cooking recipe with " + productName);

        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(notifyPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            String channelId = "YOUR_CHANNEL_ID";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        return builder.build();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void clearTextView() {
        ViewGroup group = findViewById(R.id.add_product_layout);
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText) view).setText("");
            }
        }

        mPurchaseDateAsString = LocalDate.now().format(mFormatter);
        mPurchaseDateEditTextView.setText(mPurchaseDateAsString);
    }

}
