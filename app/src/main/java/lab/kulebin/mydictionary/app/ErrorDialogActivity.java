package lab.kulebin.mydictionary.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import lab.kulebin.mydictionary.Constants;
import lab.kulebin.mydictionary.R;

public class ErrorDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final String dialogTitle = intent.getStringExtra(Constants.EXTRA_ERROR_DIALOG_TITLE);
        final String dialogMessage = intent.getStringExtra(Constants.EXTRA_ERROR_DIALOG_TEXT);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setCancelable(true)
                .setPositiveButton(getString(R.string.BUTTON_DIALOG_POSITIVE), new DialogInterface.OnClickListener() {

                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.dismiss();
                        finish();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
