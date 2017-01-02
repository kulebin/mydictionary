package lab.kulebin.mydictionary.app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.http.Api;
import lab.kulebin.mydictionary.http.HttpClient;
import lab.kulebin.mydictionary.http.IHttpClient;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.thread.ITask;
import lab.kulebin.mydictionary.thread.OnResultCallback;
import lab.kulebin.mydictionary.thread.ProgressCallback;
import lab.kulebin.mydictionary.thread.ThreadManager;
import lab.kulebin.mydictionary.utils.UriBuilder;

import static android.app.Activity.RESULT_OK;

public class EntryFragment extends Fragment {

    private static final String TAG = EntryFragment.class.getSimpleName();
    public static final int WIDTH = 500;
    public static final int HEIGHT = 500;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;
    private long mEntryId;
    private String mPhotoAbsolutePath;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {

        final View rootView = inflater.inflate(
                R.layout.item_pager_entry, container, false);
        final Bundle args = getArguments();
        mEntryId = args.getLong(Constants.EXTRA_ENTRY_ID);

        mImageView = (ImageView) rootView.findViewById(R.id.pager_item_image);
        final String imageUrl = args.getString(Constants.EXTRA_ENTRY_IMAGE_URL);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .override(WIDTH, HEIGHT)
                    //.error(R.drawable.image_default_entry_192dp)
                    .into(mImageView);
        } else {
            final Drawable entryImageDrawable = VectorDrawableCompat.create(getContext().getResources(), R.drawable.image_default_entry_192dp, null);
            mImageView.setImageDrawable(entryImageDrawable);
        }

        ((TextView) rootView.findViewById(R.id.pager_item_value)).setText(
                args.getString(Constants.EXTRA_ENTRY_VALUE));

        ((TextView) rootView.findViewById(R.id.pager_item_translation)).setText(
                args.getString(Constants.EXTRA_ENTRY_TRANSLATION));
        ((TextView) rootView.findViewById(R.id.pager_item_context_usage)).setText(
                args.getString(Constants.EXTRA_ENTRY_USAGE_CONTEXT));

        final ImageView deleteImageView = (ImageView) rootView.findViewById(R.id.pager_delete_icon);
        deleteImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle(getString(R.string.alert_title_confirm_entry_deletion));
                alertDialogBuilder
                        .setMessage(getString(R.string.alert_body_confirm_entry_deletion))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.alert_positive_button), new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                deleteEntryTask(mEntryId);
                            }
                        })
                        .setNegativeButton(getString(R.string.alert_negative_button), new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        final ImageView editImageView = (ImageView) rootView.findViewById(R.id.pager_edit_icon);
        editImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(getContext(), EditActivity.class);
                intent.putExtra(Constants.EXTRA_ENTRY_ID, mEntryId);
                intent.putExtra(Constants.EXTRA_EDIT_ACTIVITY_MODE, EditActivity.EditActivityMode.EDIT);
                startActivity(intent);
            }
        });

        final ImageView photoCameraImageView = (ImageView) rootView.findViewById(R.id.pager_camera_icon);
        final PackageManager pm = getContext().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            photoCameraImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View pView) {
                    final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(pm) != null) {
                        final Uri uir = createImageFile();
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uir);
                        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            });
        } else {
            photoCameraImageView.setVisibility(View.GONE);
        }

        return rootView;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            uploadPhotoToFirebase();
        }
    }

    private Uri createImageFile() {
        final File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        final String imageFileName = "JPEG_" + timeStamp + ".jpg";
        final File photoFile = new File(dir, imageFileName);
        final Uri contentUri = Uri.fromFile(photoFile);
        mPhotoAbsolutePath = photoFile.getAbsolutePath();
        return contentUri;
    }

    private void uploadPhotoToFirebase() {
        final Uri file = Uri.fromFile(new File(mPhotoAbsolutePath));
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference riversRef = storageRef.child("images/" + file.getLastPathSegment());
        final UploadTask uploadTask = riversRef.putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull final Exception exception) {
                Snackbar.make(getView(), "Error: Photo is not uploaded!", Snackbar.LENGTH_LONG)
                        .setAction("Ok", null).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                Snackbar.make(getView(), "Success! Photo has been uploaded!", Snackbar.LENGTH_LONG)
                        .setAction("Ok", null).show();
                final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(Entry.IMAGE_URL, downloadUrl.toString());
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                myRef.child("users")
                        .child(userUid)
                        .child(Api.ENTRIES)
                        .child(String.valueOf(mEntryId))
                        .updateChildren(childUpdates, new DatabaseReference.CompletionListener() {

                            @Override
                            public void onComplete(DatabaseError pDatabaseError, DatabaseReference pDatabaseReference) {
                                if (pDatabaseError == null && pDatabaseReference != null) {
                                    final ContentValues values = new ContentValues();
                                    values.put(Entry.IMAGE_URL, downloadUrl.toString());
                                    getContext().getContentResolver().update(
                                            UriBuilder.getTableUri(Entry.class),
                                            values,
                                            Entry.ID + "=?",
                                            new String[]{String.valueOf(mEntryId)}
                                    );
                                }
                            }
                        });
            }
        });
    }

    private String savePhotoToInternalStorage(final Bitmap bitmapImage) {
        final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final ContextWrapper cw = new ContextWrapper(getContext());
        final File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        final String imageFileName = userUid + "_" + System.currentTimeMillis() + ".png";
        final File imagePath = new File(directory, imageFileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 25, fos);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return imagePath.getAbsolutePath();
    }

    private void deleteEntryTask(final Long pEntryId) {
        //noinspection WrongConstant
        final ThreadManager threadManager = (ThreadManager) getActivity().getApplication().getSystemService(ThreadManager.APP_SERVICE_KEY);
        threadManager.execute(
                new ITask<Long, Void, Void>() {

                    @Override
                    public Void perform(final Long pEntryId, final ProgressCallback<Void> progressCallback) throws Exception {
                        final SharedPreferences shp = getActivity().getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
                        final String token = shp.getString(Constants.APP_PREFERENCES_USER_TOKEN, null);
                        final Uri uri = Uri.parse(Api.getBaseUrl()).buildUpon()
                                .appendPath(Api.ENTRIES)
                                .appendPath(pEntryId + Api.JSON_FORMAT)
                                .appendQueryParameter(Api.PARAM_AUTH, token)
                                .build();
                        final IHttpClient httpClient = new HttpClient();
                        try {
                            if (httpClient.delete(uri.toString()).equals(HttpClient.DELETE_RESPONSE_OK)) {
                                getContext().getContentResolver().delete(
                                        UriBuilder.getTableUri(Entry.class),
                                        Entry.ID + "=?",
                                        new String[]{String.valueOf(pEntryId)});
                            } else {
                                Toast.makeText(getContext(),
                                        R.string.ERROR_CONNECTION_DELETE, Toast.LENGTH_SHORT).show();
                            }
                        } catch (final Exception e) {
                            Log.v(TAG, getString(R.string.ERROR_DELETE_REQUEST));
                        }
                        return null;
                    }
                },
                pEntryId,
                new OnResultCallback<Void, Void>() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(final Void pVoid) {
                    }

                    @Override
                    public void onError(final Exception e) {
                        Toast.makeText(getContext(),
                                R.string.ERROR_DELETE_ENTRY, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {

                    }
                });
    }
}
