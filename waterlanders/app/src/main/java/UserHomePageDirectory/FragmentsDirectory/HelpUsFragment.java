package UserHomePageDirectory.FragmentsDirectory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.waterlanders.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

        CardView cardView6 = view.findViewById(R.id.card_view6);

        // Set up click listeners for each card
        cardView.setOnClickListener(v -> toggleDetails(detailsTextView, 0));
        cardView1.setOnClickListener(v -> toggleDetails(detailsTextView1, 1));
        cardView2.setOnClickListener(v -> toggleDetails(detailsTextView2, 2));
        cardView3.setOnClickListener(v -> toggleDetails(detailsTextView3, 3));
        cardView4.setOnClickListener(v -> toggleDetails(detailsTextView4, 4));
        cardView5.setOnClickListener(v -> toggleDetails(detailsTextView5, 5));

        // CardView for opening PDF
        cardView6.setOnClickListener(v -> {
            Log.d("CardViewClick", "Button clicked");
            try {
                // Open PDF from raw folder
                InputStream inputStream = getResources().openRawResource(R.raw.utorial);
                Log.d("CardViewClick", "PDF resource opened");

                // Create a temporary file to copy the PDF
                File tempFile = new File(getContext().getCacheDir(), "utorial.pdf");
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);

                // Copy the content of the InputStream to the temporary file
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, length);
                }

                // Close the streams
                fileOutputStream.close();
                inputStream.close();
                Log.d("CardViewClick", "PDF written to temp file: " + tempFile.getAbsolutePath());

                // Open the PDF using an Intent
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(getContext(), "com.example.waterlanders.fileprovider", tempFile);
                intent.setDataAndType(uri, "*/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(intent);
                    Log.d("CardViewClick", "Intent started successfully");
                } else {
                    Log.e("CardViewClick", "No application to handle PDF");
                    Toast.makeText(getContext(), "No app found to open PDF", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error opening PDF", Toast.LENGTH_SHORT).show();
            }
        });

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
