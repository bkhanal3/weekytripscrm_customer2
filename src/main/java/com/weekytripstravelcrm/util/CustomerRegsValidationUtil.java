package com.weekytripstravelcrm.util;

import com.weekytripstravelcrm.exception.RegistrationException;
import com.weekytripstravelcrm.model.CustomerRegsModel;
import org.springframework.util.StringUtils;

public class CustomerRegsValidationUtil {

    public static void validateCustomerRegistration(CustomerRegsModel customerRegsModel) throws RegistrationException {
        if (customerRegsModel == null) {
            throw new RegistrationException("Registration data cannot be empty.");
        }

        if (!StringUtils.hasText(customerRegsModel.getFirstName()) || customerRegsModel.getFirstName().length() < 2) {
            throw new RegistrationException("First Name should be at least 2 characters long.");
        }

        if (!StringUtils.hasText(customerRegsModel.getLastName()) || customerRegsModel.getLastName().length() < 2) {
            throw new RegistrationException("Last Name should be at least 2 characters long.");
        }

        if (!StringUtils.hasText(customerRegsModel.getEmail())) {
            throw new RegistrationException("Email is required.");
        }

        if (!StringUtils.hasText(customerRegsModel.getMobileNumber()) || !customerRegsModel.getMobileNumber().matches("\\d{10}")) {
            throw new RegistrationException("Invalid Mobile Number. It should be 10 digits long.");
        }

        if (!StringUtils.hasText(customerRegsModel.getPassword()) || customerRegsModel.getPassword().length() < 8
                || !customerRegsModel.getPassword().matches(".*[A-Z].*")
                || !customerRegsModel.getPassword().matches(".*\\d.*")
                || !customerRegsModel.getPassword().matches(".*[@#$%^&+=].*")) {
            throw new RegistrationException("Invalid Password. It should be at least 8 characters long and contain at least one uppercase letter, one digit, and one special character.");
        }

        if (!customerRegsModel.getPassword().equals(customerRegsModel.getConfirmPassword())) {
            throw new RegistrationException("Password and Confirm Password do not match.");
        }
    }
}