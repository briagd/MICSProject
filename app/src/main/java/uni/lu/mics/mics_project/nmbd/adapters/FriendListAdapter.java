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

import java.util.LinkedList;

import uni.lu.mics.mics_project.nmbd.R;
import uni.lu.mics.mics_project.nmbd.app.service.ImageViewUtils;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendListViewHolder>{

    private final LinkedList<String> mFriendNameList;
    private final LinkedList<String> mFriendIDList;
    LinkedList<StorageReference> stRefList;
    private LayoutInflater mInflater;
    private MyClickListener mlistener;
    private final Context context;


    public FriendListAdapter(Context context, LinkedList<String> nameList, LinkedList<String> idList, LinkedList<StorageReference> stRefList, MyClickListener listener) {
        mInflater = LayoutInflater.from(context);
        this.mFriendNameList = nameList;
        this.mFriendIDList = idList;
        this.mlistener = listener;
        this.context = context;
        this.stRefList = stRefList;
    }

    @NonNull
    @Override
    public FriendListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.friend_item,
                parent, false);
        FriendListViewHolder holder = new FriendListViewHolder(mItemView,this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendListViewHolder holder, int position) {

        String mCurrent = mFriendNameList.get(position);
        if(stRefList.size()>position) {
            ImageViewUtils.displayCirclePic(context, stRefList.get(position), holder.friendPicImageView);
        }
        holder.friendItemView.setText(mCurrent);
        holder.myClickListener = mlistener;
    }

    @Override
    public int getItemCount() {
        return mFriendNameList.size();
    }

    public interface MyClickListener{
        void onUnfriendRequest(int p);

    }

    public class FriendListViewHolder extends RecyclerView.ViewHolder{
        public final TextView friendItemView;
        public final Button unfriendButton;
        public final ImageView friendPicImageView;
        final FriendListAdapter mAdapter;
        public FriendListAdapter.MyClickListener myClickListener;

        public FriendListViewHolder(View itemView, FriendListAdapter adapter){
            super(itemView);
            friendItemView = itemView.findViewById(R.id.friend_name_label);
            friendPicImageView = itemView.findViewById(R.id.friend_item_pic_imageView);
            unfriendButton = itemView.findViewById(R.id.unfriend_button);
            unfriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.onUnfriendRequest(getAdapterPosition());
                }
            });
            this.mAdapter = adapter;
        }
    }
}
