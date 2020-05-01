package ca.ergotera.remote_ir.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.models.VirtualButton;

/**
 * Fragment (UI and behaviour) allowing users to report bugs directly within
 * the application.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class ButtonListSelectableElementFragment extends android.app.Fragment {

    private static final String CLASS_ID = ButtonListSelectableElementFragment.class.getSimpleName();

    private static final String SELECTABLE_BTN_SELECTED = "SELECTABLE_BTN_SELECTED";
    private static final String BTN_MODEL = "BTN_MODEL";

    private View view;
    private ConstraintLayout buttonListSelectableElement;
    private ImageView btnImageView;
    private TextView btnNameTextView;
    private RadioButton selectedRadioBtn;

    // Virtual button model to represent
    private VirtualButton btnModel;
    private boolean isSelected;

    /**
     * * Fragments should not have constructors, therefore, to pass arguments to a fragment, we need
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
     * @param selected         boolean state that indicates whether or not the button is selected.
     * @return an instance of the fragment with the configured bundle.
     */
    public static ButtonListSelectableElementFragment newInstance(int id, String creationDate, String modificationDate, String btnName, Uri imgPath, Uri audioPath, String signal, boolean selected) {
        ButtonListSelectableElementFragment frag = new ButtonListSelectableElementFragment();
        Bundle bundle = new Bundle(8);
        bundle.putInt(VirtualButton.BTN_ID, id);
        bundle.putString(VirtualButton.BTN_CREATION_DATE, creationDate);
        bundle.putString(VirtualButton.BTN_CREATION_DATE, modificationDate);
        bundle.putString(VirtualButton.BTN_NAME, btnName);
        bundle.putParcelable(VirtualButton.BTN_IMG_PATH, imgPath);
        bundle.putParcelable(VirtualButton.BTN_AUDIO_PATH, audioPath);
        bundle.putString(VirtualButton.BTN_SIGNAL, signal);
        bundle.putBoolean(SELECTABLE_BTN_SELECTED, selected);
        frag.setArguments(bundle);
        return frag;
    }

    /**
     * * Fragments should not have constructors, therefore, to pass arguments to a fragment, we need
     * to use this static method bundling up the parameters we need. We will then be able to
     * retrieve this data by using the getArguments().getTYPE(BUNDLE_IDENTIFIER) methods.
     *
     * @param id               id of the button to be represented by the ButtonListElement.
     * @param creationDate     creation date of the button.
     * @param modificationDate modification of the button.
     * @param btnName          name of of the button to be represented by the ButtonListElement.
     * @param imgPath          image path of the button to be represented by the ButtonListElement.
     * @param audioPath        audio path of the button to be represented by the ButtonListElement.
     * @param signal           signal id of the button to be represented by the ButtonListElement.
     * @return an instance of the fragment with the configured bundle.
     */
    public static ButtonListSelectableElementFragment newInstance(int id, String creationDate, String modificationDate, String btnName, Uri imgPath, Uri audioPath, String signal) {
        ButtonListSelectableElementFragment frag = new ButtonListSelectableElementFragment();
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
    public static ButtonListSelectableElementFragment newInstance(VirtualButton virtualButton) {
        ButtonListSelectableElementFragment frag = new ButtonListSelectableElementFragment();
        Bundle bundle = new Bundle(7);
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

        isSelected = false;

        if (getArguments() != null) {
            this.btnModel = new VirtualButton();
            this.btnModel.setId(getArguments().getInt(VirtualButton.BTN_ID));
            this.btnModel.setCreationDate(getArguments().getString(VirtualButton.BTN_CREATION_DATE));
            this.btnModel.setModificationDate(getArguments().getString(VirtualButton.BTN_MODIFICATION_DATE));
            this.btnModel.setName(getArguments().getString(VirtualButton.BTN_NAME));
            this.btnModel.setImagePath((Uri) getArguments().getParcelable(VirtualButton.BTN_IMG_PATH));
            this.btnModel.setAudioPath((Uri) getArguments().getParcelable(VirtualButton.BTN_AUDIO_PATH));
            this.btnModel.setSignal(getArguments().getString(VirtualButton.BTN_SIGNAL));
            if (getArguments().size() == 7) {
                isSelected = getArguments().getBoolean(SELECTABLE_BTN_SELECTED);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_selectable_btn_element, container, false);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the values you need from your textview into "outState"-object
        super.onSaveInstanceState(outState);
        outState.putParcelable(BTN_MODEL, this.btnModel);
        outState.putBoolean(SELECTABLE_BTN_SELECTED, this.isSelected);
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeComponents();
    }

    private void initializeComponents() {
        findComponents();
        setupComponents();
    }

    private void findComponents() {
        this.buttonListSelectableElement = (ConstraintLayout) view.findViewById(R.id.frag_selectable_btn_element_container);
        this.selectedRadioBtn = (RadioButton) view.findViewById(R.id.frag_selectable_btn_element_radio_btn);
        this.btnImageView = (ImageView) view.findViewById(R.id.frag_selectable_btn_element_btn_img);
        this.btnNameTextView = (TextView) view.findViewById(R.id.frag_selectable_btn_element_btn_name);
    }

    private void setupComponents() {
        setupListElementImage();
        setupButtonListElement();
        setupSelectedRadioBtn();
    }

    /**
     * Sets the image of the button list element if the virtual button data model has one.
     */
    private void setupListElementImage() {
        this.btnNameTextView.setText(this.btnModel.getName());
        if (this.btnModel.getImagePath() != null) {
            this.btnImageView = (ImageView) view.findViewById(R.id.frag_selectable_btn_element_btn_img);
            this.btnImageView.setImageURI(this.btnModel.getImagePath());
        }
    }

    /**
     * Sets the click listener for the button list element.
     */
    private void setupButtonListElement() {
        this.selectedRadioBtn.setChecked(this.isSelected);
        buttonListSelectableElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRadioBtn.setChecked(!selectedRadioBtn.isChecked());
            }
        });
    }

    /**
     * Sets up the selection radio button.
     */
    private void setupSelectedRadioBtn() {
        selectedRadioBtn.setClickable(false);
    }

    public boolean isSelected() {
        return this.selectedRadioBtn.isChecked();
    }

    public int getBtnModelId() {
        return this.btnModel.getId();
    }
}
