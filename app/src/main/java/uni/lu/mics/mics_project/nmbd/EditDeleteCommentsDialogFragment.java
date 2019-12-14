package uni.lu.mics.mics_project.nmbd;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

public class EditDeleteCommentsDialogFragment extends DialogFragment {
    private static final String TAG = "EditDeleteCommentsDialo";


    public interface EditDeleteListener {
         void onDialogPositiveClick(DialogFragment dialog);
         void onDialogNegativeClick(DialogFragment dialog);
         void onDialogNeutralClick(DialogFragment dialog);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder  builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Click Edit or Delete to perfom action on comment")
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Edit is clicked");
                        listener.onDialogPositiveClick(EditDeleteCommentsDialogFragment.this);
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Delete is Clicked");
                        listener.onDialogNegativeClick(EditDeleteCommentsDialogFragment.this);
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: Cancel is Clicked");
                        listener.onDialogNeutralClick(EditDeleteCommentsDialogFragment.this);
                    }
                });

        return builder.create();

    }

    EditDeleteListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (EditDeleteListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(this.toString()
                    + " must implement NoticeDialogListener");
        }
    }

}
