package ca.ergotera.remote_ir.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.models.VirtualButton;

/**
 * Fragment (UI and behaviour) displaying the list of the virtual buttons of the user.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class ButtonListElementFragment extends android.app.Fragment {

    private static final String CLASS_ID = ButtonListElementFragment.class.getSimpleName();

    private View view;
    private ConstraintLayout elementConstraintLayout;

    // Virtual button model to represent
    private VirtualButton btnModel;

    // UI elements
    private TextView btnNameTextView;
    private ImageView btnImageView;

    /**
     * Fragments should not have constructors, therefore, to pass arguments to a fragment, we need
     * to use this static method bundling up the parameters we need. We will then be able to
     * retrieve this data by using the getArguments().getTYPE(BUNDLE_IDENTIFIER) methods.
     *
     * @param id               id of the button to be represented by the ButtonListElement.
     * @param creationDate     creation date of the button.
     * @param modificationDate modification date of the button.
     * @param btnName          name of of the button to be represented by the ButtonListElement.
     * @param imgPath          image path of the button to be represented by the ButtonListElement.
     * @param audioPath        audio path of the button to be represented by the ButtonListElement.
     * @param signal           signal of the button to be represented by the ButtonListElement.
     * @return an instance of the fragment with the configured bundle.
     */
    public static ButtonListElementFragment newInstance(int id, String creationDate, String modificationDate, String btnName, Uri imgPath, Uri audioPath, String signal) {
        ButtonListElementFragment frag = new ButtonListElementFragment();
        Bundle bundle = new Bundle(7);
        bundle.putInt(VirtualButton.BTN_ID, id);
        bundle.putString(VirtualButton.BTN_CREATION_DATE, creationDate);
        bundle.putString(VirtualButton.BTN_MODIFICATION_DATE, modificationDate);
        bundle.putString(VirtualButton.BTN_NAME, btnName);
        bundle.putParcelable(VirtualButton.BTN_IMG_PATH, imgPath);
        bundle.putParcelable(VirtualButton.BTN_AUDIO_PATH, audioPath);
        bundle.putString(VirtualButton.BTN_SIGNAL, signal);
        frag.setArguments(bundle);
        return frag;
    }

    /**
     * Fragments should not have constructors, therefore, to pass arguments to a fragment, we need
     * to use this static method bundling up the parameters we need. We will then be able to
     * retrieve this data by using the getArguments().getTYPE(BUNDLE_IDENTIFIER) methods.
     *
     * @param virtualButton button to be represented by the ButtonListElement.
     * @return an instance of the fragment with the configured bundle.
     */
    public static ButtonListElementFragment newInstance(VirtualButton virtualButton) {
        ButtonListElementFragment frag = new ButtonListElementFragment();
        Bundle bundle = new Bundle(5);
        bundle.putInt(VirtualButton.BTN_ID, virtualButton.getId());
        bundle.putString(VirtualButton.BTN_CREATION_DATE, virtualButton.getCreationDate());
        bundle.putString(VirtualButton.BTN_MODIFICATION_DATE, virtualButton.getModificationDate());
        bundle.putString(VirtualButton.BTN_NAME, virtualButton.getName());
        bundle.putParcelable(VirtualButton.BTN_IMG_PATH, virtualButton.getImagePath());
        bundle.putParcelable(VirtualButton.BTN_AUDIO_PATH, virtualButton.getAudioPath());
        bundle.putString(VirtualButton.BTN_SIGNAL, virtualButton.getSignal());
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.btnModel = new VirtualButton();
            this.btnModel.setId(getArguments().getInt(VirtualButton.BTN_ID));
            this.btnModel.setCreationDate(getArguments().getString(VirtualButton.BTN_CREATION_DATE));
            this.btnModel.setModificationDate(getArguments().getString(VirtualButton.BTN_MODIFICATION_DATE));
            this.btnModel.setName(getArguments().getString(VirtualButton.BTN_NAME));
            this.btnModel.setImagePath((Uri) getArguments().getParcelable(VirtualButton.BTN_IMG_PATH));
            this.btnModel.setAudioPath((Uri) getArguments().getParcelable(VirtualButton.BTN_AUDIO_PATH));
            this.btnModel.setSignal(getArguments().getString(VirtualButton.BTN_SIGNAL));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_button_list_element, container, false);
        setListElementImage();
        setupButtonListElement();
        return view;
    }

    /**
     * Sets the image of the button list element if the virtual button data model has one.
     */
    private void setListElementImage() {
        this.btnNameTextView = (TextView) view.findViewById(R.id.frag_button_list_element_btn_name);
        this.btnNameTextView.setText(this.btnModel.getName());
        if (this.btnModel.getImagePath() != null) {
            this.btnImageView = (ImageView) view.findViewById(R.id.frag_button_list_element_btn_img);
            this.btnImageView.setImageURI(this.btnModel.getImagePath());
        }
    }

    /**
     * Sets the click listener for the button list element.
     */
    private void setupButtonListElement() {
        elementConstraintLayout = view.findViewById(R.id.frag_button_list_element_container);
        elementConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.FragmentManager fragMgr = getFragmentManager();
                fragMgr.beginTransaction().replace(R.id.content_frame, CreateButtonFragment.newInstance(btnModel.getId())).addToBackStack(null).commit();
            }
        });
    }
}
