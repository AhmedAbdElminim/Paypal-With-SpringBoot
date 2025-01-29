package com.paypal.spring.paypal_with_spring.controllers;

import org.springframework.stereotype.Controller;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.spring.paypal_with_spring.services.PaypalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaypalController {
    private final PaypalService service;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/payment/create")
    public RedirectView createPayment() throws PayPalRESTException {

        try {
            String cancelUrl = "http://localhost:8080/payment/cancel";
            String successUrl = "http://localhost:8080/payment/success";
            Payment payment = service.createPayment(
                    10.0,
                    "USD",
                    "paypal",
                    "sale",
                    "Payment Sescription",
                    cancelUrl,
                    successUrl);

            for (Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return new RedirectView(link.getHref());
                }

            }

        } catch (PayPalRESTException e) {
            log.error(null, e);
        }
        return new RedirectView("/payment/error");

    }

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payertId) {
        try {
            Payment payment = service.executePayment(paymentId, payertId);

            if (payment.getState().equals("approved")) {
                return "paymentSuccess";
            }

        } catch (PayPalRESTException e) {
            log.error(null, e);
        }
        return "paymentSuccess";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel() {

        return "paymentCancel";
    }

    @GetMapping("/payment/error")
    public String paymentError() {

        return "paymentError";
    }

}
