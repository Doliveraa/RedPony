package edu.csulb.phylo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import edu.csulb.phylo.Astral.AstralFile;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<AstralFile> astralFileList;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //Each data item is a Button
        public Button file;
        public TextView textView;

        public ViewHolder(View view) {
            super(view);
            file = (Button) view.findViewById(R.id.button_file_object); //file button object
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view){
            //Execute code for onClick
        }
    }

    /**
     * Constructor
     * @param astralFileList list of files
     */
    public FileAdapter(List<AstralFile> astralFileList) {
        this.astralFileList = astralFileList;
    }

    /**
     * Binds
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String fileName = astralFileList.get(position).getName();
        holder.textView.setText(fileName);
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
                .inflate(R.layout.button_file, parent, false);
        FileAdapter.ViewHolder viewHolder = new FileAdapter.ViewHolder(layout);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return astralFileList.size();
    }

    /**
     * Changes the data to be displayed to the user
     *
     * @param astralRoomList The list of files available
     */
    public void changeData(List<AstralFile> astralRoomList) {
        this.astralFileList = astralRoomList;
        this.notifyDataSetChanged();
    }
}
