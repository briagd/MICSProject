package uni.lu.mics.mics_project.nmbd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.LinkedList;

import uni.lu.mics.mics_project.nmbd.R;
import uni.lu.mics.mics_project.nmbd.app.service.ImageViewUtils;

public class FriendRequestListAdapter extends RecyclerView.Adapter<FriendRequestListAdapter.FriendRequestViewHolder>{

    private final LinkedList<String> mRequestNameList;
    private final LinkedList<String> mRequestIDList;
    private final HashMap<String, StorageReference> stRefList;
    private LayoutInflater mInflater;
    private AdapterDoubleCallBack mlistener;
    private final Context context;


    public FriendRequestListAdapter(Context context, LinkedList<String> nameList, LinkedList<String> idList,
                                    HashMap<String, StorageReference> stRefList, AdapterDoubleCallBack listener) {
        mInflater = LayoutInflater.from(context);
        this.mRequestNameList = nameList;
        this.mRequestIDList = idList;
        this.stRefList = stRefList;
        this.mlistener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.friend_request_item,
                parent, false);
        FriendRequestViewHolder holder = new FriendRequestViewHolder(mItemView,this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        String mCurrent = mRequestNameList.get(position);
        holder.friendRequestItemView.setText(mCurrent);
        if(stRefList.size()>position) {
            ImageViewUtils.displayCirclePic(context, stRefList.get(mRequestIDList.get(position)), holder.friendPicImageView);
        }
        holder.myClickListener = mlistener;
    }

    @Override
    public int getItemCount() {
        return mRequestIDList.size();
    }



    public class FriendRequestViewHolder extends RecyclerView.ViewHolder{
        private final TextView friendRequestItemView;
        private final Button acceptButton;
        private final Button declineButton;
        private final ImageView  friendPicImageView;
        private final FriendRequestListAdapter mAdapter;
        private AdapterDoubleCallBack myClickListener;

        private FriendRequestViewHolder(View itemView, FriendRequestListAdapter adapter){
            super(itemView);
            friendRequestItemView = itemView.findViewById(R.id.friend_request_name);
            friendPicImageView = itemView.findViewById(R.id.friend_request_pic_imageView);
            acceptButton = itemView.findViewById(R.id.friend_request_accept_button);
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.onAcceptRequest(getAdapterPosition());
                }
            });

            declineButton = itemView.findViewById(R.id.friend_request_decline_button);
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.onDeclineRequest(getAdapterPosition());
                }
            });

            this.mAdapter = adapter;
        }
    }

}
