package com.example.app.validator;


public class EmailValidator {


    public static boolean validateEmailExists(String email) {
    	if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            return false;
        }
    	return true;
    }
}