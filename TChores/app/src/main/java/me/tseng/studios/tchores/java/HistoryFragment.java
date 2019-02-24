package me.tseng.studios.tchores.java;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.adapter.SunshineAdapter;
import me.tseng.studios.tchores.java.adapter.SunshineDetailAdapter;
import me.tseng.studios.tchores.java.model.Sunshine;
import me.tseng.studios.tchores.java.viewmodel.HistoryFragmentViewModel;

import static me.tseng.studios.tchores.java.util.SunshineUtil.localDateFromString;


/**
 */
public class HistoryFragment extends Fragment implements
        SunshineAdapter.OnSunshineSelectedListener,
        SunshineDetailAdapter.OnSunshineDetailSelectedListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.recyclersunshines)
    RecyclerView mSunshineRecycler;

    @BindView(R.id.recyclersunshinedetail)
    RecyclerView mSunshineDetailRecycler;

    @BindView(R.id.labelDay)
    TextView mLabelDay;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private static final int SUNSHINE_LIMIT = 21;

    String mCurrentUserId;
    private HistoryFragmentViewModel mViewModel;

    private SunshineAdapter mAdapter;
    private SunshineDetailAdapter mDetailAdapter;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        // View model
        mViewModel = ViewModelProviders.of(this).get(HistoryFragmentViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;
        mCurrentUserId = user.getUid();

        // Get ${CHORE_LIMIT} chores
        mQuery = mFirestore.collection(Sunshine.COLLECTION_PATHNAME)
                .orderBy(Sunshine.FIELD_DAY, Query.Direction.DESCENDING)
                .whereEqualTo(Sunshine.FIELD_USERID, mCurrentUserId)
                .limit(SUNSHINE_LIMIT);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, rootView);
        //need to specify sources of view
        return rootView;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedBundle) {

        // RecyclerView
        mAdapter = new SunshineAdapter(mQuery,this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                final int itemCount = getItemCount();
                if (itemCount == 0) {
                    mSunshineRecycler.setVisibility(View.GONE);
                } else {
                    mSunshineRecycler.setVisibility(View.VISIBLE);

                    if (selectedPos == RecyclerView.NO_POSITION) {
                        LocalDate ldToday = LocalDate.now();
                        for (int i = itemCount - 1; i > 0; i--) {
                            DocumentSnapshot snapshot = getSnapshot(i);
                            LocalDate ld = localDateFromString(snapshot.getString(Sunshine.FIELD_DAY));
                            if (ldToday.isEqual(ld)) {
                                // Select today's sunshine once
                                selectedPos = i;
                                notifyDataSetChanged();
                                onSunshineSelected(snapshot.toObject(Sunshine.class));
                            }
                        }
                    }
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(getView().findViewById(android.R.id.content),
                        "SunshineAdapter Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        // Detail RecyclerView
        mDetailAdapter = new SunshineDetailAdapter(this) {        };


        mSunshineRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));
        mSunshineRecycler.setAdapter(mAdapter);
        mSunshineDetailRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mSunshineDetailRecycler.setAdapter(mDetailAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    // onSunshine*Selected() interfaces implemented below for the 2 RecyclerViews in this fragment
    // for mSunshineRecyclerView
    @Override
    public void onSunshineSelected(Sunshine sunshine) {
        // selected sunshine
        mDetailAdapter.updateSunshine(sunshine);

        final LocalDate ld = localDateFromString(sunshine.getDay());
        String fld = ld.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));
        String sHeading = String.format(getString(R.string.label_sunshine_detail_heading), fld);
        mLabelDay.setText(sHeading);
    }

    // for mSunshineDetailRecyclerView
    @Override
    public void onSunshineDetailSelected(int position) {
        // selected sunshine detail

        // nothing to do here
    }

}
