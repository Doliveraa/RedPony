package edu.csulb.phylo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import edu.csulb.phylo.Astral.AstralRoom;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
    private List<AstralRoom> astralRoomList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //Each data item is a Button
        public Button room;
        public ViewHolder(Button b) {
            super(b);
            room = b;
        }
    }

    /**
     * Room constructor
     *
     * @param astralRoomList A list of rooms
     */
    public RoomAdapter(List<AstralRoom> astralRoomList) {
        this.astralRoomList = astralRoomList;
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
        Button button = (Button) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.button, parent, false);
        ViewHolder viewHolder = new ViewHolder(button);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.room.setText(astralRoomList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return astralRoomList.size();
    }

    /**
     * Changes the data to be displayed to the user
     *
     * @param astralRoomList The list of rooms available
     */
    public void changeData(List<AstralRoom> astralRoomList) {
        this.astralRoomList = astralRoomList;
        this.notifyDataSetChanged();
    }
}
