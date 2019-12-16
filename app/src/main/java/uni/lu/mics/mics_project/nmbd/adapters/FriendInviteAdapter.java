package uni.lu.mics.mics_project.nmbd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import uni.lu.mics.mics_project.nmbd.R;
import uni.lu.mics.mics_project.nmbd.app.service.ExtendedList.ExtendedListUser;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;

public class FriendInviteAdapter extends RecyclerView.Adapter<FriendInviteAdapter.FriendInviteViewHolder>{

    private final ExtendedListUser mFriendList;
    private LayoutInflater mInflater;
    private AdapterCallBack mlistener;
    private final Context context;


    public FriendInviteAdapter(Context context, ExtendedListUser extListUser,
                             AdapterCallBack listener) {
        mInflater = LayoutInflater.from(context);
        this.mFriendList = extListUser;
        this.mlistener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendInviteAdapter.FriendInviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.friend_item,
                parent, false);
        FriendInviteAdapter.FriendInviteViewHolder holder = new FriendInviteAdapter.FriendInviteViewHolder(mItemView,this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendInviteAdapter.FriendInviteViewHolder holder, int position) {

        String mCurrent = mFriendList.getName(position);
        ImageViewUtils.displayUserCirclePicID(context, mFriendList.getId(position), holder.friendPicImageView);

        holder.friendItemView.setText(mCurrent);
        holder.myClickListener = mlistener;
    }

    @Override
    public int getItemCount() {
        return mFriendList.getSize();
    }


    public class FriendInviteViewHolder extends RecyclerView.ViewHolder{
        private final TextView friendItemView;
        private final Button inviteButton;
        private final ImageView friendPicImageView;
        private final FriendInviteAdapter mAdapter;
        private AdapterCallBack myClickListener;

        private FriendInviteViewHolder(View itemView, FriendInviteAdapter adapter){
            super(itemView);
            friendItemView = itemView.findViewById(R.id.friend_name_label);
            friendPicImageView = itemView.findViewById(R.id.friend_item_pic_imageView);
            inviteButton = itemView.findViewById(R.id.unfriend_button);
            inviteButton.setText("Invite");
            inviteButton.setBackgroundColor(ContextCompat.getColor(inviteButton.getContext(), R.color.blue));
            inviteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.onClickCallback(getAdapterPosition());
                }
            });
            this.mAdapter = adapter;
        }
    }
}
