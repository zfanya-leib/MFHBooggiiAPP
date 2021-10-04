package com.restart.myapplicationactivitytest;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.restart.myapplicationactivitytest.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private boolean videoToggle = false;
    private MediaController mediaController;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        mediaController= new MediaController(getContext());
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnEventsOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        binding.btnImgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SettingsFragment);
            }
        });

        binding.btnImgSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!videoToggle) {
                    videoToggle=true;
                    ImageView img = (ImageView) getActivity().findViewById(R.id.imageView);
                    img.setVisibility(View.INVISIBLE);
                    VideoView video = (VideoView) getActivity().findViewById(R.id.video_player);
                    video.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.a));
                    video.setVisibility(View.VISIBLE);
                    video.setMediaController(mediaController);
                    video.start();
                }
                else{
                    videoToggle=false;
                    VideoView video = (VideoView) getActivity().findViewById(R.id.video_player);
                    video.stopPlayback();
                    video.setVisibility(View.INVISIBLE);
                    ImageView img = (ImageView) getActivity().findViewById(R.id.imageView);
                    img.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}