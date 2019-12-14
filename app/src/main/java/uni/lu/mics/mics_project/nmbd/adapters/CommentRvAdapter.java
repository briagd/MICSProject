package uni.lu.mics.mics_project.nmbd.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import uni.lu.mics.mics_project.nmbd.R;


import static uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils.displayUserCirclePicID;

public class CommentRvAdapter extends RecyclerView.Adapter<CommentRvAdapter.ViewHolder> {
    private static final String TAG = "CommentRvAdapter";

    private List<String> commenterNames;
    private List<String> UrlPics;
    private List<String> commentTexts;
    public List<String> commentdates;
    private List<String> commenterIds;
    private View.OnClickListener mOnClickListener;
    private Context mContext;

    public CommentRvAdapter(Context mContext, List<String> commenterNames, List<String> urlPics, List<String> commentTexts, List<String> commentDates, List<String> commenterIds ) {
        this.commenterNames = commenterNames;
        this.UrlPics = urlPics;
        this.commentTexts = commentTexts;
        this.commentdates = commentDates;
        this.commenterIds = commenterIds;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_commentlist_item, parent, false);
        view.setOnClickListener(mOnClickListener);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");

        displayUserCirclePicID(mContext, commenterIds.get(position), holder.commenterPic);


        holder.commenterName.setText(commenterNames.get(position));
        holder.commentText.setText(commentTexts.get(position));
        holder.date.setText(commentdates.get(position));

    }

    @Override
    public int getItemCount() {
        return commenterNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView commenterPic;
        TextView commenterName;
        RelativeLayout parentLayout;
        TextView commentText;
        TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            commenterPic = itemView.findViewById(R.id.commenterPic);
            commenterName = itemView.findViewById(R.id.commenterName);
            parentLayout = itemView.findViewById(R.id.parentRelativeLayout);
            commentText = itemView.findViewById(R.id.commentText);
            date = itemView.findViewById(R.id.commentTimestamp);

        }
    }
}
