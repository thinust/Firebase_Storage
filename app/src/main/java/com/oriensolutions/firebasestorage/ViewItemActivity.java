package com.oriensolutions.firebasestorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.oriensolutions.firebasestorage.adapter.ItemAdapter;
import com.oriensolutions.firebasestorage.model.Item;

import java.util.ArrayList;

public class ViewItemActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;

    private FirebaseStorage storage;

    private ArrayList<Item> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item);


        firestore = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();

        items = new ArrayList<>();

        RecyclerView itemView = findViewById(R.id.itemView);

        ItemAdapter itemAdapter = new ItemAdapter(items, ViewItemActivity.this);
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        itemView.setLayoutManager(layoutManager);
        itemView.setAdapter(itemAdapter);

        firestore.collection("Items").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        items.clear();
                        for (QueryDocumentSnapshot snapshot : task.getResult()) {
                            Item item = snapshot.toObject(Item.class);
                            items.add(item);

                        }
                        itemAdapter.notifyDataSetChanged();
                    }
                });

        firestore.collection("Items").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentSnapshot snapshot : value.getDocuments()) {
                    Item item = snapshot.toObject(Item.class);
                    items.clear();
                    items.add(item);
                }
                items.clear();
                for (DocumentChange change : value.getDocumentChanges()) {

                    Item item = change.getDocument().toObject(Item.class);
                    switch (change.getType()) {
                        case ADDED:
                            items.clear();
                            items.add(item);
                        case MODIFIED:
                            Item old = items.stream().filter(i -> i.getName().equals(item.getName())).findFirst().orElse(null);

//                            for(Item i :items){
//                                if(i.getName().equals(item.getName())){
//
//                                }
//                            }
//
//                            for (int i=0; i<items.size(); i++){
//                                Item item1 = items.get(i);
//                            }

                            if (old != null) {
                                old.setDescription(item.getDescription());
                                old.setPrice(item.getPrice());
                                old.setImage(item.getImage());
                            }
                            break;
                        case REMOVED:
                            items.remove(item);
                    }
                }
                itemAdapter.notifyDataSetChanged();
            }
        });
    }
}