package com.github.fenrir.xtraceprocessor.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class URISelectorConfig {
    private Map<String, List<String>> selector = null;

    public URISelectorConfig(){}


    public Map<String, List<String>> getSelector() {
        return selector;
    }

    public void setSelector(Map<String, List<String>> selector) {
        this.selector = selector;
    }

    public String match(String serviceName, String interfaceName){
        assert this.selector != null;
        List<String> interfaceList = this.selector.getOrDefault(serviceName, null);
        if(interfaceList == null)
            return interfaceName;

        for(String target : interfaceList){
            if(this._match(interfaceName, target)){
                return target;
            }
        }
        return interfaceName;

    }

    private boolean _match(String uri, String target){
        String[] uriSplit = uri.split("/");
        String[] targetSplit = target.split("/");

        if(uriSplit.length != targetSplit.length){
            return false;
        }else{
            for(int i = 0; i < uriSplit.length; i++){
                if(!targetSplit[i].equals("*")){
                    if(!targetSplit[i].equals(uriSplit[i]))
                        return false;
                }
            }
        }
        return true;
    }

    public static URISelectorConfig tmpCreate(){
        Map<String, List<String>> selector = new ConcurrentHashMap<>();

        // admin-basic-info-service
        List<String> adminBasicInfoService = new ArrayList<>();
        adminBasicInfoService.add("/api/v1/adminbasicservice/adminbasic/contacts/*");
        adminBasicInfoService.add("/api/v1/adminbasicservice/adminbasic/trains/*");
        adminBasicInfoService.add("/api/v1/adminbasicservice/adminbasic/configs/*");
        adminBasicInfoService.add("/api/v1/adminbasicservice");
        selector.put("admin-basic-info-service", adminBasicInfoService);

        // verification-code-service
        List<String> verificationCodeService = new ArrayList<>();
        verificationCodeService.add("/api/v1/verifycode/verify/*");
        selector.put("verification-code-service", verificationCodeService);

        // travel2-service
        List<String> travel2Service = new ArrayList<>();
        travel2Service.add("/api/v1/travel2service/train_types/*");
        travel2Service.add("/api/v1/travel2service/routes/*");
        travel2Service.add("/api/v1/travel2service/trips/*");
        selector.put("travel2-service", travel2Service);

        // train-service
        List<String> trainService = new ArrayList<>();
        trainService.add("/api/v1/trainservice/trains/*");
        selector.put("train-service", trainService);

        // travel-service
        List<String> travelService = new ArrayList<>();
        travelService.add("/api/v1/travelservice/train_types/*");
        travelService.add("/api/v1/travelservice/routes/*");
        travelService.add("/api/v1/travelservice/trips/*");
        selector.put("travel-service", travelService);

        // ticketinfo-service
        List<String> ticketInfoService = new ArrayList<>();
        ticketInfoService.add("/api/v1/ticketinfoservice/ticketinfo/*");
        selector.put("ticketinfo-service", ticketInfoService);

        // station-service
        List<String> stationService = new ArrayList<>();
        stationService.add("/api/v1/stationservice/stations/id/*");
        stationService.add("/api/v1/stationservice/stations/name/*");
        selector.put("station-service", stationService);

        // route-service
        List<String> routeService = new ArrayList<>();
        routeService.add("/api/v1/routeservice/routes/*");
        routeService.add("/api/v1/routeservice/routes/*/*");
        selector.put("route-service", routeService);

        // price-service
        List<String> priceService = new ArrayList<>();
        priceService.add("/api/v1/priceservice/prices/*/*");
        selector.put("price-service", priceService);

        // order-service
        List<String> orderService = new ArrayList<>();
        orderService.add("/api/v1/orderservice/order/*/*");
        orderService.add("/api/v1/orderservice/order/price/*");
        orderService.add("/api/v1/orderservice/order/orderPay/*");
        orderService.add("/api/v1/orderservice/order/*");
        orderService.add("/api/v1/orderservice/order/status/*/*");
        orderService.add("/api/v1/orderservice/order/security/*/*");
        selector.put("order-service", orderService);

        // order-other-service
        List<String> orderOtherService = new ArrayList<>();
        orderOtherService.add("/api/v1/orderOtherService/orderOther/*/*");
        orderOtherService.add("/api/v1/orderOtherService/orderOther/price/*");
        orderOtherService.add("/api/v1/orderOtherService/orderOther/orderPay/*");
        orderOtherService.add("/api/v1/orderOtherService/orderOther/*");
        orderOtherService.add("/api/v1/orderOtherService/orderOther/status/*/*");
        orderOtherService.add("/api/v1/orderOtherService/orderOther/security/*/*");
        selector.put("order-other-service", orderOtherService);

        // inside-payment-service
        List<String> insidePaymentService = new ArrayList<>();
        insidePaymentService.add("/api/v1/inside_pay_service/inside_payment/*/*");
        insidePaymentService.add("/api/v1/inside_pay_service/inside_payment/drawback/*/*");
        selector.put("inside-payment-service", insidePaymentService);

        // food-service
        List<String> foodService = new ArrayList<>();
        foodService.add("/api/v1/foodservice/orders/*");
        foodService.add("/api/v1/foodservice/foods/*/*/*/*");
        selector.put("food-service", foodService);

        // food-map-service
        List<String> foodMapService = new ArrayList<>();
        foodMapService.add("/api/v1/foodmapservice/foodstores/*");
        foodMapService.add("/api/v1/foodmapservice/trainfoods/*");
        selector.put("food-map-service", foodMapService);

        // execute-service
        List<String> executeService = new ArrayList<>();
        executeService.add("/api/v1/executeservice/execute/execute/*");
        executeService.add("/api/v1/executeservice/execute/collected/*");
        selector.put("execute-service", executeService);

        // contacts-service
        List<String> contactsService = new ArrayList<>();
        contactsService.add("/api/v1/contactservice/contacts/*");
        contactsService.add("/api/v1/contactservice/contacts/account/*");
        selector.put("contacts-service", contactsService);

        // config-service
        List<String> configService = new ArrayList<>();
        configService.add("/api/v1/configservice/configs/*");
        selector.put("config-service", configService);

        // consign-service
        List<String> consignService = new ArrayList<>();
        consignService.add("/api/v1/consignservice/consigns/account/*");
        consignService.add("/api/v1/consignservice/consigns/order/*");
        consignService.add("/api/v1/consignservice/consigns/*");
        selector.put("consign-service", consignService);

        // consign-price-service
        List<String> consignPriceService = new ArrayList<>();
        consignPriceService.add("/api/v1/consignpriceservice/consignprice/*/*");
        selector.put("consign-price-service", consignPriceService);

        // user-service
        List<String> userService = new ArrayList<>();
        userService.add("/api/v1/userservice/users/*");
        userService.add("/api/v1/userservice/users/id/*");
        selector.put("user-service", userService);

        // cancel-service
        List<String> cancelService = new ArrayList<>();
        cancelService.add("/api/v1/cancelservice/cancel/refound/*");
        cancelService.add("/api/v1/cancelservice/cancel/*/*");
        selector.put("cancel-service", cancelService);

        // basic-service
        List<String> basicService = new ArrayList<>();
        basicService.add("/api/v1/basicservice/basic/*");
        selector.put("basic-service", basicService);

        // auth-service
        List<String> authService = new ArrayList<>();
        authService.add("/api/v1/users/*");
        selector.put("auth-service", authService);

        // assurance-service
        List<String> assuranceService = new ArrayList<>();
        assuranceService.add("/api/v1/assuranceservice/assurances/assuranceid/*");
        assuranceService.add("/api/v1/assuranceservice/assurances/orderid/*");
        assuranceService.add("/api/v1/assuranceservice/assurances/*/*/*");
        assuranceService.add("/api/v1/assuranceservice/assurances/*/*");
        selector.put("assurance-service", assuranceService);

        // admin-user-service
        List<String> adminUserService = new ArrayList<>();
        adminUserService.add("/api/v1/adminuserservice/users/*");
        selector.put("admin-user-service", adminUserService);

        // admin-travel-service
        List<String> adminTravelService = new ArrayList<>();
        adminTravelService.add("/api/v1/admintravelservice/admintravel/*");
        selector.put("admin-travel-service", adminTravelService);

        // admin-route-service
        List<String> adminRouteService = new ArrayList<>();
        adminRouteService.add("/api/v1/adminrouteservice/adminroute/*");
        selector.put("admin-route-service", adminRouteService);

        // admin-order-service
        List<String> adminOrderService = new ArrayList<>();
        adminOrderService.add("/api/v1/adminorderservice/adminorder/*/*");
        selector.put("admin-order-service", adminOrderService);

        // security-service
        List<String> securityService = new ArrayList<>();
        securityService.add("/api/v1/securityservice/securityConfigs/*");
        selector.put("security-service", securityService);

        URISelectorConfig config = new URISelectorConfig();
        config.selector = selector;
        return config;
    }
}
