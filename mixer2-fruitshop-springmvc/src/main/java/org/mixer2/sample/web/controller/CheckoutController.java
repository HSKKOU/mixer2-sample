package org.mixer2.sample.web.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.mixer2.sample.service.PurchaseService;
import org.mixer2.sample.web.dto.Cart;
import org.mixer2.sample.web.dto.Shipping;
import org.mixer2.sample.web.view.helper.TransactionTokenHelper;
import org.mixer2.xhtml.exception.TagTypeUnmatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/checkout")
@SessionAttributes(value = { "shipping", "cart" })
public class CheckoutController {

    private Logger logger = Logger.getLogger(CheckoutController.class);

    @Autowired
    protected PurchaseService purchaseService;

    @ModelAttribute("shipping")
    public Shipping createShipping() {
        logger.debug("#### create shipping object...");
        return new Shipping();
    }

    @ModelAttribute("cart")
    public Cart createCart() {
        logger.debug("#### create cart object...");
        return new Cart();
    }

    @RequestMapping(value = "/shipping")
    public String shipping(
            Model model,
            @RequestParam(value = "redirected", required = false) boolean redirected,
            @Valid Shipping shipping, Errors errors) throws IOException,
            TagTypeUnmatchException, IllegalAccessException,
            InvocationTargetException {
        
        model.addAttribute("shipping", shipping);
        model.addAttribute("redirected", redirected);
        model.addAttribute("errors", errors);
        return "shippingView";
    }

    @RequestMapping(value = "confirm")
    public String confirm(Model model, Cart cart, @Valid Shipping shipping,
            Errors errors) throws IOException,
            TagTypeUnmatchException {

        // if cart is empty, redirect to cart view page.
        if (cart.getReadOnlyItemList().size() < 1) {
            return "redirect:/cart/view";
        }

        // validation
        if (errors.hasErrors()) {
            logger.debug(errors);
            return "redirect:shipping?redirected=true";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("shipping", shipping);
        return "confirmView";
    }

    @RequestMapping(value = "complete")
    public String complete(Shipping shipping, Cart cart, HttpSession httpSession)
            throws Exception {

        // check transaction token
        boolean checkResult = TransactionTokenHelper.checkToken(httpSession,
                shipping.getTransactionToken());
        if (!checkResult) {
            throw new Exception("Transaction Token Unmatch !");
        }

        boolean result = purchaseService.execPurchase(cart, shipping);
        if (result) {
            TransactionTokenHelper.removeToken(httpSession);
            cart.removeAll();
            logger.debug("### purchase succeed!!");
        }

        return "redirect:thankyou";
    }

    @RequestMapping(value = "thankyou")
    public String thankyou() throws IOException, TagTypeUnmatchException {
        return "thankyouView";
    }

}
