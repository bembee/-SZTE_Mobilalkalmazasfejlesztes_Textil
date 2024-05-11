package com.example.textil2;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int RC_NOTIFICATION = 99;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ArrayList<ShoppingItem> mItemList;
    private ShoppingItemAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;
    private NotificationHandler mNotificationHander;
    private int cartItems = 0;
    private int gridNumber = 1;
    private boolean viewRow = true;

    private FrameLayout redCircle;
    private TextView countTextView;
    private AlarmManager mAlarmManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();
        mAdapter = new ShoppingItemAdapter(this, mItemList);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");
        mNotificationHander = new NotificationHandler(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, RC_NOTIFICATION);
        }

        setAlarmManager();
        queryData();

    }


    private void queryData() {
        mItemList.clear();
        mItems.orderBy("carCount", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ShoppingItem item = document.toObject(ShoppingItem.class);
                item.setId(document.getId());
                mItemList.add(item);
            }

            if (mItemList.size() == 0) {
                initlizeData();
                queryData();
            }
            mAdapter.notifyDataSetChanged();

        });

    }

    public void deleteItem(ShoppingItem item) {
        DocumentReference ref = mItems.document(item._getId());

        ref.delete().addOnSuccessListener(sucess -> {
                    Toast.makeText(this, "Item is successfully deleted: " + item._getId(), Toast.LENGTH_LONG).show();
                }
        ).addOnFailureListener(failure -> {
            Toast.makeText(this, "Item is can not deleted: ", Toast.LENGTH_LONG).show();

        });
        queryData();

    }


    private void initlizeData() {
        String[] itemList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemPrice = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.shopping_item_images);


        for (int i = 0; i < itemList.length; i++) {
            mItems.add(new ShoppingItem(
                    itemList[i],
                    itemInfo[i],
                    itemPrice[i],
                    itemsImageResource.getResourceId(i, 0), 0));
            Log.d("tag", "adatfeltölt: " + itemInfo[i]);
        }
        itemsImageResource.recycle();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d("TAG", "menu létrejött: ");
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.app_bar_logout_button) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
            return true;
        } else if (item.getItemId() == R.id.app_view_selector) {
            if (viewRow) {
                changeSpanCount(item, R.drawable.baseline_view_module_24, 1);
            } else {
                changeSpanCount(item, R.drawable.baseline_view_stream_24, 2);
            }

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.app_bar_cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        countTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(alertMenuItem);
            }
        });
        return super.onPrepareOptionsMenu(menu);

    }

    public void updateAlertIcon(ShoppingItem item) {
        cartItems = (cartItems + 1);
        if (0 < cartItems) {
            countTextView.setText(String.valueOf(cartItems));
        } else {
            countTextView.setText("");
        }

        redCircle.setVisibility((cartItems > 0) ? VISIBLE : GONE);

        mItems.document(item._getId()).update("carCount", item.getCarCount() + 1)
                .addOnFailureListener(failure -> {
                    Toast.makeText(this, "Item is cannot changed: ", Toast.LENGTH_LONG).show();
                });
        mNotificationHander.send("Köszönjök vásárlását");
        queryData();
    }

    private void setAlarmManager() {
        long triggerTime = AlarmManager.INTERVAL_HALF_HOUR;
        Intent intent = new Intent(this, MyReceiver.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        } else {
            pendingIntent = PendingIntent.getActivity(this,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, triggerTime, pendingIntent);

    }

}




