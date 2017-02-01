package lab.kulebin.mydictionary.app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.R;
import lab.kulebin.mydictionary.db.DbHelper;
import lab.kulebin.mydictionary.http.Api;
import lab.kulebin.mydictionary.http.HttpClient;
import lab.kulebin.mydictionary.http.IHttpClient;
import lab.kulebin.mydictionary.http.UrlBuilder;
import lab.kulebin.mydictionary.model.Entry;
import lab.kulebin.mydictionary.thread.ITask;
import lab.kulebin.mydictionary.thread.OnResultCallback;
import lab.kulebin.mydictionary.thread.ProgressCallback;
import lab.kulebin.mydictionary.thread.ThreadManager;
import lab.kulebin.mydictionary.utils.NameUtils;
import lab.kulebin.mydictionary.utils.UriBuilder;

import static android.app.Activity.RESULT_OK;

public class EntryFragment extends Fragment {

    private static final String TAG = EntryFragment.class.getSimpleName();
    public static final int WIDTH = 500;
    public static final int HEIGHT = 500;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MAX_UPLOAD_PHOTO_SIZE = 2 * 1000 * 1000;

    private long mEntryId;
    private File mPhotoFile;
    private ThreadManager mThreadManager;
    private ProgressBar mProgressBar;

    @SuppressWarnings("WrongConstant")
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {

        final View rootView = inflater.inflate(
                R.layout.item_pager_entry, container, false);
        final Bundle args = getArguments();
        mEntryId = args.getLong(Constants.EXTRA_ENTRY_ID);
        mThreadManager = (ThreadManager) getActivity().getApplication().getSystemService(ThreadManager.APP_SERVICE_KEY);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        final ImageView imageView = (ImageView) rootView.findViewById(R.id.pager_item_image);
        final String imageUrl = args.getString(Constants.EXTRA_ENTRY_IMAGE_URL);
        Glide.with(this)
                .load(imageUrl)
                .override(WIDTH, HEIGHT)
                .error(VectorDrawableCompat.create(getContext().getResources(), R.drawable.image_default_entry_192dp, null))
                .into(imageView);

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
                alertDialogBuilder.setTitle(getString(R.string.TITLE_DIALOG_CONFIRM_ENTRY_DELETION));
                alertDialogBuilder
                        .setMessage(getString(R.string.TEXT_DIALOG_CONFIRM_ENTRY_DELETION))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.BUTTON_DIALOG_POSITIVE), new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                deleteEntryTask(mEntryId);
                            }
                        })
                        .setNegativeButton(getString(R.string.BUTTON_DIALOG_NEGATIVE), new DialogInterface.OnClickListener() {

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
                        final File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        mPhotoFile = new File(dir, NameUtils.createImageFileName());
                        final Uri uir = Uri.fromFile(mPhotoFile);
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

    private void uploadPhotoToFirebase() {
        mThreadManager.execute(new ITask<Void, Void, byte[]>() {

                                   @Override
                                   public byte[] perform(final Void pVoid, final ProgressCallback<Void> progressCallback) throws FileNotFoundException {

                                       final BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                       Bitmap bitmap = BitmapFactory.decodeFile(mPhotoFile.getAbsolutePath(), bmOptions);

                                       while (bitmap.getByteCount() > MAX_UPLOAD_PHOTO_SIZE) {
                                           final int width = bitmap.getWidth();
                                           final int height = bitmap.getHeight();

                                           final int halfWidth = width / 2;
                                           final int halfHeight = height / 2;

                                           bitmap = Bitmap.createScaledBitmap(bitmap, halfWidth,
                                                   halfHeight, false);
                                       }

                                       final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                       bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                                       if (!mPhotoFile.delete()) {
                                           throw new FileNotFoundException();
                                       }

                                       return baos.toByteArray();
                                   }
                               },
                null,
                new OnResultCallback<byte[], Void>() {

                    @Override
                    public void onStart() {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onSuccess(final byte[] pBytes) {
                        final StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        final StorageReference riversRef = storageRef.child(Api.IMAGES_FOLDER + mPhotoFile.getName());

                        final UploadTask uploadTask = riversRef.putBytes(pBytes);
                        uploadTask.addOnFailureListener(new OnFailureListener() {

                            @Override
                            public void onFailure(@NonNull final Exception exception) {
                                if (getView() != null) {
                                    Snackbar.make(getView(), R.string.ERROR_FILE_NOT_UPLOADED, Snackbar.LENGTH_LONG)
                                            .setAction(R.string.BUTTON_OK, null).show();
                                }
                                if (mProgressBar.isShown()) {
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                                if (taskSnapshot.getDownloadUrl() != null) {
                                    final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    final Map<String, Object> childUpdates = new HashMap<>();
                                    childUpdates.put(Entry.IMAGE_URL, downloadUrl.toString());
                                    final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                                    myRef.child(Api.USERS)
                                            .child(userUid)
                                            .child(DbHelper.getTableName(Entry.class))
                                            .child(String.valueOf(mEntryId))
                                            .updateChildren(childUpdates, new DatabaseReference.CompletionListener() {

                                                @Override
                                                public void onComplete(final DatabaseError pDatabaseError, final DatabaseReference pDatabaseReference) {
                                                    if (pDatabaseError == null && pDatabaseReference != null) {
                                                        updateEntryUriTask(downloadUrl.toString());
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                    }

                    @Override
                    public void onError(final Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        if (getView() != null) {
                            Snackbar.make(getView(), R.string.ERROR_FILE_NOT_FOUND, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.BUTTON_OK, null).show();
                        }
                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {

                    }
                });
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void updateEntryUriTask(final String pDownloadUrl) {
        mThreadManager.execute(new ITask<String, Void, Void>() {

                                   @Override
                                   public Void perform(final String pS, final ProgressCallback<Void> progressCallback) throws Exception {
                                       final ContentValues values = new ContentValues();
                                       values.put(Entry.IMAGE_URL, pDownloadUrl);
                                       getContext().getContentResolver().update(
                                               UriBuilder.getTableUri(Entry.class),
                                               values,
                                               Entry.ID + "=?",
                                               new String[]{String.valueOf(mEntryId)}
                                       );
                                       return null;
                                   }
                               },
                pDownloadUrl,
                new OnResultCallback<Void, Void>() {

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(final Void pVoid) {
                        if (mProgressBar.isShown()) {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(final Exception e) {
                        if (mProgressBar.isShown()) {
                            mProgressBar.setVisibility(View.GONE);
                        }
                        if (getView() != null) {
                            Snackbar.make(getView(), R.string.ERROR_FILE_NOT_UPLOADED, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.BUTTON_OK, null).show();
                        }

                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {

                    }
                });
    }

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void deleteEntryTask(final Long pEntryId) {
        mThreadManager.execute(
                new ITask<Long, Void, Void>() {

                    @Override
                    public Void perform(final Long pEntryId, final ProgressCallback<Void> progressCallback) throws Exception {
                        final IHttpClient httpClient = new HttpClient();
                        final String url = UrlBuilder.getPersonalisedUrl(
                                new String[]{DbHelper.getTableName(Entry.class), String.valueOf(pEntryId)},
                                null
                        );

                        try {
                            if (httpClient.delete(url).equals(HttpClient.DELETE_RESPONSE_OK)) {
                                getContext().getContentResolver().delete(
                                        UriBuilder.getTableUri(Entry.class),
                                        Entry.ID + "=?",
                                        new String[]{String.valueOf(pEntryId)});
                            } else {
                                Toast.makeText(getContext(),
                                        R.string.ERROR_NO_CONNECTION, Toast.LENGTH_SHORT).show();
                            }
                        } catch (final Exception e) {
                            Toast.makeText(getContext(),
                                    R.string.ERROR_ENTRY_NOT_DELETED,
                                    Toast.LENGTH_SHORT).show();
                        }
                        return null;
                    }
                },
                pEntryId,
                new OnResultCallback<Void, Void>() {

                    @Override
                    public void onStart() {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onSuccess(final Void pVoid) {
                        mProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(final Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                                R.string.ERROR_ENTRY_NOT_DELETED, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {

                    }
                });
    }
}
