package net.ivanvega.mimultimediab2021.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import net.ivanvega.mimultimediab2021.R;
import net.ivanvega.mimultimediab2021.databinding.FragmentDashboardBinding;

import java.io.File;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;

    private Button btnV;
    private VideoView videoV;
    private Uri urivideo;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final TextView textView = binding.textDashboard;


        btnV = (Button)root.findViewById(R.id.btnVideo);
        videoV = (VideoView)root.findViewById(R.id.videoView);

        btnV.setOnClickListener(view -> {
            File file =
                    new File(getActivity().getExternalFilesDir(null),
                            "chilakilvideo.mp3");

            Log.d("RUTA", String.valueOf(file.getAbsoluteFile()));

            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);


            urivideo = FileProvider.getUriForFile(getActivity(),
                    "net.ivanvega.mimultimediab2021.fileprovider" ,
                    file);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, urivideo);

            startActivityForResult(intent, 1001);
        });

        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        videoV.setVideoURI(urivideo);
        videoV.start();
    }
}