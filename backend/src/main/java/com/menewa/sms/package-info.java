/**
 * Envoi de SMS (Twilio ou passerelle HTTP JSON générique). Sans dépendance : utilisé par identity (codes OTP)
 * et par platform (notifications).
 */
@org.springframework.modulith.ApplicationModule(displayName = "SMS")
package com.menewa.sms;
