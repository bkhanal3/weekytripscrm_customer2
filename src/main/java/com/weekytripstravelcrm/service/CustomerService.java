package com.weekytripstravelcrm.service;

import com.weekytripstravelcrm.email.EmailService;
import com.weekytripstravelcrm.email.MailRequest;
import com.weekytripstravelcrm.entity.CustomerAddress;
import com.weekytripstravelcrm.entity.Customer;
import com.weekytripstravelcrm.exception.NullException;
import com.weekytripstravelcrm.exception.RegistrationException;
import com.weekytripstravelcrm.model.CustomerRegsAddressModel;
import com.weekytripstravelcrm.model.CustomerRegsModel;
import com.weekytripstravelcrm.repository.CustomerRepository;
import com.weekytripstravelcrm.util.KeyGenerator;
import com.weekytripstravelcrm.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomerService {
    Logger log = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Registers a new customer with the provided registration data.
     *
     * @param customerRegsModel The customer registration data to be processed and saved.
     * @return A success message if registration is successful.
     * @throws RegistrationException If registration fails due to validation or database issues.
     */

    @Transactional
    public String registerCustomer(CustomerRegsModel customerRegsModel) throws RegistrationException {
        log.info("Saving registered customers record.");
        if (customerRegsModel != null) {
            try {
                validateRequestModel(customerRegsModel);
                if (customerRepository.findByEmail(customerRegsModel.getEmail()) != null) {
                    throw new RegistrationException("An account with the same email already exists");
                }

                if (customerRepository.existsByMobileNumber(customerRegsModel.getMobileNumber())) {
                    throw new RegistrationException("An account with the same mobile number already exists");
                }

                Customer newCustomer = new Customer();

                Long maxCustomerID = customerRepository.findMaxCustomerID();
                log.info("Max Customer ID found in the database: {}", maxCustomerID);

                String prefix = "WTC";
                String startingValue = "1000";
                long startingLongValue = Long.parseLong(startingValue);
                newCustomer.setCustomerID(KeyGenerator.generateId(prefix,maxCustomerID, CustomerService.class,startingLongValue));

                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                newCustomer.setFirstName(customerRegsModel.getFirstName());
                newCustomer.setLastName(customerRegsModel.getLastName());
                newCustomer.setEmail(customerRegsModel.getEmail());
                newCustomer.setMobileNumber(customerRegsModel.getMobileNumber());
                newCustomer.setPassword(bCryptPasswordEncoder.encode(customerRegsModel.getPassword()));
                newCustomer.setConfirmPassword(bCryptPasswordEncoder.encode(customerRegsModel.getConfirmPassword()));

                CustomerRegsAddressModel addressModel = customerRegsModel.getAddress();
                if (addressModel != null) {
                    CustomerAddress addressEntity = new CustomerAddress();
                    addressEntity.setStreet(addressModel.getStreet());
                    addressEntity.setApt(addressModel.getApt());
                    addressEntity.setCity(addressModel.getCity());
                    addressEntity.setState(addressModel.getState());
                    addressEntity.setPinCode(addressModel.getPinCode());
                    newCustomer.setAddress(addressEntity);

                }

                customerRepository.save(newCustomer);
                log.info("New customer saved with Customer ID: {}", newCustomer.getCustomerID());

                MailRequest mailRequest = new MailRequest();
                mailRequest.setName("Weeky Travels");
                mailRequest.setFrom("wewkyTravels@email.com");
                mailRequest.setTo(customerRegsModel.getEmail());
                mailRequest.setSubject("Registration Confirmation");
                mailRequest.setTemplateName("email-template.jsp");

                Map<String, Object> emailModel = new HashMap<>();
                emailModel.put("firstName", customerRegsModel.getFirstName());
                emailModel.put("lastName", customerRegsModel.getLastName());
                emailModel.put("email", customerRegsModel.getEmail());
                emailModel.put("password", customerRegsModel.getPassword());
                emailModel.put("Subject", "Registration Confirmation");
                emailService.sendEmail(mailRequest, emailModel);

                log.info("Email sent to: {}", customerRegsModel.getEmail());
                return "Customer registered successfully!";
            } catch (Exception e) {
                log.error("Failed to save the customer record into the database: {}", e.getMessage());
                throw new RegistrationException("An error occurred while processing the registration: " + e.getMessage());
            }
        }else{
            throw new NullException("Customer Data cannot be null");
        }
    }

    /**
     * Validates the provided customer registration data.
     *
     * @param customerRegsModel The customer registration data to be validated.
     * @throws Exception If any validation checks fail.
     */
    public void validateRequestModel(CustomerRegsModel customerRegsModel) throws Exception {
        ValidateUtil.isValidName(customerRegsModel.getFirstName());
        ValidateUtil.isValidName(customerRegsModel.getLastName());
        ValidateUtil.isValidEmail(customerRegsModel.getEmail());
        ValidateUtil.isValidMobile(customerRegsModel.getMobileNumber());
        ValidateUtil.isValidPassword(customerRegsModel.getPassword());
        ValidateUtil.doPasswordsMatch(customerRegsModel.getPassword(), customerRegsModel.getConfirmPassword());
    }
}