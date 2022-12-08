package com.example.helpinghands;

import static com.example.helpinghands.Utils.checkInternetStatus;
import static com.example.helpinghands.Utils.noInternetConnectionAlert;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RequestsFragment extends Fragment {


    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_requests, container, false);
        User user = new User(requireContext());
        RecyclerView recyclerView = root.findViewById(R.id.recyclerView);

        final List<RequestItem> itemList = new ArrayList<>();

        if( !checkInternetStatus(requireContext())) {noInternetConnectionAlert(requireContext());}
        else {
            LoadingDialogue loadingDialogue = new LoadingDialogue(requireContext());
            loadingDialogue.show();
            TextView loadingText = loadingDialogue.findViewById(R.id.textView7);
            loadingText.setText(R.string.fetching_data_from_database);
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("emergency_requests").orderBy("created", Query.Direction.DESCENDING).whereEqualTo("userId", user.getUserid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            RequestItem requestItem = new RequestItem(
                                    "Help requested",
                                    document.getId(),
                                    (Timestamp) document.get("created"),
                                    document.get("latitude") + "",
                                    document.get("longitude") + "",
                                    document.get("status") + "",
                                    document.get("volunteerNo") + "",
                                    document.get("localeCity") + "");
                            itemList.add(requestItem);
                        }
                        db.collection("emergency_requests").whereEqualTo("volunteerID", user.getUserid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                                if (task1.isSuccessful()) {
                                    for (QueryDocumentSnapshot document1 : task1.getResult()) {
                                        RequestItem requestItem1 = new RequestItem(
                                                getString(R.string.Help_provided),
                                                document1.getId(),
                                                (Timestamp) document1.get("created"),
                                                document1.get("latitude")+"",
                                                document1.get("longitude")+"",
                                                document1.get("status")+"",
                                                document1.get("volunteerNo")+"",
                                                document1.get("localeCity")+"");
                                        itemList.add(requestItem1);
                                    }
                                    Collections.sort(itemList, new sortRequestItems());
                                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                                    recyclerView.setAdapter(new RequestListViewAdapter(getContext(), itemList, new RequestListViewAdapter.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(View view, int position, List<RequestItem> itemList) {
                                            new RequestItemView(requireActivity(), itemList.get(position)).show();

                //                            Toast.makeText(requireContext(), "Position: "+ position, Toast.LENGTH_LONG).show();
                                        }
                                    }));
                                    loadingDialogue.cancel();
//                                    Toast.makeText(getActivity(), "" + itemList.size(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else {
                        Toast.makeText(getActivity(), R.string.Error_connecting_Database, Toast.LENGTH_SHORT).show();
                        loadingDialogue.cancel();
                        Log.e("RequestFragment", task.getException().toString());
                    }
                }
            });
        }

        return root;
    }
}

class sortRequestItems implements Comparator<RequestItem>{

    @Override
    public int compare(RequestItem o1, RequestItem o2) {
        return o2.timestamp.compareTo(o1.timestamp);
    }
}

class RequestItem {
    String requestId;
    Timestamp timestamp;
    String latitude;
    String longitude;
    String status;
    String volunteerNo;
    String city;
    String type;

    public RequestItem(String type, String requestId, Timestamp timestamp, String latitude, String longitude, String status, String volunteerNo, String city) {
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.volunteerNo = volunteerNo;
        this.city = city;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVolunteerNo() {
        return volunteerNo;
    }

    public void setVolunteerNo(String volunteerNo) {
        this.volunteerNo = volunteerNo;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

class RequestListViewHolder extends RecyclerView.ViewHolder {

    TextView date, status, type;
    Button stopRequest;
    View container;

    public RequestListViewHolder(@NonNull View itemView) {
        super(itemView);
        date = itemView.findViewById(R.id.date);
        status = itemView.findViewById(R.id.status);
        stopRequest = itemView.findViewById(R.id.stopRequest);
        type = itemView.findViewById(R.id.type);
        container = itemView;
    }
}

class RequestListViewAdapter extends RecyclerView.Adapter<RequestListViewHolder> {

    Context context;
    List<RequestItem> itemList;
    OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position, List<RequestItem> itemList);
    }

    public RequestListViewAdapter(Context context, List<RequestItem> itemList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.itemList = itemList;
        this.onItemClickListener = onItemClickListener;
    }


    @NonNull
    @Override
    public RequestListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RequestListViewHolder requestListViewHolder = new RequestListViewHolder(
                LayoutInflater.from(context).inflate(R.layout.request_list_row, parent, false));
        requestListViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(v, requestListViewHolder.getAdapterPosition(), itemList);
            }
        });
        return requestListViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RequestListViewHolder holder, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String requestId = itemList.get(position).getRequestId();
        holder.date.setText(itemList.get(position).getTimestamp().toDate().toString());
        holder.type.setText(itemList.get(position).getType());
        String status = itemList.get(position).getStatus();
        holder.status.setText(status);
        if(status.equals("Resolved")){ holder.stopRequest.setVisibility(View.GONE); }
        else{
            holder.stopRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection("emergency_requests").document(requestId).update("status","Resolved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                holder.stopRequest.setVisibility(View.GONE);
                                holder.status.setText("Resolved");
                                User user = new User(context);
                                user.setVolunteerId("");
                                Toast.makeText(context, R.string.Request_stopped_successfully, Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(context, R.string.Error_connecting_Database, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
