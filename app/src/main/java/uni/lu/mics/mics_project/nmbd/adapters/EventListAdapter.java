package uni.lu.mics.mics_project.nmbd.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import uni.lu.mics.mics_project.nmbd.R;
import uni.lu.mics.mics_project.nmbd.app.service.ExtendedListEvent;
import uni.lu.mics.mics_project.nmbd.app.service.ImageViewUtils;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventListViewHolder>{
    private final String TAG = "EventListAdapter";
    private ExtendedListEvent extList;
    private LayoutInflater mInflater;
    private AdapterCallBack mlistener;
    private final Context context;


    public EventListAdapter(Context context, ExtendedListEvent extList, AdapterCallBack listener) {
        mInflater = LayoutInflater.from(context);
        this.extList = extList;
        this.mlistener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public EventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.event_cardview_item,
                parent, false);
        EventListViewHolder holder = new EventListViewHolder(mItemView,this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventListViewHolder holder, int position) {

        String name = extList.getName(position);

        String date = extList.getDate(position);
        String address= extList.getAddress(position);
        String category = extList.getCategory(position);

        if(extList.getStrgListSize()>position) {
            ImageViewUtils.displayPic(context, extList.getStrgRef(position), holder.eventPicImageView);
        }
        holder.nameItemView.setText(name);
        holder.addressTextView.setText(address);
        holder.dateTextView.setText(date);
        holder.cateTextView.setText(category);
        holder.myClickListener = mlistener;
    }

    @Override
    public int getItemCount() {
        return extList.getSize();
    }


    public class EventListViewHolder extends RecyclerView.ViewHolder{
        private final TextView nameItemView;
        private final TextView addressTextView;
        private final TextView dateTextView;
        private final Button viewEventButton;
        private final ImageView eventPicImageView;
        private final TextView cateTextView;
        private final EventListAdapter mAdapter;
        private AdapterCallBack myClickListener;

        private EventListViewHolder(View itemView, EventListAdapter adapter){
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_textview);
            addressTextView = itemView.findViewById(R.id.address_textview);
            cateTextView = itemView.findViewById(R.id.cate_textview);
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

