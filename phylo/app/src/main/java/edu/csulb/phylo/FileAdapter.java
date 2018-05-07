package edu.csulb.phylo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import edu.csulb.phylo.Astral.Astral;
import edu.csulb.phylo.Astral.AstralFile;
import edu.csulb.phylo.Astral.AstralItem;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private List<AstralItem> astralItemList;

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
     * @param astralItemList list of files
     */
    public FileAdapter(List<AstralItem> astralItemList) {
        this.astralItemList = astralItemList;
    }

    /**
     * Binds
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String fileName = astralItemList.get(position).getName();
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
        return astralItemList.size();
    }

    /**
     * Changes the data to be displayed to the user
     *
     * @param astralItemList The list of files available
     */
    public void changeData(List<AstralItem> astralItemList) {
        this.astralItemList = astralItemList;
        this.notifyDataSetChanged();
    }
}
