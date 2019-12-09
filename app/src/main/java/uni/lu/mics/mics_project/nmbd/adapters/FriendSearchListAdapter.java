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

import uni.lu.mics.mics_project.nmbd.R;
import uni.lu.mics.mics_project.nmbd.app.service.ExtendedList.ExtendedListUser;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;

public class FriendSearchListAdapter  extends RecyclerView.Adapter<FriendSearchListAdapter.FriendSearchViewHolder> {

//    private final LinkedList<String> mSearchResultList;
//    private final LinkedList<String> mSearchIDList;
    private final ExtendedListUser mFriendList;

    private LayoutInflater mInflater;
    private AdapterCallBack mlistener;
    private final Context context;

    public FriendSearchListAdapter(Context context,
                                   ExtendedListUser extListUser,
                                   AdapterCallBack listener) {
        mInflater = LayoutInflater.from(context);
//        this.mSearchResultList = wordList;
//        this.mSearchIDList = idList;
        this.mlistener = listener;
        this.context =context;
        this.mFriendList = extListUser;
    }



    @NonNull
    @Override
    public FriendSearchListAdapter.FriendSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View mItemView = mInflater.inflate(R.layout.friend_search_result_item,
                parent, false);
        FriendSearchViewHolder holder = new FriendSearchViewHolder(mItemView,this);
        return holder;

    }

    @Override
    public void onBindViewHolder(@NonNull FriendSearchListAdapter.FriendSearchViewHolder holder, final int position) {
        String mCurrent = mFriendList.getName(position);
        ImageViewUtils.displayUserCirclePicID(context, mFriendList.getId(position), holder.searchPicImageView);
        holder.friendSearchResultItemView.setText(mCurrent);
        holder.myClickListener = mlistener;
    }

    @Override
    public int getItemCount() {
        return mFriendList.getSize();
    }




    //Holder
    public class FriendSearchViewHolder extends RecyclerView.ViewHolder{
        private final ImageView searchPicImageView;
        private final TextView friendSearchResultItemView;
        private final Button addFriendButton;
        private final FriendSearchListAdapter mAdapter;
        private AdapterCallBack myClickListener;

        private FriendSearchViewHolder(View itemView, FriendSearchListAdapter adapter){
            super(itemView);
            friendSearchResultItemView = itemView.findViewById(R.id.friend_search_result_name);
            searchPicImageView = itemView.findViewById(R.id.friend_search_pic_imageView);
            addFriendButton = itemView.findViewById(R.id.friend_search_result_add_friend_button);
            addFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.onClickCallback(getAdapterPosition());
                }
            });

            this.mAdapter = adapter;
        }

    }

}
