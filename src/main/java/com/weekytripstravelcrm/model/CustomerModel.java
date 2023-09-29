package com.weekytripstravelcrm.model;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CustomerModel {
    private String name;
    private String email;
    private String contact;
    private CustomerAddressModel customerAddress;

}
