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
import uni.lu.mics.mics_project.nmbd.app.service.ExtendedList.ExtendedListInvitation;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;

public class EventInvitationAdapter extends RecyclerView.Adapter<EventInvitationAdapter.EventInvitationViewHolder>{
    private final String TAG = "EventInvitationAdapter";
    private ExtendedListInvitation eventExtList;
    private LayoutInflater mInflater;
    private AdapterCallBack mlistener;
    private final Context context;


    public EventInvitationAdapter(Context context, ExtendedListInvitation extendedListInvitation, AdapterCallBack listener) {
        mInflater = LayoutInflater.from(context);
        this.eventExtList = extendedListInvitation;
        this.mlistener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public EventInvitationAdapter.EventInvitationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.event_invitation_item,
                parent, false);
        EventInvitationAdapter.EventInvitationViewHolder holder = new EventInvitationAdapter.EventInvitationViewHolder(mItemView,this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventInvitationAdapter.EventInvitationViewHolder holder, int position) {

        String eventName = eventExtList.getEventName(position);
        String organizorID = eventExtList.geteventOrganizorsId(position);
        String organizorName = eventExtList.geteventOrganizorsName(position);

        ImageViewUtils.displayUserCirclePicID(context, organizorID,holder.organiserPicImageView);
        holder.nameEventView.setText(eventName);
        holder.nameInvitatorTextview.setText(organizorName);
        holder.myClickListener = mlistener;
    }

    @Override
    public int getItemCount() {
        return eventExtList.getSize();
    }


    public class EventInvitationViewHolder extends RecyclerView.ViewHolder{
        private final TextView nameEventView;
        private final ImageView organiserPicImageView;
        private final TextView nameInvitatorTextview;
        private final EventInvitationAdapter mAdapter;
        private final Button viewEventButton;
        private AdapterCallBack myClickListener;

        private EventInvitationViewHolder(View itemView, EventInvitationAdapter adapter){
            super(itemView);
            nameInvitatorTextview = itemView.findViewById(R.id.nameOfInvitor);
            nameEventView = itemView.findViewById(R.id.nameEvent);
            organiserPicImageView = itemView.findViewById(R.id.organisorImageView);
            viewEventButton = itemView.findViewById(R.id.viewEvent);
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

