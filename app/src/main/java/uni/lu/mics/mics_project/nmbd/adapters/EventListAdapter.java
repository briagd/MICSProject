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

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventListViewHolder>{

    private final LinkedList<String> mFriendNameList;
    private final LinkedList<String> mFriendIDList;
    private HashMap<String, StorageReference> stRefList;
    private LayoutInflater mInflater;
    private AdapterCallBack mlistener;
    private final Context context;


    public EventListAdapter(Context context, LinkedList<String> nameList, LinkedList<String> idList,
                             HashMap<String, StorageReference> stRefList, AdapterCallBack listener) {
        mInflater = LayoutInflater.from(context);
        this.mFriendNameList = nameList;
        this.mFriendIDList = idList;
        this.mlistener = listener;
        this.context = context;
        this.stRefList = stRefList;
    }

    @NonNull
    @Override
    public EventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.friend_item,
                parent, false);
        EventListViewHolder holder = new EventListViewHolder(mItemView,this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventListViewHolder holder, int position) {

        String mCurrent = mFriendNameList.get(position);
        if(stRefList.size()>position) {
            ImageViewUtils.displayPic(context, stRefList.get(mFriendIDList.get(position)), holder.eventPicImageView);
        }
        holder.nameItemView.setText(mCurrent);
        holder.myClickListener = mlistener;
    }

    @Override
    public int getItemCount() {
        return mFriendNameList.size();
    }


    public class EventListViewHolder extends RecyclerView.ViewHolder{
        private final TextView nameItemView;
        private final TextView addressTextView;
        private final TextView dateTextView;
        private final Button viewEventButton;
        private final ImageView eventPicImageView;
        private final EventListAdapter mAdapter;
        private AdapterCallBack myClickListener;

        private EventListViewHolder(View itemView, EventListAdapter adapter){
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_textview);
            addressTextView = itemView.findViewById(R.id.address_textview);
            nameItemView = itemView.findViewById(R.id.name_textview);
            eventPicImageView = itemView.findViewById(R.id.event_imageview);
            viewEventButton = itemView.findViewById(R.id.view_event_button);
            viewEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.onClickCallback(getAdapterPosition());
                }
            });
            this.mAdapter = adapter;
        }
    }
}

