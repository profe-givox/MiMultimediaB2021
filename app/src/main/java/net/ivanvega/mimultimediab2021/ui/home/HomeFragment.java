package net.ivanvega.mimultimediab2021.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import net.ivanvega.mimultimediab2021.R;
import net.ivanvega.mimultimediab2021.databinding.FragmentHomeBinding;

import java.io.File;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private File fileFtot;
    private ImageView imgFto;
    private Uri uriFile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
         imgFto = binding.imgFoto;
        final Button btnFto = binding.btnFOto;

        btnFto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentFOto =
                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                Toast.makeText( getActivity(),
                    getActivity().getExternalFilesDir(null).getAbsolutePath(),
                Toast.LENGTH_LONG).show();

                fileFtot = new File(getActivity().getExternalFilesDir(null), "mifoto.jpg");

                uriFile = FileProvider.getUriForFile(getActivity(),
                        "net.ivanvega.mimultimediab2021.fileprovider",
                        fileFtot);

                Toast.makeText( getActivity(),
                        uriFile.toString(),
                        Toast.LENGTH_LONG).show();

                intentFOto.putExtra(MediaStore.EXTRA_OUTPUT, uriFile);
                intentFOto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intentFOto, 1001);

            }
        });

        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imgFto.setImageURI(uriFile);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}