package UserHomePageDirectory.FragmentsDirectory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;



import com.example.waterlanders.R;

public class HelpUsFragment extends Fragment {

    // Track the visibility state for each card
    private boolean[] isExpanded = {false, false, false, false, false, false};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help_us, container, false);

        // Find all the views
        CardView cardView = view.findViewById(R.id.card_view);
        TextView detailsTextView = view.findViewById(R.id.details);

        CardView cardView1 = view.findViewById(R.id.card_view1);
        TextView detailsTextView1 = view.findViewById(R.id.details1);

        CardView cardView2 = view.findViewById(R.id.card_view2);
        TextView detailsTextView2 = view.findViewById(R.id.details2);

        CardView cardView3 = view.findViewById(R.id.card_view3);
        TextView detailsTextView3 = view.findViewById(R.id.details3);

        CardView cardView4 = view.findViewById(R.id.card_view4);
        TextView detailsTextView4 = view.findViewById(R.id.details4);

        CardView cardView5 = view.findViewById(R.id.card_view5);
        TextView detailsTextView5 = view.findViewById(R.id.details5);

        // Set up click listeners for each card
        cardView.setOnClickListener(v -> toggleDetails(detailsTextView, 0));
        cardView1.setOnClickListener(v -> toggleDetails(detailsTextView1, 1));
        cardView2.setOnClickListener(v -> toggleDetails(detailsTextView2, 2));
        cardView3.setOnClickListener(v -> toggleDetails(detailsTextView3, 3));
        cardView4.setOnClickListener(v -> toggleDetails(detailsTextView4, 4));
        cardView5.setOnClickListener(v -> toggleDetails(detailsTextView5, 5));

        return view;
    }

    // Method to toggle the visibility of the details
    private void toggleDetails(TextView detailsTextView, int index) {
        if (isExpanded[index]) {
            // Collapse the details
            detailsTextView.setVisibility(View.GONE);
        } else {
            // Expand the details
            detailsTextView.setVisibility(View.VISIBLE);
        }
        isExpanded[index] = !isExpanded[index];  // Toggle the expansion state
    }
}
