package uni.lu.mics.mics_project.nmbd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import uni.lu.mics.mics_project.nmbd.R;

public class FriendRequestListAdapter extends RecyclerView.Adapter<FriendRequestListAdapter.FriendRequestViewHolder>{

    private final LinkedList<String> mRequestNameList;
    private final LinkedList<String> mRequestIDList;
    private LayoutInflater mInflater;
    private MyClickListener mlistener;


    public FriendRequestListAdapter(Context context, LinkedList<String> nameList, LinkedList<String> idList, MyClickListener listener) {
        mInflater = LayoutInflater.from(context);
        this.mRequestNameList = nameList;
        this.mRequestIDList = idList;
        this.mlistener = listener;
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
        holder.myClickListener = mlistener;
    }

    @Override
    public int getItemCount() {
        return mRequestIDList.size();
    }

    public interface MyClickListener{
        void onAcceptRequest(int p);
        void onDeclineRequest(int p);
    }

    public class FriendRequestViewHolder extends RecyclerView.ViewHolder{
        public final TextView friendRequestItemView;
        public final Button acceptButton;
        public final Button declineButton;
        final FriendRequestListAdapter mAdapter;
        public FriendRequestListAdapter.MyClickListener myClickListener;

        public FriendRequestViewHolder(View itemView, FriendRequestListAdapter adapter){
            super(itemView);
            friendRequestItemView = itemView.findViewById(R.id.friend_request_name);

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
