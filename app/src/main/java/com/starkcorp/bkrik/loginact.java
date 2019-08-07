package com.starkcorp.bkrik;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class loginact extends AppCompatActivity {
private EditText EmailorPhoneField;
private EditText passwordField;
private FirebaseAuth mAuth;
private ProgressDialog progressDialog;
private boolean isNumber=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginact);
        Button loginbtn=(Button)findViewById(R.id.loginbutton);
        EmailorPhoneField=(EditText)findViewById(R.id.phoneEmail);
        passwordField=(EditText)findViewById(R.id.loginpassword);

        mAuth=FirebaseAuth.getInstance();
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String emailOrPhone=EmailorPhoneField.getText().toString();
                final String password=passwordField.getText().toString();

               /* if(TextUtils.isDigitsOnly(emailOrPhone)) {
                    isNumber = true;
                }else{
                    isNumber = false;
                }

                if(isNumber==true){
                    if(!validatePhoneNumber()){
                        return;
                    }
                }else{
                    if(!validateEmail()){
                        return;
                    }
                }
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("users");

                ValueEventListener eventListener=new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean accountExist=false;
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            String number=(String)ds.child("phoneNumber").getValue();
                            number=number.substring(3);
                            Log.i("fuck",number);
                            String email=(String)ds.child("email").getValue();
                        if(number.equals(emailOrPhone)||email.equals(emailOrPhone)){
                            accountExist=true;
                            String loginpassword=(String)ds.child("password").getValue();
                            if(loginpassword.equals(password)){
                                progressDialog = ProgressDialog.show(loginact.this,"Wait","authenticating",false,false);
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(loginact.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                // If sign in fails, display a message to the user. If sign in succeeds
                                                // the auth state listener will be notified and logic to handle the
                                                // signed in user can be handled in the listener.
                                                progressDialog.dismiss();
                                                if (!task.isSuccessful()) {
                                                    // there was an error

                                                    Toast.makeText(loginact.this, "authentication failed", Toast.LENGTH_LONG).show();

                                                } else {
                                                    Intent intent = new Intent(loginact.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        });

                            }else{
                                passwordField.setError("Incorrect password");
                                Toast.makeText(loginact.this,"Incorrect password",Toast.LENGTH_LONG).show();
                            }
                        }
                        }
                        if(!accountExist) {
                            if (isNumber) {
                                EmailorPhoneField.setError("No account with this Number");
                                Toast.makeText(loginact.this, "No account with this Number", Toast.LENGTH_LONG).show();
                            } else {
                                EmailorPhoneField.setError("No account with this Email");
                                Toast.makeText(loginact.this, "No account with this Email", Toast.LENGTH_LONG).show();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                    ref.addListenerForSingleValueEvent(eventListener);
*/
               FirebaseAuth.getInstance().signInWithEmailAndPassword(emailOrPhone,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                   @Override
                   public void onSuccess(AuthResult authResult) {
                       FirebaseUser user=authResult.getUser();
                       Toast.makeText(loginact.this,user.getDisplayName(),Toast.LENGTH_LONG).show();
                       Intent intent = new Intent(loginact.this, MainActivity.class);
                       startActivity(intent);
                       finish();
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(loginact.this,e.getMessage(),Toast.LENGTH_LONG).show();
                   }
               });
            }
        });


    }
    private boolean validatePhoneNumber() {
        String phoneNumber = EmailorPhoneField.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            EmailorPhoneField.setError("Invalid phone number.");
            return false;
        }

        return true;
    }
    private boolean validateEmail() {
        String Email = EmailorPhoneField.getText().toString();
        if (TextUtils.isEmpty(Email)||!Email.contains("@")||!Email.contains(".")) {
            EmailorPhoneField.setError("Invalid Email.");
            return false;
        }

        return true;
    }
    public void signupwindow(View v){
        Intent intent = new Intent(loginact.this, activitylogin.class);
        startActivity(intent);
        finish();
    }

}
