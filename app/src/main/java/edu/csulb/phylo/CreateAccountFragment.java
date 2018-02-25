package edu.csulb.phylo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Daniel on 2/24/2018.
 */

public class CreateAccountFragment extends Fragment
        implements View.OnClickListener {

    //Views
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private TextView invalidPasswordGuideTextView;
    private ProgressBar progressBar;
    private Button createAccountButton;
    //Constants
    private String TAG = "CreateAccountFragment";
    //Variables
    private AlertDialog.Builder builder;
    CognitoUserPool cognitoUserPool;
    private boolean accountCreated;
    //Interface
    public interface OnAccountCreatedListener{
        void onAccountCreated(CognitoUser cognitoUser);
        void onCreateAccountFinished();
    }
    private OnAccountCreatedListener onAccountCreatedListener;
    //Handlers
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            updateButton();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    SignUpHandler signupCallback = new SignUpHandler() {
        @Override
        public void onSuccess(CognitoUser cognitoUser, boolean userConfirmed,
                              CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
            //Sign Up was successful

            //Check if the user needs to be confirmed
            if (!userConfirmed) {
                Log.d(TAG, "onSuccess: userNotConfirmed");
                //Send CognitoUser object to the container to hold it
                onAccountCreatedListener.onAccountCreated(cognitoUser);
                // This user must be confirmed and a confirmation code was sent to the user
                // cognitoUserCodeDeliveryDetails will indicate where the confirmation code was sent
                // Get the confirmation code from user
                String codeDeliveraryDestination = cognitoUserCodeDeliveryDetails.getDestination();
                String codeDeliveryMedium = cognitoUserCodeDeliveryDetails.getDeliveryMedium();
                //Display to the user the verification code information
                builder.setTitle("Success");
                builder.setMessage("The verification code was sent to " +
                        codeDeliveraryDestination + " via " + codeDeliveryMedium + ".");
                builder.show();
                Log.d(TAG, "CodeDeliveryDestination: " + codeDeliveraryDestination + "\n" +
                        "CodeMedium: " + codeDeliveryMedium);
                accountCreated = true;
            } else {
                //The user has already been confirmed
                Log.d(TAG, "OnSuccess: user is already confirmed");
            }
        }

        @Override
        public void onFailure(Exception exception) {
            //Sign up failed, check exception for the cause
            Log.d(TAG, "Sign Up Failed");
            if(exception instanceof UsernameExistsException) {
                Log.d(TAG, "Email already exists : " + emailEditText.getText().toString());
                progressBar.setVisibility(View.GONE);
                builder.setTitle("Error");
                builder.setMessage("The email has already been registered.");
                builder.show();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Initalize Variables
        accountCreated = false;

        //Initialize the views
        firstNameEditText = (EditText) getActivity().findViewById(R.id.first_name_edit_text);
        lastNameEditText = (EditText) getActivity().findViewById(R.id.last_name_edit_text);
        emailEditText = (EditText) getActivity().findViewById(R.id.email_edit_text);
        passwordEditText = (EditText) getActivity().findViewById(R.id.password_edit_text);
        confirmPasswordEditText = (EditText) getActivity().findViewById(R.id.confirm_password_edit_text);
        invalidPasswordGuideTextView = (TextView) getActivity().findViewById(R.id.invalid_password_guide_text_view);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.create_account_progress_bar);
        createAccountButton = (Button) getActivity().findViewById(R.id.create_account_button);
        createAccountButton.setEnabled(false);

        //Attach Listeners
        createAccountButton.setOnClickListener(this);
        firstNameEditText.addTextChangedListener(textWatcher);
        lastNameEditText.addTextChangedListener(textWatcher);
        emailEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);
        confirmPasswordEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (invalidPasswordGuideTextView.getVisibility() == View.VISIBLE) {
                    String password = charSequence.toString();
                    if (passwordGuidelineCheck(password)) {
                        invalidPasswordGuideTextView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (invalidPasswordGuideTextView.getVisibility() == View.GONE) {
                    String password = passwordEditText.getText().toString();
                    if (!hasFocus && !passwordGuidelineCheck(password)) {
                        invalidPasswordGuideTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        //Create alert dialog box
        builder = new AlertDialog.Builder(getActivity());
        //Give the user the option to press ok when the user get an error
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (accountCreated) {
                    getActivity().getFragmentManager().popBackStack();
                    onAccountCreatedListener.onCreateAccountFinished();
                } else {
                    //Maybe automatically set the cursor back to the empty input area and make the
                    //soft-keyboard show
                }
            }
        });
    }

    private void createUserAccount(String firstName, String lastName, String emailAddress, String password) {
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();

        //Add required user attributes
        userAttributes.addAttribute("name", firstName + " " + lastName);
        userAttributes.addAttribute("email", emailAddress);

        //Call the sign-up API
        cognitoUserPool.signUpInBackground(emailAddress, password, userAttributes, null, signupCallback);

    }

    //Checks the user's password every time to prevent dictionary attacks
    private boolean passwordGuidelineCheck(String password) {
        /**
         * Passwords must contain at least one a-z character, one A-Z character
         * one 0-9 character,and be 8 characters long minimum
         */
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public static boolean isEmailValid(String email) {
        String expression = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"" +
                "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\" +
                "x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]" +
                "*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|" +
                "[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\" +
                "x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    //Prints a message to the user in the form of a dialog
    private void displayErrorMessage(int messageID) {
        builder.setTitle("Error Message");
        builder.setMessage(messageID);
        builder.show();
    }

    //Updates the Button color if all the right conditions are met
    private void updateButton() {
        if (!createAccountButton.isEnabled() && !firstNameEditText.getText().toString().isEmpty() && !lastNameEditText.getText().toString().isEmpty()
                && !emailEditText.getText().toString().isEmpty() && !passwordEditText.getText().toString().isEmpty()
                && !confirmPasswordEditText.getText().toString().isEmpty()) {
            createAccountButton.setEnabled(true);
            createAccountButton.setTextColor(getResources().getColor(R.color.black, null));
        } else if (createAccountButton.isEnabled() && (firstNameEditText.getText().toString().isEmpty() || lastNameEditText.getText().toString().isEmpty()
                || emailEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().isEmpty()
                || confirmPasswordEditText.getText().toString().isEmpty())) {
            createAccountButton.setEnabled(false);
            createAccountButton.setTextColor(getResources().getColor(R.color.gray, null));
        }
    }

    //Prints a message to the user in the form of a Toast
    private void printToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public void setCognitoUserPool(CognitoUserPool cognitoUserPool) {
        this.cognitoUserPool = cognitoUserPool;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_account_button:
                String emailAddress = emailEditText.getText().toString();
                //First check if the email is valid
                if (isEmailValid(emailEditText.getText().toString())) {
                    String password = passwordEditText.getText().toString();
                    //Check if the user follow the password guide line
                    if (passwordGuidelineCheck(password)) {
                        String confirmPassword = confirmPasswordEditText.getText().toString();
                        //Check if password and confirm password is equal
                        if (password.matches(confirmPassword)) {
                            String firstName = firstNameEditText.getText().toString();
                            String lastName = lastNameEditText.getText().toString();
                            createUserAccount(firstName, lastName, emailAddress, password);
                            progressBar.setVisibility(View.VISIBLE);
                            //Hide the soft input keyboard
                            View currentlyFocusedView = getActivity().getCurrentFocus();
                            currentlyFocusedView.clearFocus();
                            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(currentlyFocusedView.getWindowToken(), 0);
                        } else {
                            displayErrorMessage(R.string.invalid_confirm_password);
                        }
                    } else {

                        displayErrorMessage(R.string.incorrect_password_format);
                    }
                } else {
                    displayErrorMessage(R.string.incorrect_email_format);
                }
                break;
        }
    }

    public void setOnAccountCreatedListener(OnAccountCreatedListener onAccountCreatedListener) {
        this.onAccountCreatedListener = onAccountCreatedListener;
    }
}