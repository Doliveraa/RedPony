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

import java.util.ArrayList;
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
    private EditText usernameEditText;
    private ProgressBar progressBar;
    private Button createAccountButton;
    //Constants
    private String TAG = "CreateAccountFragment";
    //Variables
    private AlertDialog.Builder builder;
    private CognitoUserPool cognitoUserPool;
    private boolean accountCreated;
    private ArrayList<EditText> listOfInputFields;
    //Interface
    public interface OnAccountCreatedListener{
        void onAccountCreated(CognitoUser cognitoUser);
        void onCreateAccountFinished();
    }

    private OnAccountCreatedListener onAccountCreatedListener;

    /**
     * Handler that receives successful or failed results from the user creating an account
     */
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

                //We were able to successfully create a user, start filling out the HttpUser item

                //Changes how the alert dialog works, once set to true, this activity will finish
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
                displayErrorMessage("Email already exists.");
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
        cognitoUserPool = AuthHelper.getCognitoUserPool(getActivity());

        //Initialize the views
        firstNameEditText = (EditText) getActivity().findViewById(R.id.first_name_edit_text);
        lastNameEditText = (EditText) getActivity().findViewById(R.id.last_name_edit_text);
        emailEditText = (EditText) getActivity().findViewById(R.id.email_edit_text);
        passwordEditText = (EditText) getActivity().findViewById(R.id.password_edit_text);
        confirmPasswordEditText = (EditText) getActivity().findViewById(R.id.confirm_password_edit_text);
        usernameEditText = (EditText) getActivity().findViewById(R.id.username_edit_text);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.create_account_progress_bar);
        createAccountButton = (Button) getActivity().findViewById(R.id.create_account_button);
        final TextView usernameFormatTextView = (TextView) getActivity().findViewById(R.id.text_view_username_format);
        final TextView passwordFormatTextView = (TextView) getActivity().findViewById(R.id.text_view_password_format);

        //Add all of the EditText views in an array to check if they are empty later on
        listOfInputFields = new ArrayList<>();
        listOfInputFields.add(firstNameEditText);
        listOfInputFields.add(lastNameEditText);
        listOfInputFields.add(emailEditText);
        listOfInputFields.add(passwordEditText);
        listOfInputFields.add(confirmPasswordEditText);
        listOfInputFields.add(usernameEditText);

        //Attach Listeners
        createAccountButton.setOnClickListener(this);
        usernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            //Makes the username format hint appear if on focus and disappear otherwise
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    usernameFormatTextView.setVisibility(View.VISIBLE);
                } else {
                    usernameFormatTextView.setVisibility(View.GONE);
                }
            }
        });
        passwordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    passwordFormatTextView.setVisibility(View.VISIBLE);
                } else {
                    passwordFormatTextView.setVisibility(View.GONE);
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

    /**
     * Calls the CognitoUserPool API to create a user account
     *
     * @param firstName The user's first name
     * @param lastName The user's last name
     * @param emailAddress The user's email address
     * @param password The user's password
     */
    private void createUserAccount(String firstName, String lastName, String emailAddress, String password) {
        CognitoUserAttributes userAttributes = new CognitoUserAttributes();

        //Add required user attributes
        userAttributes.addAttribute("name", firstName + " " + lastName);
        userAttributes.addAttribute("email", emailAddress);

        //Call the sign-up API
        cognitoUserPool.signUpInBackground(emailAddress, password, userAttributes, null, signupCallback);

    }

    /**
     * Checks the user's password to make sure it is strong enough to prevent dictionary attacks
     *
     * @param password The user's desired password
     *
     * @return True if the password is strong enough and false otherwise
     */
    private boolean passwordGuidelineCheck(String password) {
        /**
         * Passwords must contain at least one a-z character, one A-Z character
         * one 0-9 character,and be 8 characters long minimum
         */
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    /**
     * Checks if the email matches the specified pattern
     *
     * @param email The user's email
     *
     * @return True if the email specified is valid and false otherwise
     */
    public boolean isEmailValid(String email) {
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

    /**
     * Checks if the username matches the specified pattern
     *
     * @param username The user's username choice
     *
     * @return True if the username is valid and false otherwise
     */
    public boolean isUsernameValid(String username) {
        String expression = "(?:[a-zA-Z0-9._-]{3,12}$)";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(username);

        return matcher.matches();
    }


    /**
     * Creates and displays an Alert Dialog to the user in case of an error
     *
     * @param errorMessage The message to display the user
     */
    private void displayErrorMessage(String errorMessage) {
        builder.setTitle("Error Message");
        builder.setMessage(errorMessage);
        builder.show();
    }

    /**
     * This method will not be called if there exists any fields that are empty
     * Allows the user to create their account upon successful pre-requisites that include:
     *  AstralUser email field must contain the right format
     *  AstralUser password field must follow proper password guidelines
     *  Confirm password must match the user's previously entered password
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_account_button:
                //Check if there are any empty fields
                for(EditText inputField : listOfInputFields) {
                    if(inputField.getText().toString().isEmpty()) {
                        displayErrorMessage("All fields must not be empty.");
                        return;
                    }
                }
                //Check if username is valid
                String username = usernameEditText.getText().toString();
                if(!isUsernameValid(username)) {
                    displayErrorMessage("Username must be 3-12 characters and only use" +
                            " the following: \n(a-z, A-Z, 0-9, dots, dashes, underlines");
                    return;
                }

                //Check if the email address field is properly formatted and display an error
                //error message if the format is wrong
                String emailAddress = emailEditText.getText().toString().toLowerCase();
                if(!isEmailValid(emailAddress)) {
                    displayErrorMessage("Email is not valid.");
                    return;
                }

                //Check if the user password field is not empty and contains the proper format
                //and displays an error message if the password does not meet the guidelines
                String password = passwordEditText.getText().toString();
                if(!passwordGuidelineCheck(password)) {
                    displayErrorMessage("Password must contain a minimum of 8 characters and " +
                            "the following : (a-z, A-Z, 0-9).");
                    return;
                }

                //Makes sure that the confirm password is exactly the same as the password
                //and displays an error message if the passwords do not equal
                String confirmPassword = confirmPasswordEditText.getText().toString();
                if(!confirmPassword.equals(password)) {
                    displayErrorMessage("Passwords do not equal");
                    return;
                }

                //Retrieves the first name and last name of the user from the edit boxes
                String firstName = firstNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();

                //Everything is ok, begin creating the user account
                createUserAccount(firstName, lastName, emailAddress, password);

                //Make the progress bar appear to show that the application is still working
                //but is in the process of completing a task
                progressBar.setVisibility(View.VISIBLE);
                //Hide the soft input keyboard from the user
                View currentlyFocusedView = getActivity().getCurrentFocus();
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(currentlyFocusedView.getWindowToken(), 0);

                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        //Clear all of the Edit Text boxes
        firstNameEditText.setText("");
        lastNameEditText.setText("");
        usernameEditText.setText("");
        confirmPasswordEditText.setText("");
        emailEditText.setText("");
        passwordEditText.setText("");
    }

    /**
     * Allows the Fragment activity to sent events to the container activity
     *
     * @param onAccountCreatedListener The listener implemented in the container activity
     */
    public void setOnAccountCreatedListener(OnAccountCreatedListener onAccountCreatedListener) {
        this.onAccountCreatedListener = onAccountCreatedListener;
    }
}