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

public class FriendSearchListAdapter  extends RecyclerView.Adapter<FriendSearchListAdapter.FriendSearchViewHolder> {

    private final LinkedList<String> mSearchResultList;
    private final LinkedList<String> mSearchIDList;
    private HashMap<String, StorageReference> stRefList;
    private LayoutInflater mInflater;
    private MyClickListener mlistener;
    private final Context context;

    public FriendSearchListAdapter(Context context,
                                   LinkedList<String> wordList, LinkedList<String> idList,
                                   HashMap<String, StorageReference> stRefList, MyClickListener listener) {
        mInflater = LayoutInflater.from(context);
        this.mSearchResultList = wordList;
        this.mSearchIDList = idList;
        this.mlistener = listener;
        this.context =context;
        this. stRefList = stRefList;
    }


    public interface MyClickListener{
        void onAddFriend(int p);
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
        String mCurrent = mSearchResultList.get(position);
        if(stRefList.size()>position) {
            ImageViewUtils.displayCirclePic(context, stRefList.get(mSearchIDList.get(position)), holder.searchPicImageView);
        }
        holder.friendSearchResultItemView.setText(mCurrent);
        holder.myClickListener = mlistener;
    }

    @Override
    public int getItemCount() {
        return mSearchResultList.size();
    }




    //Holder
    public class FriendSearchViewHolder extends RecyclerView.ViewHolder{
        private final ImageView searchPicImageView;
        private final TextView friendSearchResultItemView;
        private final Button addFriendButton;
        private final FriendSearchListAdapter mAdapter;
        private FriendSearchListAdapter.MyClickListener myClickListener;

        private FriendSearchViewHolder(View itemView, FriendSearchListAdapter adapter){
            super(itemView);
            friendSearchResultItemView = itemView.findViewById(R.id.friend_search_result_name);
            searchPicImageView = itemView.findViewById(R.id.friend_search_pic_imageView);
            addFriendButton = itemView.findViewById(R.id.friend_search_result_add_friend_button);
            addFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.onAddFriend(getAdapterPosition());
                }
            });

            this.mAdapter = adapter;
        }

    }

}
