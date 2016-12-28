package lab.kulebin.mydictionary.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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

public class EntryFragment extends Fragment {

    private static final String TAG = EntryFragment.class.getSimpleName();
    public static final int WIDTH = 500;
    public static final int HEIGHT = 500;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {

        final View rootView = inflater.inflate(
                R.layout.item_pager_entry, container, false);
        final Bundle args = getArguments();
        final long entryId = args.getLong(Constants.EXTRA_ENTRY_ID);

        final ImageView imageView = (ImageView) rootView.findViewById(R.id.pager_item_image);
        final String imageUrl = args.getString(Constants.EXTRA_ENTRY_IMAGE_URL);
        Glide.with(this)
                .load(imageUrl)
                .override(WIDTH, HEIGHT)
                .error(R.drawable.image_default_entry_192dp)
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
                alertDialogBuilder.setTitle(getString(R.string.alert_title_confirm_entry_deletion));
                alertDialogBuilder
                        .setMessage(getString(R.string.alert_body_confirm_entry_deletion))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.alert_positive_button), new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                deleteEntryTask(entryId);
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
                intent.putExtra(Constants.EXTRA_ENTRY_ID, entryId);
                intent.putExtra(Constants.EXTRA_EDIT_ACTIVITY_MODE, EditActivity.EditActivityMode.EDIT);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void deleteEntryTask(final Long pEntryId) {
        //noinspection WrongConstant
        final ThreadManager threadManager = (ThreadManager)getActivity().getApplication().getSystemService(ThreadManager.APP_SERVICE_KEY);
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
