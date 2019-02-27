package me.tseng.studios.tchores.java;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.adapter.AwardsAdapter;
import me.tseng.studios.tchores.java.model.Sunshine;
import me.tseng.studios.tchores.java.viewmodel.AwardsFragmentViewModel;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AwardsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AwardsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AwardsFragment extends Fragment implements
        AwardsAdapter.OnAwardSelectedListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.recyclerAwards)
    RecyclerView mAwardRecycler;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    String mCurrentUserId;
    private AwardsFragmentViewModel mViewModel;

    private AwardsAdapter mAdapter;

    public AwardsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AwardsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AwardsFragment newInstance(String param1, String param2) {
        AwardsFragment fragment = new AwardsFragment();
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
        mViewModel = ViewModelProviders.of(this).get(AwardsFragmentViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;
        mCurrentUserId = user.getUid();


        mQuery = mFirestore.collection(Sunshine.COLLECTION_PATHNAME)
                .whereEqualTo(Sunshine.FIELD_USERID, mCurrentUserId)
                .whereEqualTo(Sunshine.FIELD_AWARDPERFECTDAY, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_awards, container, false);
        ButterKnife.bind(this, rootView);
        //need to specify sources of view
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

/*
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
                        for (int i = itemCount - 1; i >= 0; i--) {
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

    }*/

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

    public void onAwardSelected(int x) {

    }
}
