package me.tseng.studios.tchores.java;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.adapter.ChoreAdapter;
import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.viewmodel.ChoresFragmentViewModel;


/**
 *
 */
public class ChoresFragment extends Fragment implements
        ChoreAdapter.OnChoreSelectedListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.recyclerchores)
    RecyclerView mchoresRecycler;

    @BindView(R.id.viewEmpty)
    ViewGroup mEmptyView;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private static final int CHORE_LIMIT = 50;

    String mCurrentUserName;
    private ChoresFragmentViewModel mViewModel;

    private ChoreAdapter mAdapter;


    public ChoresFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChoresFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChoresFragment newInstance(String param1, String param2) {
        ChoresFragment fragment = new ChoresFragment();
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
        mViewModel = ViewModelProviders.of(this).get(ChoresFragmentViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;
        mCurrentUserName = user.getDisplayName();
        mViewModel.getFilters().setCategory(mCurrentUserName);

        // Get ${CHORE_LIMIT} chores
        mQuery = mFirestore.collection("chores")
                .orderBy(Chore.FIELD_ADTIME, Query.Direction.DESCENDING)
                .whereEqualTo(Chore.FIELD_CATEGORY, mCurrentUserName)
                .limit(CHORE_LIMIT);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.fragment_chores, container, false);
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedBundle) {

        // RecyclerView
        mAdapter = new ChoreAdapter(mQuery,this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mchoresRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mchoresRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(getView().findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mchoresRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mchoresRecycler.setAdapter(mAdapter);

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



    @OnClick(R.id.fabShowchoreAddDialog)
    public void onAddRatingClicked(View view) {
        //new code
        Intent intent = new Intent(getContext(), ChoreAddActivity.class);
        startActivity(intent);
    }


    @Override
    public void onchoreSelected(DocumentSnapshot chore) {
        // Go to the details page for the selected chore
        Intent intent = new Intent(getContext(), ChoreDetailActivity.class);
        intent.putExtra(ChoreDetailActivity.KEY_CHORE_ID, chore.getId());
        intent.putExtra(ChoreDetailActivity.KEY_ACTION, ChoreDetailActivity.ACTION_VIEW);

        startActivity(intent);
        //  TODO overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);


    }

}