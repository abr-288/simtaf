package com.menewa.identity;

interface OtpDelivery {
    void send(String phone, String code);
}
