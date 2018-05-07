package edu.csulb.phylo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.util.List;

import edu.csulb.phylo.Astral.AstralItem;
import edu.csulb.phylo.Astral.RoomKey;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    public interface OnRoomClickedListener{
        void onRoomClick(String roomPassword);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //Each data item is a Button
        public Button room;
        public ImageView keyImage;

        public ViewHolder(View view) {
            super(view);
            room = (Button) view.findViewById(R.id.button_room_object);
            keyImage = (ImageView) view.findViewById(R.id.imageview_key);
        }

        public void bind(final AstralItem astralItem, final OnRoomClickedListener onRoomClickedListener){
            //Check to see if the room comes with a password
            Gson converter = new Gson();
            final RoomKey roomKey = converter.fromJson(astralItem.getData(), RoomKey.class);
            if(roomKey.getRoomKey().isEmpty()) {
                //The room does not have a password, remove the key image
                keyImage.setVisibility(View.GONE);
            }
            //Set the name of the room
            room.setText(astralItem.getName());
            //Set the listener
            room.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "bind-> onClick");
                    onRoomClickedListener.onRoomClick(roomKey.getRoomKey());
                }
            });
        }

    }

    //Constants
    private final static String TAG = RoomAdapter.class.getSimpleName();
    private List<AstralItem> astralItemList;
    //Interface
    private OnRoomClickedListener listener;


    /**
     * Room constructor
     *
     * @param astralItemList A list of rooms
     */
    public RoomAdapter(List<AstralItem> astralItemList, OnRoomClickedListener listener) {
        this.astralItemList = astralItemList;
        this.listener = listener;
    }

    /**
     * Create new views which is invoked by the layout manager
     *
     * @param parent The Parent view group (container)
     *
     * @param viewType The view type specified
     *
     * @return a view object
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Create a new view
        FrameLayout layout = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.button_room, parent, false);
        ViewHolder viewHolder = new ViewHolder(layout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Turn the RoomKey JSON to an object
        holder.bind(astralItemList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return astralItemList.size();
    }

    /**
     * Changes the data to be displayed to the user
     *
     * @param astralItemList The list of rooms available
     */
    public void changeData(List<AstralItem> astralItemList) {
        this.astralItemList = astralItemList;
        this.notifyDataSetChanged();
    }
}
