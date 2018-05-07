package edu.csulb.phylo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import edu.csulb.phylo.Astral.Astral;
import edu.csulb.phylo.Astral.AstralFile;
import edu.csulb.phylo.Astral.AstralHttpInterface;
import edu.csulb.phylo.Astral.AstralRoom;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;

import static com.facebook.internal.FacebookDialogFragment.TAG;

public class InsideRoomFragment extends Fragment {
    //Fragment variables
    private View fragView;
    private User user;

    //Astral Files variables
    List<AstralRoom> astralFileList;
    FileAdapter adapter;

    //Instantiate an InsideRoomFragment object
    public static InsideRoomFragment newInstance() {
        InsideRoomFragment fragment = new InsideRoomFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.fragment_room, container, false);
        return fragView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Data to populate the file with
        astralFileList = null; //Names of the files

        //Recycler View
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.button_file_object);
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
        adapter = new FileAdapter(astralFileList);

        //Set click events here
        recyclerView.setAdapter(adapter);
    }

    /**
     * Retrieves all files around the area
     */
    private void retrieveFiles(final LatLng currUserLocation, final boolean toUpdate) {
        //Start a GET request to retrieve all of the rooms in the area
        final Astral astral = new Astral(getActivity().getString(R.string.astral_base_url));
        //Add logging interceptor
        astral.addLoggingInterceptor(HttpLoggingInterceptor.Level.HEADERS);
        AstralHttpInterface astralHttpInterface = astral.getHttpInterface();

        //Create the GET request
        Call<List<AstralRoom>> request = astralHttpInterface.getRooms(
                getString(R.string.astral_key),
                currUserLocation.latitude,
                currUserLocation.longitude,
                10,
                user.getUserAstralTokens()
        );

        request.enqueue(new Callback<List<AstralRoom>>() {
            @Override
            public void onResponse(Call<List<AstralRoom>> call, retrofit2.Response<List<AstralRoom>> response) {
                if (response.code() == Astral.OK) {
                    Log.d(TAG, "retrieveRooms-> onResponse: Success Code : " + response.code());
                    astralFileList = response.body();
                    //Progress bar must disappear, we have loaded all the rooms
                    if(!toUpdate) {
                        recyclerView.setAdapter(adapter);
                        updateRoomList = true;
                    } else {
                        //We are updating the set of files
                        adapter.changeData(astralFileList);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<AstralRoom>> call, Throwable throwable) {
                Log.w(TAG, "retrieveRooms-> onFailure");
            }
        });
    }
}
